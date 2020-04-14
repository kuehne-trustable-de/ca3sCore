package de.trustable.ca3s.core.web.rest.support;

import java.time.Instant;
import java.util.Optional;

import javax.validation.Valid;

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

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.AuditUtil;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.web.rest.data.AdministrationType;
import de.trustable.ca3s.core.web.rest.data.CSRAdministrationData;

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
    private CertificateRepository certificateRepository;

	@Autowired
	private BPMNUtil bpmnUtil;

	@Autowired
	private CSRUtil csrUtil;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	
    /**
     * {@code POST  /administerRequest} : Process a PKCSXX-object encoded as PEM.
     *
     * @param a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerRequest")
	@Transactional
    public ResponseEntity<Long> administerRequest(@Valid @RequestBody CSRAdministrationData adminData) {
    	
    	LOG.debug("REST request to reject / accept CSR : {}", adminData);
        
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String raOfficerName = auth.getName();

    	Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
    	if( optCSR.isPresent()) {
    		
    		CSR csr = optCSR.get();
			csr.setAdministeredBy(raOfficerName);
			if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
				csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_ADMINISTRATION_COMMENT, adminData.getComment(), true);
			}
    		
    		if(AdministrationType.ACCEPT.equals(adminData.getAdministrationType())){
    			csr.setApprovedOn(Instant.now());
    			
    			Certificate cert = bpmnUtil.startCertificateCreationProcess(csr);
    			if(cert != null) {
    				certificateRepository.save(cert);
        			
    				csr.setCertificate(cert);
    				csr.setStatus(CsrStatus.ISSUED);
        			csrRepository.save(csr);
        			
        			applicationEventPublisher.publishEvent(
        			        new AuditApplicationEvent(
        			        		raOfficerName, AuditUtil.AUDIT_CERTIFICATE_ACCEPTED, "csr " + csr.getId() + " accepted by RA Officer"));
        			
    	    		return new ResponseEntity<Long>(cert.getId(), HttpStatus.CREATED);

    			} else {
    				LOG.warn("creation of certificate requested for CSR {} failed ", csr.getId());
    	    		return new ResponseEntity<Long>(adminData.getCsrId(), HttpStatus.BAD_REQUEST);
    			}

    		}else {
    			csr.setRejectionReason(adminData.getRejectionReason());
    			csr.setRejectedOn(Instant.now());
    			csr.setStatus(CsrStatus.REJECTED);
    			csrRepository.save(csr);
    			
    			
    			applicationEventPublisher.publishEvent(
    			        new AuditApplicationEvent(
    			        		raOfficerName, AuditUtil.AUDIT_CERTIFICATE_REJECTED, "csr " + csr.getId() + " rejected by RA Officer"));
    			
        		return new ResponseEntity<Long>(adminData.getCsrId(), HttpStatus.OK);
    		}
    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}


    /**
     * {@code POST  /withdrawOwnRequest} : Withdraw own request .
     *
     * @param a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
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
			if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
				csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_ADMINISTRATION_COMMENT, adminData.getComment(), true);
			}
    		
			csr.setRejectionReason(adminData.getRejectionReason());
			csr.setRejectedOn(Instant.now());
			csr.setStatus(CsrStatus.REJECTED);
			csrRepository.save(csr);
			
			applicationEventPublisher.publishEvent(
			        new AuditApplicationEvent(
			        		userName, AuditUtil.AUDIT_CERTIFICATE_REJECTED, "csr " + csr.getId() + " withdrawn by user"));
			
    		return new ResponseEntity<Long>(adminData.getCsrId(), HttpStatus.OK);
    		
    	}else {
    		return ResponseEntity.notFound().build();
    	}
    	
	}


}
