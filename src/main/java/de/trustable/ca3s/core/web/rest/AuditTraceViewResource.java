package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.service.AuditTraceService;
import de.trustable.ca3s.core.service.dto.AuditTraceView;
import tech.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing {@link AuditTrace}.
 */
@RestController
@RequestMapping("/api")
public class AuditTraceViewResource {

    private final Logger log = LoggerFactory.getLogger(AuditTraceViewResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuditTraceService auditTraceService;

    public AuditTraceViewResource(AuditTraceService auditTraceService) {
        this.auditTraceService = auditTraceService;
    }


    /**
     * {@code GET  /audit-trace-views} : get all the auditTraces.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of auditTraces in body.
     */
    @GetMapping(value = "/audit-trace-views")
    public ResponseEntity<Page<AuditTraceView>> getAllAuditTraces(
        Pageable pageable, HttpServletRequest request,
        @RequestParam(value = "certificateId", required = false) Long certificateId,
        @RequestParam(value = "csrId", required = false) Long csrId,
        @RequestParam(value = "pipelineId", required = false) Long pipelineId,
        @RequestParam(value = "caConnectorId", required = false) Long caConnectorId,
        @RequestParam(value = "processInfoId", required = false) Long processInfoId,
        @RequestParam(value = "acmeOrderId", required = false) Long acmeOrderId,
        @RequestParam(value = "scepOrderId", required = false) Long scepOrderId
        ) {
        log.debug("REST request to get AuditTraceViews");

        if( certificateId == null){
            certificateId = Long.MIN_VALUE;
        }
        if( csrId == null){
            csrId = Long.MIN_VALUE;
        }
        if( pipelineId == null){
            pipelineId = Long.MIN_VALUE;
        }
        if( caConnectorId == null){
            caConnectorId = Long.MIN_VALUE;
        }
        if( processInfoId == null){
            processInfoId = Long.MIN_VALUE;
        }

        Page<AuditTrace> page = auditTraceService.findBy( pageable,
            certificateId,
            csrId,
            pipelineId,
            caConnectorId,
            processInfoId,
            acmeOrderId,
            scepOrderId);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AuditTraceView> alvList = new ArrayList<>(page.getContent().size());
        for(AuditTrace at: page.getContent()){
            alvList.add(new AuditTraceView(at));
        }

        return ResponseEntity.ok().headers(headers).body(new PageImpl(alvList));

    }


}
