package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.repository.ACMEOrderViewRepository;
import de.trustable.ca3s.core.service.dto.ACMEOrderView;
import de.trustable.ca3s.core.service.dto.CSRView;
import tech.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * REST controller for managing {@link CSRView}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class ACMEOrderListResource {

    private final ACMEOrderViewRepository acmeOrderViewRepository;


    private final Logger log = LoggerFactory.getLogger(ACMEOrderListResource.class);

    public ACMEOrderListResource(ACMEOrderViewRepository acmeOrderViewRepository) {
        this.acmeOrderViewRepository = acmeOrderViewRepository;
    }


    /**
     * {@code GET  /acmeAccountList} : get all the ACME orders.
     *
     * @param pageable the pagination information.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME orders in body.
     */
    @GetMapping("/acmeOrderList")
    public ResponseEntity<List<ACMEOrderView>> getAllACMEOrders(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of ACMEAccountViews");
        Page<ACMEOrderView> page = acmeOrderViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
