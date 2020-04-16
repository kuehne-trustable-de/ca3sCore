package de.trustable.ca3s.core.web.rest.support;

import java.time.Instant;
import java.util.Date;
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

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
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
	private ApplicationEventPublisher applicationEventPublisher;
	
	
    /**
     * {@code POST  /administerCertificate} : revoke a certificate.
     *
     * @param 
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerCertificate")
	@Transactional
    public ResponseEntity<Long> administerRequest(@Valid @RequestBody CertificateAdministrationData adminData) {
    	
    	LOG.debug("REST request to revoke certificate : {}", adminData);
        
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String raOfficerName = auth.getName();

    	Optional<Certificate> optCert = certificateRepository.findById(adminData.getCertificateId());
    	if( optCert.isPresent()) {
    		
    		Certificate certificate = optCert.get();

    		revokeCertificate(certificate, adminData, raOfficerName);

			applicationEventPublisher.publishEvent(
			        new AuditApplicationEvent(
			        		raOfficerName, AuditUtil.AUDIT_CERTIFICATE_REVOKED, "certificate " + certificate.getId() + " revoked by RA Officer  '" + raOfficerName + "'"));
			
    		return new ResponseEntity<Long>(adminData.getCertificateId(), HttpStatus.OK);
    	
    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}


    /**
     * {@code POST  /withdrawOwnCertificate} : Withdraw own certificate.
     *
     * @param 
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

    		revokeCertificate(certificate, adminData, userName);

			applicationEventPublisher.publishEvent(
			        new AuditApplicationEvent(
			        		userName, AuditUtil.AUDIT_CERTIFICATE_REVOKED, "certificate " + certificate.getId() + " revoked by owner '" + userName + "'"));
			
    		return new ResponseEntity<Long>(adminData.getCertificateId(), HttpStatus.OK);
    	
    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}

	private void revokeCertificate(Certificate certDao, final CertificateAdministrationData adminData, final String revokingUser) {
		

		if (certDao.isRevoked()) {
			LOG.warn("failureReason: " +
					"certificate with id '" + certDao.getId() + "' already revoked.");
		}

		CRLReason crlReason = cryptoUtil.crlReasonFromString(adminData.getRevocationReason());

		String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
		LOG.debug("crlReason : " + crlReasonStr + " from " + adminData.getRevocationReason());

		Date revocationDate = new Date();
		
		bpmnUtil.startCertificateRevoctionProcess(certDao, crlReason, revocationDate);

		certDao.setRevoked(true);
		certDao.setRevokedSince(Instant.now());
		certDao.setRevocationReason(crlReasonStr);
		
		if( adminData.getComment() != null && adminData.getComment().trim().length() > 0) {
			certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_REVOCATION_COMMENT, adminData.getComment());
		}
		certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_REVOKED_BY, revokingUser);
		
		/*
		 * @ todo
		 */
		certDao.setRevocationExecutionId("39");

		certificateRepository.save(certDao);

	}

}
