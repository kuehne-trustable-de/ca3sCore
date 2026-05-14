package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.CRLStatusService;
import de.trustable.ca3s.core.service.dto.CrlStatusSet;
import de.trustable.ca3s.core.service.dto.CrlUrlStatusView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing {@link CRLExpirationNotification}.
 */
@RestController
@RequestMapping("/api")
public class CRLUrlStatusViewsResource {

    private final Logger log = LoggerFactory.getLogger(CRLUrlStatusViewsResource.class);


    private final CRLStatusService crlStatusService;


    public CRLUrlStatusViewsResource(CRLStatusService crlStatusService) {
        this.crlStatusService = crlStatusService;
    }

    /**
     * {@code GET  /certificates} : get all the certificates.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/crl-url-status-views")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<CrlUrlStatusView>> getAllCrlUrlStatusView(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CRLUrlStatusView");

        CrlStatusSet crlStatusSet = crlStatusService.getCrlStatusSet();

        List<CrlUrlStatusView> pageContent = new ArrayList<>();
        for( long i = pageable.getOffset(); pageContent.size() < pageable.getPageSize(); i++ ){
            if(i >= crlStatusSet.getCrlUrlStatusList().size() ){
                break;
            }
            pageContent.add( new CrlUrlStatusView(crlStatusSet.getCrlUrlStatusList().get((int) i)));
        }


        Page<CrlUrlStatusView> page = new PageImpl<CrlUrlStatusView>(pageContent, pageable, crlStatusSet.getCrlUrlStatusList().size());

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        return ResponseEntity.ok().headers(headers).body(pageContent);
    }

}
