package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.repository.AuditTraceViewRepository;
import de.trustable.ca3s.core.service.dto.AuditTraceView;
import de.trustable.ca3s.core.service.dto.CSRView;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST controller for managing {@link CSRView}.
 */
@RestController
@RequestMapping("/api")
public class AuditTraceListResource {

    private final Logger log = LoggerFactory.getLogger(AuditTraceListResource.class);

    final AuditTraceViewRepository auditTraceViewRepository;

    public AuditTraceListResource(AuditTraceViewRepository auditTraceViewRepository) {
        this.auditTraceViewRepository = auditTraceViewRepository;
    }


    /**
     * {@code GET  /auditTraceList} : get all audit traces.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of audit traces in body.
     */
    @GetMapping("/auditTraceList")
    public ResponseEntity<List<AuditTraceView>> getAllCertificates(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of AuditTraceView");
        Page<AuditTraceView> page = auditTraceViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}
