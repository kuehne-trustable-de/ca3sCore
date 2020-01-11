package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.service.CSRService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

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

    public CSRResource(CSRService cSRService) {
        this.cSRService = cSRService;
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
        log.debug("REST request to save CSR : {}", cSR);
        if (cSR.getId() != null) {
            throw new BadRequestAlertException("A new cSR cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CSR result = cSRService.save(cSR);
        return ResponseEntity.created(new URI("/api/csrs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
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
        if (cSR.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CSR result = cSRService.save(cSR);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cSR.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /csrs} : get all the cSRS.
     *

     * @param filter the filter of the request.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cSRS in body.
     */
    @GetMapping("/csrs")
    public List<CSR> getAllCSRS(@RequestParam(required = false) String filter) {
        if ("certificate-is-null".equals(filter)) {
            log.debug("REST request to get all CSRs where certificate is null");
            return cSRService.findAllWhereCertificateIsNull();
        }
        log.debug("REST request to get all CSRS");
        return cSRService.findAll();
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
        Optional<CSR> cSR = cSRService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cSR);
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
        cSRService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
