package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.ScepOrder;
import de.trustable.ca3s.core.service.ScepOrderService;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import de.trustable.ca3s.core.service.util.ScepOrderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.jhipster.web.util.ResponseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link ScepOrder}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class ScepOrderViewResource {

    private final Logger log = LoggerFactory.getLogger(ScepOrderViewResource.class);

    private final ScepOrderService scepOrderService;

    private final ScepOrderUtil scepOrderUtil;

    public ScepOrderViewResource(ScepOrderService scepOrderService, ScepOrderUtil scepOrderUtil) {
        this.scepOrderService = scepOrderService;
        this.scepOrderUtil = scepOrderUtil;
    }


    /**
     * {@code GET  /scepOrderViews} : get all ACME orders.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME accounts in body.
     */
    @GetMapping("/scepOrderViews")
    public List<ScepOrderView> getAllAcmeOrderViews() {
        log.debug("REST request to get all AcmeOrderViews");
        List<ScepOrderView> avList = new ArrayList<>();
        for( ScepOrder scepOrder: scepOrderService.findAll()){
            avList.add(scepOrderUtil.from(scepOrder));
        }
        return avList;
    }

    /**
     * {@code GET  /scepOrderViews/:id} : get the "id" ACME order.
     *
     * @param id the id of the ACME account to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ACME account, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/scepOrderViews/{id}")
    public ResponseEntity<ScepOrderView> getscepOrder(@PathVariable Long id) {
        log.debug("REST request to get scepOrderView : {}", id);
        Optional<ScepOrder> scepOrderOptional = scepOrderService.findOne(id);
        Optional<ScepOrderView> avOpt = Optional.empty();
        if( scepOrderOptional.isPresent()){
            ScepOrder scepOrder = scepOrderOptional.get();
            ScepOrderView scepOrderView = scepOrderUtil.from(scepOrder);
            avOpt = Optional.of(scepOrderView);
        }else{
            log.info("acme order not found, order id '{}' unknown!", id);
        }
        return ResponseUtil.wrapOrNotFound(avOpt);
    }

}
