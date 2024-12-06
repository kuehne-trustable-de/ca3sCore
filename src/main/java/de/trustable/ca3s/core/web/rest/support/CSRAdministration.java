package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.service.util.*;
import de.trustable.ca3s.core.web.rest.data.AdministrationType;
import de.trustable.ca3s.core.web.rest.data.CSRAdministrationData;
import de.trustable.ca3s.core.service.dto.NamedValue;
import de.trustable.ca3s.core.web.rest.data.CSRAdministrationResponse;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.validation.Valid;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.trustable.ca3s.core.service.util.PipelineUtil.ADDITIONAL_EMAIL_RECIPIENTS;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class CSRAdministration {

	private final Logger LOG = LoggerFactory.getLogger(CSRAdministration.class);


    private final CSRRepository csrRepository;

    private final CsrAttributeRepository csrAttributeRepository;

    private final CSRUtil csrUtil;

    private final CertificateProcessingUtil cpUtil;

    private final PipelineUtil pipelineUtil;

    private final UserRepository userRepository;

    private final UserUtil userUtil;

    private final AuditService auditService;

    private final NotificationService notificationService;
    private final ProtectedContentUtil protectedContentUtil;
    private final PreferenceUtil preferenceUtil;

    private final boolean selfIssuanceAllowed;



    public CSRAdministration(CSRRepository csrRepository,
                             CsrAttributeRepository csrAttributeRepository,
                             CSRUtil csrUtil,
                             CertificateProcessingUtil cpUtil,
                             PipelineUtil pipelineUtil,
                             UserRepository userRepository,
                             UserUtil userUtil,
                             AuditService auditService,
                             NotificationService notificationService,
                             ProtectedContentUtil protectedContentUtil,
                             PreferenceUtil preferenceUtil,
                             @Value("${ca3s.issuance.ra.self-issuance-allowed:false}") boolean selfIssuanceAllowed
    ) {
        this.csrRepository = csrRepository;
        this.csrAttributeRepository = csrAttributeRepository;
        this.csrUtil = csrUtil;
        this.cpUtil = cpUtil;
        this.pipelineUtil = pipelineUtil;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.protectedContentUtil = protectedContentUtil;
        this.preferenceUtil = preferenceUtil;
        this.selfIssuanceAllowed = selfIssuanceAllowed;
    }

    /**
     * {@code POST  /administerRequest} : Process or comment a CSR.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerRequest")
	@Transactional
    public ResponseEntity<CSRAdministrationResponse> administerRequest(@Valid @RequestBody CSRAdministrationData adminData) throws MessagingException {

    	LOG.debug("REST request to reject / accept CSR : {}", adminData);

        User currentUser = userUtil.getCurrentUser();
        String userName = currentUser.getLogin();

        Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
    	if( optCSR.isPresent()) {
            CSR csr = optCSR.get();

            if( !pipelineUtil.isUserValidAsRA(csr.getPipeline(), currentUser)){
                LOG.warn("REST request by user '{}' to accept CSR '{}' is not allowed by the pipeline!", userName, adminData.getCsrId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if( userName.equals(csr.getRequestedBy()) ){

                if(AdministrationType.ACCEPT.equals(adminData.getAdministrationType())) {
                    if (selfIssuanceAllowed) {
                        LOG.info("REST request by ra officer '{}' to accept CSR '{}' issued by himself accepted!", userName, adminData.getCsrId());
                    } else {
                        LOG.warn("REST request by ra officer '{}' to accept CSR '{}' issued by himself rejected!", userName, adminData.getCsrId());
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
                    }
                }
            }

			csr.setAdministeredBy(userName);
            updateComment(adminData, csr);

            if(AdministrationType.ACCEPT.equals(adminData.getAdministrationType())){
    			csr.setApprovedOn(Instant.now());
                csr.setAcceptedBy(userName);

                updateComment(adminData, csr);
                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());
                csrRepository.save(csr);

                auditService.saveAuditTrace(auditService.createAuditTraceCsrAccepted(csr));

                Certificate cert;
                try{
                    cert = cpUtil.processCertificateRequestImmediate( csr, userName, AuditService.AUDIT_RA_CERTIFICATE_CREATED );
                }catch (CAFailureException caFailureException) {
                    LOG.info("problem creating certificate", caFailureException);
                    CSRAdministrationResponse csrAdministrationResponse = new CSRAdministrationResponse();
                    csrAdministrationResponse.setAdministrationType(adminData.getAdministrationType());
                    csrAdministrationResponse.setCsrId(adminData.getCsrId());
                    csrAdministrationResponse.setProblemOccured(caFailureException.getMessage());
                    return new ResponseEntity<>(csrAdministrationResponse, HttpStatus.ACCEPTED);
                }

                Instant validTo = Instant.now().plus(preferenceUtil.getServerSideKeyDeleteAfterDays(), ChronoUnit.DAYS);
                int leftUsages = preferenceUtil.getServerSideKeyDeleteAfterUses();
                protectedContentUtil.updateServersideKeyRetentionSettings(csr.getId(),
                   validTo,
                    leftUsages);

    			if(cert != null) {

                    Set<String> additionalEmailSet = new HashSet<>();
                    if( (cert.getCsr() != null) && (cert.getCsr().getPipeline() != null)) {
                        String emails = pipelineUtil.getPipelineAttribute(cert.getCsr().getPipeline(), ADDITIONAL_EMAIL_RECIPIENTS, "");
                        NotificationService.addSplittedEMailAddress(additionalEmailSet, emails);
                    }

        			Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
        			if( optUser.isPresent()) {
        				User requestor = optUser.get();
	    		        if (requestor.getEmail() == null) {
	    		        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
	    		        }else {
                            notificationService.notifyUserCertificateIssuedAsync(requestor, cert, additionalEmailSet);
	    		        }
        			} else {
        				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
        			}
                    CSRAdministrationResponse csrAdministrationResponse = new CSRAdministrationResponse();
                    csrAdministrationResponse.setAdministrationType(adminData.getAdministrationType());
                    csrAdministrationResponse.setCertId(cert.getId());
    	    		return new ResponseEntity<>(csrAdministrationResponse, HttpStatus.CREATED);

    			} else {
    				LOG.warn("creation of certificate requested for CSR {} failed ", csr.getId());
                    CSRAdministrationResponse csrAdministrationResponse = new CSRAdministrationResponse();
                    csrAdministrationResponse.setAdministrationType(adminData.getAdministrationType());
                    csrAdministrationResponse.setCsrId(adminData.getCsrId());
    	    		return new ResponseEntity<>(csrAdministrationResponse, HttpStatus.BAD_REQUEST);
    			}

    		}else if(AdministrationType.REJECT.equals(adminData.getAdministrationType())){

    			csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, adminData.getRejectionReason());
    			csrRepository.save(csr);

    			Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
    			if( optUser.isPresent()) {
    				User requestor = optUser.get();
    		        if (requestor.getEmail() == null) {
    		        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
    		        }else {
                        notificationService.notifyUserCertificateRejectedAsync(requestor, csr );
    		        }
    			} else {
    				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
    			}

                updateComment(adminData, csr);
                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());

                auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr));

                CSRAdministrationResponse csrAdministrationResponse = new CSRAdministrationResponse();
                csrAdministrationResponse.setAdministrationType(adminData.getAdministrationType());
                csrAdministrationResponse.setCsrId(adminData.getCsrId());
        		return new ResponseEntity<>(csrAdministrationResponse, HttpStatus.OK);

            }else if(AdministrationType.UPDATE.equals(adminData.getAdministrationType())){
                updateComment(adminData, csr);
                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());

                CSRAdministrationResponse csrAdministrationResponse = new CSRAdministrationResponse();
                csrAdministrationResponse.setAdministrationType(adminData.getAdministrationType());
                csrAdministrationResponse.setCsrId(adminData.getCsrId());
                return new ResponseEntity<>(csrAdministrationResponse, HttpStatus.OK);
            } else {
                LOG.info("administration type '{}' unexpected!", adminData.getAdministrationType());
                return ResponseEntity.badRequest().build();
    		}
    	}else {
    		return ResponseEntity.notFound().build();
    	}

	}

    /**
     * {@code POST  /withdrawOwnRequest} : Withdraw own request .
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/withdrawOwnRequest")
    @Transactional
    public ResponseEntity<Long> withdrawOwnRequest(@Valid @RequestBody CSRAdministrationData adminData) {

        LOG.debug("REST request to withdraw CSR : {}", adminData);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();

        Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
        if( optCSR.isPresent()) {

            CSR csr = optCSR.get();
            if( userName == null ||
                !userName.equals(csr.getRequestedBy()) ){

                LOG.debug("REST request by '{}' to withdraw CSR '{}' rejected ", userName, adminData.getCsrId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            csr.setAdministeredBy(userName);
            updateComment(adminData, csr);

            csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, adminData.getRejectionReason());
            csrRepository.save(csr);
            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr));

            return new ResponseEntity<>(adminData.getCsrId(), HttpStatus.OK);

        }else {
            return ResponseEntity.notFound().build();
        }

    }


    /**
     * {@code POST  /selfAdministerRequest} : update own request .
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/selfAdministerRequest")
    @Transactional
    public ResponseEntity<Long> selfAdministerRequest(@Valid @RequestBody CSRAdministrationData adminData) {

        LOG.debug("REST request to update CSR : {}", adminData);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();

        Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
        if( optCSR.isPresent()) {

            CSR csr = optCSR.get();
            if( userName == null ||
                !userName.equals(csr.getRequestedBy()) ){

                LOG.debug("REST request by '{}' to update CSR '{}' rejected ", userName, adminData.getCsrId());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            csr.setAdministeredBy(userName);
            updateComment(adminData, csr);

            updateARAttributes(adminData, csr);

            csrRepository.save(csr);

            return new ResponseEntity<>(adminData.getCsrId(), HttpStatus.OK);

        }else {
            return ResponseEntity.notFound().build();
        }

    }

    private void updateComment(CSRAdministrationData adminData, CSR csr) {
        csrUtil.setCSRComment(csr, adminData.getComment());
/*
        if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
            auditService.saveAuditTrace(
                auditService.createAuditTraceCsrAttribute("Comment", csr.getAdministrationComment(), adminData.getComment(), csr));
            csr.setAdministrationComment(adminData.getComment());
        }

 */
    }


    private void updateARAttributes(CSRAdministrationData adminData, CSR csr) {

        Set<CsrAttribute> csrAttributeSet = csr.getCsrAttributes();
        for(CsrAttribute csrAttr: csrAttributeSet){
            for(NamedValue nv: adminData.getArAttributeArr()){
                if( StringUtils.equals(csrAttr.getName(), CsrAttribute.ARA_PREFIX + nv.getName())){

                    if( !StringUtils.equals(csrAttr.getValue(), nv.getValue())) {
                        auditService.saveAuditTrace(
                            auditService.createAuditTraceCsrAttribute(csrAttr.getName(), csrAttr.getValue(), nv.getValue(), csr));

                        csrAttr.setValue(nv.getValue());
                        LOG.debug("CSR attribute {} updated to {}", csrAttr.getName(), csrAttr.getValue());
                    }
                }
            }
        }

        for(NamedValue nv: adminData.getArAttributeArr()){

            if( !csrAttributeSet.stream().anyMatch(certAtt ->( StringUtils.equals(certAtt.getName(), CsrAttribute.ARA_PREFIX + nv.getName())))){

                auditService.saveAuditTrace(
                    auditService.createAuditTraceCsrAttribute(nv.getName(), "", nv.getValue(), csr));

                CsrAttribute csrAttribute = new CsrAttribute();
                csrAttribute.setCsr(csr);
                csrAttribute.setName(CsrAttribute.ARA_PREFIX + nv.getName());
                csrAttribute.setValue(nv.getValue());
                csrAttributeSet.add(csrAttribute);
            }
        }

        csrAttributeRepository.saveAll(csrAttributeSet);
        csr.setCsrAttributes(csrAttributeSet);

    }


}
