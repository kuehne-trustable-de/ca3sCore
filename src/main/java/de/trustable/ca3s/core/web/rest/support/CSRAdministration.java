package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.web.rest.data.AdministrationType;
import de.trustable.ca3s.core.web.rest.data.CSRAdministrationData;
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
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.time.Instant;
import java.util.Locale;
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
	private BPMNUtil bpmnUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MailService mailService;

	@Autowired
	private AuditService auditService;


    /**
     * {@code POST  /administerRequest} : Process a PKCSXX-object encoded as PEM.
     *
     * @param adminData a structure holding some crypto-related content, e.g. CSR, certificate, P12 container
     * @return the {@link ResponseEntity} .
     */
    @PostMapping("/administerRequest")
	@Transactional
    public ResponseEntity<Long> administerRequest(@Valid @RequestBody CSRAdministrationData adminData) {

    	LOG.debug("REST request to reject / accept CSR : {}", adminData);


    	Optional<CSR> optCSR = csrRepository.findById(adminData.getCsrId());
    	if( optCSR.isPresent()) {
            CSR csr = optCSR.get();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			csr.setAdministeredBy(auth.getName());
			if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
				csr.setAdministrationComment(adminData.getComment());
			}

    		if(AdministrationType.ACCEPT.equals(adminData.getAdministrationType())){
    			csr.setApprovedOn(Instant.now());
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

		    		        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
		    		        Context context = new Context(locale);
		    		        context.setVariable("certId", cert.getId());
		    		        context.setVariable("subject", cert.getSubject());

		    		    	String downloadFilename = CertificateUtil.getDownloadFilename(cert);

		    		    	boolean isServersideKeyGeneration = false;
		    		    	if(cert.getCsr() != null) {
		    		    		isServersideKeyGeneration = cert.getCsr().isServersideKeyGeneration();
		    		    	}
		    		        context.setVariable("isServersideKeyGeneration", isServersideKeyGeneration);

		    		        context.setVariable("filenameCrt", downloadFilename + ".crt");
		    		        context.setVariable("filenamePem", downloadFilename + ".pem");

		    		        mailService.sendEmailFromTemplate(context, requestor, "mail/acceptedRequestEmail", "email.acceptedRequest.title");
	    		        }
        			} else {
        				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
        			}
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

    			Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
    			if( optUser.isPresent()) {
    				User requestor = optUser.get();
    		        if (requestor.getEmail() == null) {
    		        	LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
    		        }else {

	    		        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
	    		        Context context = new Context(locale);
	    		        context.setVariable("csr", csr);
	    		        mailService.sendEmailFromTemplate(context, requestor, "mail/rejectedRequestEmail", "email.request.rejection.title");
    		        }
    			} else {
    				LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
    			}

                auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr));

        		return new ResponseEntity<Long>(adminData.getCsrId(), HttpStatus.OK);
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
			if( adminData.getComment() != null && !adminData.getComment().trim().isEmpty()) {
				csr.setAdministrationComment(adminData.getComment());
			}

			csr.setRejectionReason(adminData.getRejectionReason());
			csr.setRejectedOn(Instant.now());
			csr.setStatus(CsrStatus.REJECTED);
			csrRepository.save(csr);

            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr));

    		return new ResponseEntity<Long>(adminData.getCsrId(), HttpStatus.OK);

    	}else {
    		return ResponseEntity.notFound().build();
    	}

	}


}
