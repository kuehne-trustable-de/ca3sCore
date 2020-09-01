package de.trustable.ca3s.core.web.rest.support;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

import javax.validation.Valid;

import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.schedule.CertExpiryScheduler;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.util.AuditUtil;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.CertificateAdministrationData;
import de.trustable.util.CryptoUtil;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class CertificateAdministration {

	private final Logger LOG = LoggerFactory.getLogger(CertificateAdministration.class);

    @Autowired
    private CertificateRepository certificateRepository;

	@Autowired
	private BPMNUtil bpmnUtil;

	@Autowired
	private CryptoUtil cryptoUtil;
	
  	@Autowired
  	private CertificateUtil certUtil;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private MailService mailService;
	
	@Autowired
	private CertExpiryScheduler certExpiryScheduler;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	
    /**
     * {@code POST  /administerCertificate} : revoke a certificate.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerCertificate")
	@Transactional
    public ResponseEntity<Long> administerCertificate(@Valid @RequestBody CertificateAdministrationData adminData) {
    	
    	LOG.debug("REST request to revoke certificate : {}", adminData);
        
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String raOfficerName = auth.getName();

    	Optional<Certificate> optCert = certificateRepository.findById(adminData.getCertificateId());
    	if( optCert.isPresent()) {
    		
    		Certificate cert = optCert.get();

    		try {
				revokeCertificate(cert, adminData, raOfficerName);

				applicationEventPublisher.publishEvent(
				        new AuditApplicationEvent(
				        		raOfficerName, AuditUtil.AUDIT_CERTIFICATE_REVOKED, "certificate " + cert.getId() + " revoked by RA Officer  '" + raOfficerName + "'"));
	
				CSR csr = cert.getCsr();
				if( csr != null) {
					Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
					if( optUser.isPresent()) {
						User requestor = optUser.get();
				        if (requestor.getEmail() == null) {
				        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
				        }else {
				        
					        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
					        Context context = new Context(locale);
					        context.setVariable("csr", csr);
					        context.setVariable("cert", cert);
					        String subject = cert.getSubject();
					        if( subject == null ) {
					        	subject =  "";
					        }
					        String[] args = {subject, cert.getSerial(), cert.getIssuer()};
					        mailService.sendEmailFromTemplate(context, requestor, "mail/revokedCertificateEmail", "email.revokedCertificate.title", args);
				        }
					} else {
						LOG.info("certificate requestor '{}' unknown!", csr.getRequestedBy());
					}
				}
	
	    		return new ResponseEntity<Long>(adminData.getCertificateId(), HttpStatus.OK);

			} catch (GeneralSecurityException e) {
	    		return ResponseEntity.badRequest().build();
			}

    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}


    /**
     * {@code POST  /withdrawOwnCertificate} : Withdraw own certificate.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/withdrawOwnCertificate")
	@Transactional
    public ResponseEntity<Long> withdrawOwnCertificate(@Valid @RequestBody CertificateAdministrationData adminData) {
    	
    	LOG.debug("REST request to withdraw Certificate : {}", adminData);
        
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();

    	Optional<Certificate> optCert = certificateRepository.findById(adminData.getCertificateId());
    	if( optCert.isPresent()) {
    		
    		Certificate certificate = optCert.get();

    		String requestedBy = certificate.getCsr().getRequestedBy();
    		if( userName == null ||
    				requestedBy == null ||
    				!userName.equals(requestedBy) ){
    			
    	    	LOG.debug("REST request by '{}' to revoke certificate '{}' rejected ", userName, adminData.getCertificateId());
        		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    		}

    		try {
	    		revokeCertificate(certificate, adminData, userName);
	
				applicationEventPublisher.publishEvent(
				        new AuditApplicationEvent(
				        		userName, AuditUtil.AUDIT_CERTIFICATE_REVOKED, "certificate " + certificate.getId() + " revoked by owner '" + userName + "'"));
				
	    		return new ResponseEntity<Long>(adminData.getCertificateId(), HttpStatus.OK);
	    		
			} catch (GeneralSecurityException e) {
	    		return ResponseEntity.badRequest().build();
			}

    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}

    /**
     * 
     * @param certDao
     * @param adminData
     * @param revokingUser
     * @throws GeneralSecurityException 
     */
	private void revokeCertificate(Certificate certDao, final CertificateAdministrationData adminData, final String revokingUser) throws GeneralSecurityException {
		

		if (certDao.isRevoked()) {
			LOG.warn("failureReason: " +
					"certificate with id '" + certDao.getId() + "' already revoked.");
		}

		CRLReason crlReason = cryptoUtil.crlReasonFromString(adminData.getRevocationReason());

		String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
		LOG.debug("crlReason : " + crlReasonStr + " from " + adminData.getRevocationReason());

		Date revocationDate = new Date();
		
		bpmnUtil.startCertificateRevoctionProcess(certDao, crlReason, revocationDate);

		// @todo isn't this already done in the process?
		certDao.setActive(false);
		certDao.setRevoked(true);
		certDao.setRevokedSince(Instant.now());
		certDao.setRevocationReason(crlReasonStr);
		
		if( adminData.getComment() != null && adminData.getComment().trim().length() > 0) {
			certDao.setAdministrationComment(adminData.getComment());
		}
		certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_REVOKED_BY, revokingUser);
		
		/*
		 * @ todo
		 */
		certDao.setRevocationExecutionId("39");

		certificateRepository.save(certDao);

	}

    /**
     * {@code POST  /withdrawOwnCertificate} : Withdraw own certificate.
     *
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/sendExpiringCertificateEmail")
	@Transactional
    public ResponseEntity<Integer> sendExpiringCertificateEmail() {
    	int nExpiringCerts = certExpiryScheduler.notifyRAOfficerHolderOnExpiry();
		return new ResponseEntity<Integer>(nExpiringCerts, HttpStatus.OK);
    }
}
