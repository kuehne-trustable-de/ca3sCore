package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AuditTraceService;

import org.springframework.security.access.prepost.PreAuthorize;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
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
     * {@code GET  /audit-traces} : get all the auditTraces.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditTraces in body.
     */
    @GetMapping(value = "/audit-traces")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Page<AuditTrace> getAllAuditTraces(
        Pageable pageable,
        @RequestParam(value = "certificate", required = false) Long certificateId,
        @RequestParam(value = "csr", required = false) Long csrId,
        @RequestParam(value = "pipeline", required = false) Long pipelineId,
        @RequestParam(value = "caConnector", required = false) Long caConnectorId,
        @RequestParam(value = "processInfo", required = false) Long processInfoId,
        @RequestParam(value = "acmeOrder", required = false) Long acmeOrderId,
        @RequestParam(value = "scepOrder", required = false) Long scepOrderId
        ) {
        log.debug("REST request to get AuditTraces");
        return auditTraceService.findBy( pageable, certificateId, csrId, pipelineId, caConnectorId, processInfoId, acmeOrderId, scepOrderId);
    }

    /**
     * {@code GET  /audit-traces/:id} : get the "id" auditTrace.
     *
     * @param id the id of the auditTrace to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the auditTrace, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/audit-traces/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
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
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAuditTrace(@PathVariable Long id) {
        log.debug("REST request to delete AuditTrace : {}", id);
        auditTraceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
