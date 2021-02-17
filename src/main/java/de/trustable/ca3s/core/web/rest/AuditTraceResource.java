package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.service.AuditTraceService;
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

/**
 * REST controller for managing {@link AuditTrace}.
 */
@RestController
@RequestMapping("/api")
public class AuditTraceResource {

    private final Logger log = LoggerFactory.getLogger(AuditTraceResource.class);

    private static final String ENTITY_NAME = "auditTrace";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuditTraceService auditTraceService;

    public AuditTraceResource(AuditTraceService auditTraceService) {
        this.auditTraceService = auditTraceService;
    }

    /**
     * {@code POST  /audit-traces} : Create a new auditTrace.
     *
     * @param auditTrace the auditTrace to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new auditTrace, or with status {@code 400 (Bad Request)} if the auditTrace has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/audit-traces")
    public ResponseEntity<AuditTrace> createAuditTrace(@Valid @RequestBody AuditTrace auditTrace) throws URISyntaxException {
        log.debug("REST request to save AuditTrace : {}", auditTrace);
        if (auditTrace.getId() != null) {
            throw new BadRequestAlertException("A new auditTrace cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AuditTrace result = auditTraceService.save(auditTrace);
        return ResponseEntity.created(new URI("/api/audit-traces/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /audit-traces} : Updates an existing auditTrace.
     *
     * @param auditTrace the auditTrace to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated auditTrace,
     * or with status {@code 400 (Bad Request)} if the auditTrace is not valid,
     * or with status {@code 500 (Internal Server Error)} if the auditTrace couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/audit-traces")
    public ResponseEntity<AuditTrace> updateAuditTrace(@Valid @RequestBody AuditTrace auditTrace) throws URISyntaxException {
        log.debug("REST request to update AuditTrace : {}", auditTrace);
        if (auditTrace.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AuditTrace result = auditTraceService.save(auditTrace);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, auditTrace.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /audit-traces} : get all the auditTraces.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditTraces in body.
     */
    @GetMapping("/audit-traces")
    public List<AuditTrace> getAllAuditTraces() {
        log.debug("REST request to get all AuditTraces");
        return auditTraceService.findAll();
    }

    /**
     * {@code GET  /audit-traces/:id} : get the "id" auditTrace.
     *
     * @param id the id of the auditTrace to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditTrace, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/audit-traces/{id}")
    public ResponseEntity<AuditTrace> getAuditTrace(@PathVariable Long id) {
        log.debug("REST request to get AuditTrace : {}", id);
        Optional<AuditTrace> auditTrace = auditTraceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(auditTrace);
    }

    /**
     * {@code DELETE  /audit-traces/:id} : delete the "id" auditTrace.
     *
     * @param id the id of the auditTrace to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/audit-traces/{id}")
    public ResponseEntity<Void> deleteAuditTrace(@PathVariable Long id) {
        log.debug("REST request to delete AuditTrace : {}", id);
        auditTraceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
