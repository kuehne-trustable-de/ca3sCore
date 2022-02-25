package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.CSRService;
import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CSR}.
 */
@RestController
@RequestMapping("/api")
public class CSRResource {

    private final Logger log = LoggerFactory.getLogger(CSRResource.class);

    private static final String ENTITY_NAME = "cSR";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CSRService cSRService;
    private final CSRUtil csrUtil;
    private final PipelineUtil pipelineUtil;
    private final UserRepository userRepository;

    public CSRResource(CSRService cSRService, CSRUtil csrUtil, PipelineUtil pipelineUtil, UserRepository userRepository) {
        this.cSRService = cSRService;
        this.csrUtil = csrUtil;
        this.pipelineUtil = pipelineUtil;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /csrs} : Create a new cSR.
     *
     * @param cSR the cSR to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cSR, or with status {@code 400 (Bad Request)} if the cSR has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/csrs")
    public ResponseEntity<CSR> createCSR(@Valid @RequestBody CSR cSR) throws URISyntaxException {

        return (ResponseEntity<CSR>) ResponseEntity.badRequest();
/*
        log.debug("REST request to save CSR : {}", cSR);
        if (cSR.getId() != null) {
            throw new BadRequestAlertException("A new cSR cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CSR result = cSRService.save(cSR);
        return ResponseEntity.created(new URI("/api/csrs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
 */
    }

    /**
     * {@code PUT  /csrs} : Updates an existing cSR.
     *
     * @param cSR the cSR to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cSR,
     * or with status {@code 400 (Bad Request)} if the cSR is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cSR couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/csrs")
    public ResponseEntity<CSR> updateCSR(@Valid @RequestBody CSR cSR) throws URISyntaxException {
        log.debug("REST request to update CSR : {}", cSR);
        return (ResponseEntity<CSR>) ResponseEntity.badRequest();
/*
        if (cSR.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CSR result = cSRService.save(cSR);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cSR.getId().toString()))
            .body(result);
 */
    }

    /**
     * {@code GET  /csrs} : get all the cSRS.
     *
     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cSRS in body.
     */
    @GetMapping("/csrs")
    public List<CSR> getAllCSRS(@RequestParam(required = false) String filter) {

        return (List<CSR>) ResponseEntity.badRequest();
/*
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();

        List<CSR> csrList;
        if ("certificate-is-null".equals(filter)) {
            log.debug("REST request to get all CSRs where certificate is null");
            csrList = cSRService.findAllWhereCertificateIsNull();
        }else {
            log.debug("REST request to get all CSRS");
            csrList = cSRService.findAll();
        }

//        List<CSR> cleanList = new ArrayList<>();
        for(CSR csr: csrList){
            if( !csr.getRequestedBy().equals(userName)){
                csr.setCsrBase64("");
            }
        }
        return csrList;
 */
    }

    /**
     * {@code GET  /csr/:id} : get the "id" cSR.
     *
     * @param id the id of the cSR to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cSR, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/csrView/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<CSRView> getCSRView(@PathVariable Long id) {
        log.debug("REST request to get CSRView for CSR id : {}", id);

        Optional<CSR> cSROptional = cSRService.findOne(id);

        if(cSROptional.isPresent()){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();

            CSR csr = cSROptional.get();
            CSRView csrView = new CSRView(csrUtil, csr);

            Optional<User> optCurrentUser = userRepository.findOneByLogin(userName);
            if(!optCurrentUser.isPresent()) {
                log.warn("Name of ra officer '{}' not found as user", userName);
                return ResponseEntity.notFound().build();
            }

            csrView.setAdministrable(pipelineUtil.isUserValidAsRA(csr.getPipeline(), optCurrentUser.get()));

            if( csr.getRequestedBy().equals(userName) || csrView.getIsAdministrable()){
                csrView.setCsrBase64(csr.getCsrBase64());
            }

            log.debug("returning CSRView for id : {} -> {}", id, csrView);
            return ResponseEntity.ok(csrView);
        }
        return ResponseEntity.notFound().build();
    }

   /**
     * {@code GET  /csrs/:id} : get the "id" cSR.
     *
     * @param id the id of the cSR to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cSR, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/csrs/{id}")
    public ResponseEntity<CSR> getCSR(@PathVariable Long id) {
        log.debug("REST request to get CSR : {}", id);
        return (ResponseEntity<CSR>) ResponseEntity.badRequest();

/*
        Optional<CSR> cSROptional = cSRService.findOne(id);

        if(cSROptional.isPresent()){
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userName = auth.getName();

            CSR csr = cSROptional.get();
            if( !csr.getRequestedBy().equals(userName)){
                csr.setCsrBase64("");
            }
        }
        return ResponseUtil.wrapOrNotFound(cSROptional);

 */
    }

    /**
     * {@code DELETE  /csrs/:id} : delete the "id" cSR.
     *
     * @param id the id of the cSR to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/csrs/{id}")
    public ResponseEntity<Void> deleteCSR(@PathVariable Long id) {
        log.debug("REST request to delete CSR : {}", id);
        return (ResponseEntity<Void>) ResponseEntity.badRequest();
/*
        cSRService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();

 */
    }
}
