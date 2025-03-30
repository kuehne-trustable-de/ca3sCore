package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.schedule.CertExpiryScheduler;
import de.trustable.ca3s.core.service.AsyncNotificationService;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CRLUpdateInfo;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.AdministrationType;
import de.trustable.ca3s.core.web.rest.data.CertificateAdministrationData;
import de.trustable.ca3s.core.service.dto.NamedValue;
import de.trustable.util.CryptoUtil;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class CertificateAdministration {

	private final Logger LOG = LoggerFactory.getLogger(CertificateAdministration.class);

    final private CertificateRepository certificateRepository;

    final private CertificateAttributeRepository certificateAttributeRepository;

    final private BPMNUtil bpmnUtil;

    final private CryptoUtil cryptoUtil;

    final private CertificateUtil certUtil;

    private final CSRUtil csrUtil;
    private final CRLUtil crlUtil;

    final private UserRepository userRepository;

    final private CertExpiryScheduler certExpiryScheduler;

    final private AsyncNotificationService asyncNotificationService;

    final private AuditService auditService;

    public CertificateAdministration(CertificateRepository certificateRepository,
                                     CertificateAttributeRepository certificateAttributeRepository,
                                     BPMNUtil bpmnUtil, CryptoUtil cryptoUtil,
                                     CertificateUtil certUtil,
                                     CSRUtil csrUtil, CRLUtil crlUtil, UserRepository userRepository,
                                     CertExpiryScheduler certExpiryScheduler,
                                     AsyncNotificationService asyncNotificationService,
                                     AuditService auditService) {
        this.certificateRepository = certificateRepository;
        this.certificateAttributeRepository = certificateAttributeRepository;
        this.bpmnUtil = bpmnUtil;
        this.cryptoUtil = cryptoUtil;
        this.certUtil = certUtil;
        this.csrUtil = csrUtil;
        this.crlUtil = crlUtil;
        this.userRepository = userRepository;
        this.certExpiryScheduler = certExpiryScheduler;
        this.asyncNotificationService = asyncNotificationService;
        this.auditService = auditService;
    }

    /**
     * {@code POST  /administerCertificate} : revoke a certificate.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerCertificate")
	@Transactional
    public ResponseEntity<Long> administerCertificate(@Valid @RequestBody CertificateAdministrationData adminData) {

    	LOG.debug("REST request to revoke / update certificate : {}", adminData);

    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String raOfficerName = auth.getName();

    	Optional<Certificate> optCert = certificateRepository.findById(adminData.getCertificateId());
    	if( optCert.isPresent()) {

    		Certificate cert = optCert.get();

            try {
                if(AdministrationType.REVOKE.equals(adminData.getAdministrationType())){
                    revokeCertificate(cert, adminData, raOfficerName);

                    CSR csr = cert.getCsr();
                    if (csr != null) {
                        Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
                        if (optUser.isPresent()) {
                            User requestor = optUser.get();
                            if (requestor.getEmail() == null) {
                                LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
                            } else {
                                Set<String> additionalEmailSet = csrUtil.getAdditionalEmailRecipients(cert.getCsr());
                                asyncNotificationService.notifyUserCertificateRevokedAsync(requestor, cert, csr, additionalEmailSet );
                            }
                        } else {
                            LOG.info("certificate requestor '{}' unknown!", csr.getRequestedBy());
                        }
                    }
                    updateComment(adminData, cert);
                    updateARAttributes(adminData, cert);

                } else if(AdministrationType.UPDATE.equals(adminData.getAdministrationType())){
                    updateComment(adminData, cert);
                    updateARAttributes(adminData, cert);
                    updateTrustedFlag(adminData, cert);
                } else if(AdministrationType.UPDATE_CRL.equals(adminData.getAdministrationType())){

                    CRLUpdateInfo crlInfo = certUtil.checkAllCRLsForCertificate( cert,
                        CertificateUtil.convertPemToCertificate(cert.getContent()),
                        crlUtil,
                        new HashSet<>());

                    if( !crlInfo.isbCRLDownloadSuccess() ) {
                        LOG.info("Downloading all CRL #{} for certificate {} failed", crlInfo.getCrlUrlCount(), cert.getId());
                    }
                } else {
                    LOG.info("administration type '{}' unexpected!", adminData.getAdministrationType());
                    return ResponseEntity.badRequest().build();
                }

                certUtil.setCertificateComment(cert, adminData.getComment());

                return new ResponseEntity<>(adminData.getCertificateId(), HttpStatus.OK);
            } catch (GeneralSecurityException | RuntimeException e) {
                LOG.debug("problem administrating certificate", e );
                auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_ADMINISTRATION_FAILED,e.getMessage(), cert));
                return ResponseEntity.badRequest().build();
			}

    	}else {
    		return ResponseEntity.notFound().build();
    	}

	}

    private void updateTrustedFlag(CertificateAdministrationData adminData, Certificate cert) {
        if( adminData.getTrusted() && cert.isTrusted()){
            LOG.debug("Certificate id {} already marked as trusted", cert.getId() );
        }else if( adminData.getTrusted() && !cert.isTrusted()){
            LOG.debug("Setting Certificate id {} as trusted", cert.getId() );
            if(cert.isRevoked()){
                LOG.error("Cannot set revoked Certificate id {} as trusted!", cert.getId() );
                return;
            }
            if(!cert.isActive()){
                LOG.error("Cannot set expired Certificate id {} as trusted!", cert.getId() );
                return;
            }

            if(cert.isEndEntity()){
                LOG.error("Cannot set end entity certificate id {} as trusted!", cert.getId() );
                return;
            }

            if(!cert.isSelfsigned()){
                LOG.warn("Trying to set the certificate id {} as trusted, but it is not selfsigned!", cert.getId() );
            }
            auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_SET_TRUSTED, cert));
            cert.setTrusted(true);
            certificateRepository.save(cert);
        }else if( !adminData.getTrusted() && cert.isTrusted()){
            LOG.debug("Revoking 'trusted' status from certificate id {}", cert.getId() );
            auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_UNSET_TRUSTED, cert));
            cert.setTrusted(false);
            certificateRepository.save(cert);
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
    		if(userName == null ||
                !userName.equals(requestedBy)){

    	    	LOG.debug("REST request by '{}' to revoke certificate '{}' rejected ", userName, adminData.getCertificateId());
        		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    		}

            updateComment(adminData, certificate);
            updateARAttributes(adminData, certificate);

            try {
	    		revokeCertificate(certificate, adminData, userName);

                asyncNotificationService.notifyRAOfficerOnUserRevocation(certificate);

	    		return new ResponseEntity<>(adminData.getCertificateId(), HttpStatus.OK);

			} catch (GeneralSecurityException e) {
	    		return ResponseEntity.badRequest().build();
			}

    	}else {
    		return ResponseEntity.notFound().build();
    	}

	}

    /**
     *
     * @param cert
     * @param adminData
     * @param revokingUser
     * @throws GeneralSecurityException
     */
	private void revokeCertificate(Certificate cert, final CertificateAdministrationData adminData, final String revokingUser) throws GeneralSecurityException {


		if (cert.isRevoked()) {
			LOG.warn("failureReason: " +
					"certificate with id '" + cert.getId() + "' already revoked.");
		}

        auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_REVOKED, cert));

        CRLReason crlReason = cryptoUtil.crlReasonFromString(adminData.getRevocationReason());

		String crlReasonStr = cryptoUtil.crlReasonAsString(crlReason);
		LOG.debug("crlReason : " + crlReasonStr + " from " + adminData.getRevocationReason());

		Date revocationDate = new Date();

		bpmnUtil.startCertificateRevocationProcess(cert, crlReason, revocationDate);

		// @todo isn't this already done in the process?
		cert.setActive(false);
		cert.setRevoked(true);
		cert.setRevokedSince(Instant.now());
		cert.setRevocationReason(crlReasonStr);

		if( adminData.getComment() != null && adminData.getComment().trim().length() > 0) {
			cert.setAdministrationComment(adminData.getComment());
		}
		certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_REVOKED_BY, revokingUser, false);

		/*
		 * @ todo
		 */
		cert.setRevocationExecutionId("39");

		certificateRepository.save(cert);

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
        return new ResponseEntity<>(nExpiringCerts, HttpStatus.OK);
    }

