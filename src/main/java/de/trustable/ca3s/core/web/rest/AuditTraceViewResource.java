package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.AuditTraceService;
import de.trustable.ca3s.core.service.dto.AuditTraceView;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
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
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        @RequestParam(value = "certificate", required = false) Long certificateId,
        @RequestParam(value = "csr", required = false) Long csrId,
        @RequestParam(value = "pipeline", required = false) Long pipelineId,
        @RequestParam(value = "caConnector", required = false) Long caConnectorId,
        @RequestParam(value = "processInfo", required = false) Long processInfoId
        ) {
        log.debug("REST request to get AuditTraceViews");

        Page<AuditTrace> page = auditTraceService.findBy( pageable,certificateId, csrId, pipelineId, caConnectorId, processInfoId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<AuditTraceView> alvList = new ArrayList<>(page.getContent().size());
        for(AuditTrace at: page.getContent()){
            alvList.add(new AuditTraceView(at));
        }

        return ResponseEntity.ok().headers(headers).body(new PageImpl(alvList));

    }


}
