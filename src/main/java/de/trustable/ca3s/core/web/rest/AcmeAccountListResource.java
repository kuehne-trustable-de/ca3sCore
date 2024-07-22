package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.repository.AcmeAccountViewRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.dto.AcmeAccountView;
import de.trustable.ca3s.core.service.dto.CSRView;
import org.springframework.security.access.prepost.PreAuthorize;
import tech.jhipster.web.util.PaginationUtil;
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
public class AcmeAccountListResource {

    private final AcmeAccountViewRepository acmeAccountViewRepository;


    private final Logger log = LoggerFactory.getLogger(AcmeAccountListResource.class);

    public AcmeAccountListResource(AcmeAccountViewRepository acmeAccountViewRepository) {
        this.acmeAccountViewRepository = acmeAccountViewRepository;
    }


    /**
     * {@code GET  /acmeAccountList} : get all the ACME accounts.
     *
     * @param pageable the pagination information.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME account in body.
     */
    @GetMapping("/acmeAccountList")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<AcmeAccountView>> getAllAcmeAccounts(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of AcmeAccountViews");
        Page<AcmeAccountView> page = acmeAccountViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