//    selfAdministerCertificate

    /**
     * {@code POST  /selfAdministerCertificate} : update own certificate's attributes .
     *
     * @param adminData a structure holding certificate specific data
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/selfAdministerCertificate")
    @Transactional(noRollbackFor = CAFailureException.class)
    public ResponseEntity<Long> selfAdministerCertificate(@Valid @RequestBody CertificateAdministrationData adminData) {

        LOG.debug("REST request to update certificate : {}", adminData);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();

        Optional<Certificate> optCert = certificateRepository.findById(adminData.getCertificateId());
        if( optCert.isPresent()) {

            Certificate certificate = optCert.get();

            String requestedBy = null;
            if( certificate.getCsr() != null){
                requestedBy = certificate.getCsr().getRequestedBy();
            }
            if( userName == null ||
                !userName.equals(requestedBy) ){

                LOG.debug("REST request by '{}' to update certificate '{}' rejected ", userName, adminData.getCertificateId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            updateARAttributes(adminData, certificate);
            updateComment(adminData, certificate);

            certificateRepository.save(certificate);

            return new ResponseEntity<>(adminData.getCertificateId(), HttpStatus.OK);

        }else {
            return ResponseEntity.notFound().build();
        }

    }

    private void updateComment(CertificateAdministrationData adminData, Certificate cert) {
        certUtil.setCertificateComment(cert, adminData.getComment());
    }


    private void updateARAttributes(CertificateAdministrationData adminData, Certificate cert ) {

        Set<CertificateAttribute> certificateAttributeSet = cert.getCertificateAttributes();

        for(CertificateAttribute certAttr: certificateAttributeSet){
            if(certAttr.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                for(NamedValue nv: adminData.getArAttributeArr()){
                    if( StringUtils.equals( certAttr.getName(), CsrAttribute.ARA_PREFIX + nv.getName())){
                        if( !StringUtils.equals(certAttr.getValue(),nv.getValue())) {
                            auditService.saveAuditTrace(
                                auditService.createAuditTraceCertificateAttribute(certAttr.getName(), certAttr.getValue(), nv.getValue(), cert));

                            certAttr.setValue(nv.getValue());
                            LOG.debug("certificate attribute {} updated to {}", certAttr.getName(), certAttr.getValue());
                        }
                    }
                }
            }
        }

        for(NamedValue nv: adminData.getArAttributeArr()){

            if( !certificateAttributeSet.stream().anyMatch(certAtt ->( StringUtils.equals(certAtt.getName(), CsrAttribute.ARA_PREFIX + nv.getName())))){

                auditService.saveAuditTrace(
                    auditService.createAuditTraceCertificateAttribute(nv.getName(), "", nv.getValue(), cert));

                CertificateAttribute certificateAttribute = new CertificateAttribute();
                certificateAttribute.setCertificate(cert);
                certificateAttribute.setName(CsrAttribute.ARA_PREFIX +nv.getName());
                certificateAttribute.setValue(nv.getValue());

                certificateAttributeSet.add(certificateAttribute);
            }
        }

        certificateAttributeRepository.saveAll(certificateAttributeSet);
        cert.setCertificateAttributes(certificateAttributeSet);
    }



}
