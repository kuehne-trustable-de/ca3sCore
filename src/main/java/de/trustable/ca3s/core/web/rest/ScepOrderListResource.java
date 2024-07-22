package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.repository.ScepOrderViewRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST controller for managing {@link ScepOrderView}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class ScepOrderListResource {

    private final ScepOrderViewRepository scepOrderViewRepository;


    private final Logger log = LoggerFactory.getLogger(ScepOrderListResource.class);

    public ScepOrderListResource(ScepOrderViewRepository scepOrderViewRepository) {
        this.scepOrderViewRepository = scepOrderViewRepository;
    }


    /**
     * {@code GET  /ScepOrderList} : get all SCEP orders.
     *
     * @param pageable the pagination information.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of SCEP orders in body.
     */
    @GetMapping("/scepOrderList")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<ScepOrderView>> getScepOrderViews(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of ScepOrderViews");
        Page<ScepOrderView> page = scepOrderViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
