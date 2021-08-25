package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.web.rest.data.AdministrationType;
import de.trustable.ca3s.core.web.rest.data.CSRAdministrationData;
import de.trustable.ca3s.core.service.dto.NamedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class CSRAdministration {

	private final Logger LOG = LoggerFactory.getLogger(CSRAdministration.class);

    @Autowired
    private CSRRepository csrRepository;

    @Autowired
    private CsrAttributeRepository csrAttributeRepository;

    @Autowired
	private BPMNUtil bpmnUtil;

    @Autowired
    private CSRUtil csrUtil;

    @Autowired
	private UserRepository userRepository;

	@Autowired
	private AuditService auditService;

    @Autowired
    private NotificationService notificationService;

    /**
     * {@code POST  /administerRequest} : Process a PKCSXX-object encoded as PEM.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerRequest")
	@Transactional
    public ResponseEntity<Long> administerRequest(@Valid @RequestBody CSRAdministrationData adminData) throws MessagingException {

    	LOG.debug("REST request to reject / accept CSR : {}", adminData);


    	Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
    	if( optCSR.isPresent()) {
            CSR csr = optCSR.get();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			csr.setAdministeredBy(auth.getName());
            updateComment(adminData, csr);

            if(AdministrationType.ACCEPT.equals(adminData.getAdministrationType())){
    			csr.setApprovedOn(Instant.now());
                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());
                csrRepository.save(csr);

                auditService.saveAuditTrace(auditService.createAuditTraceCsrAccepted(csr));

    			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
    			if(cert != null) {

        			Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
        			if( optUser.isPresent()) {
        				User requestor = optUser.get();
	    		        if (requestor.getEmail() == null) {
	    		        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
	    		        }else {
                            notificationService.notifyUserCerificateIssuedAsync(requestor, cert);
	    		        }
        			} else {
        				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
        			}
    	    		return new ResponseEntity<>(cert.getId(), HttpStatus.CREATED);

    			} else {
    				LOG.warn("creation of certificate requested for CSR {} failed ", csr.getId());
    	    		return new ResponseEntity<>(adminData.getCsrId(), HttpStatus.BAD_REQUEST);
    			}

    		}else if(AdministrationType.REJECT.equals(adminData.getAdministrationType())){

    			csr.setRejectionReason(adminData.getRejectionReason());
    			csr.setRejectedOn(Instant.now());
    			csr.setStatus(CsrStatus.REJECTED);
    			csrRepository.save(csr);

    			Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
    			if( optUser.isPresent()) {
    				User requestor = optUser.get();
    		        if (requestor.getEmail() == null) {
    		        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
    		        }else {
                        notificationService.notifyUserCerificateRejectedAsync(requestor, csr );
    		        }
    			} else {
    				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
    			}

                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());

                auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr));

        		return new ResponseEntity<>(adminData.getCsrId(), HttpStatus.OK);

            }else if(AdministrationType.UPDATE.equals(adminData.getAdministrationType())){
                updateARAttributes(adminData, csr);
                csrUtil.setCSRComment(csr, adminData.getComment());

                return new ResponseEntity<>(adminData.getCsrId(), HttpStatus.OK);
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

            csr.setRejectionReason(adminData.getRejectionReason());
            csr.setRejectedOn(Instant.now());
            csr.setStatus(CsrStatus.REJECTED);
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
        if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
            auditService.saveAuditTrace(
                auditService.createAuditTraceCsrAttribute("Comment", csr.getAdministrationComment(), adminData.getComment(), csr));
            csr.setAdministrationComment(adminData.getComment());
        }
    }


    private void updateARAttributes(CSRAdministrationData adminData, CSR csr) {

        for(CsrAttribute csrAttr: csr.getCsrAttributes()){
            if(csrAttr.getName().startsWith(CsrAttribute.ARA_PREFIX) ){
                for(NamedValue nv: adminData.getArAttributeArr()){
                    if( csrAttr.getName().equals(CsrAttribute.ARA_PREFIX + nv.getName())){
                        if( !csrAttr.getValue().equals(nv.getValue())) {
                            auditService.saveAuditTrace(
                                auditService.createAuditTraceCsrAttribute(csrAttr.getName(), csrAttr.getValue(), nv.getValue(), csr));

                            csrAttr.setValue(nv.getValue());
                            LOG.debug("CSR attribute {} updated to {}", csrAttr.getName(), csrAttr.getValue());
                        }
                    }
                }
            }
        }
        csrAttributeRepository.saveAll(csr.getCsrAttributes());
    }


}
