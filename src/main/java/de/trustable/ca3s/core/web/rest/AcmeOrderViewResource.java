package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.AcmeOrderService;
import de.trustable.ca3s.core.service.dto.AcmeChallengeView;
import de.trustable.ca3s.core.service.dto.AcmeOrderView;
import de.trustable.ca3s.core.service.util.AcmeOrderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link AcmeOrder}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class AcmeOrderViewResource {

    private final Logger log = LoggerFactory.getLogger(AcmeOrderViewResource.class);

    private final AcmeOrderService acmeOrderService;

    private final AcmeOrderUtil acmeOrderUtil;

    public AcmeOrderViewResource(AcmeOrderService acmeOrderService, AcmeOrderUtil acmeOrderUtil) {
        this.acmeOrderService = acmeOrderService;
        this.acmeOrderUtil = acmeOrderUtil;
    }

    /**
     * {@code GET  /acmeOrderViews} : get all ACME orders.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME accounts in body.
     */
    @GetMapping("/acmeOrderViews")
    public List<AcmeOrderView> getAllAcmeOrderViews() {
        log.debug("REST request to get all AcmeOrderViews");
        List<AcmeOrderView> avList = new ArrayList<>();
        for( AcmeOrder acmeOrder: acmeOrderService.findAll()){
            avList.add(acmeOrderUtil.from(acmeOrder));
        }
        return avList;
    }

    /**
     * {@code GET  /acmeOrderViews/:id} : get the "id" ACME order.
     *
     * @param id the id of the ACME account to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ACME account, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acmeOrderViews/{id}")
    public ResponseEntity<AcmeOrderView> getacmeOrder(@PathVariable Long id) {
        log.debug("REST request to get acmeOrderView : {}", id);
        Optional<AcmeOrder> acmeOrderOptional = acmeOrderService.findOne(id);
        Optional<AcmeOrderView> avOpt = Optional.empty();
        if( acmeOrderOptional.isPresent()){
            AcmeOrder acmeOrder = acmeOrderOptional.get();
            AcmeOrderView acmeOrderView = acmeOrderUtil.from(acmeOrder);
            avOpt = Optional.of(acmeOrderView);
        }else{
            log.info("acme order not found, order id '{}' unknown!", id);
        }
        return ResponseUtil.wrapOrNotFound(avOpt);
    }

    /**
     * {@code GET  /acmeOrderViews/:id/challenges} : get all challenges for order "id" .
     *
     * @param id the id of the ACME order to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ACME order, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acmeOrderView/{id}/challenges")
    public ResponseEntity<List<AcmeChallengeView>> getAcmeChallenges(@PathVariable Long id) {
        log.debug("REST request to get challenges of acme order : {}", id);
        Optional<AcmeOrder> acmeOrderOptional = acmeOrderService.findOne(id);
        if( acmeOrderOptional.isPresent()){
            AcmeOrder acmeOrder = acmeOrderOptional.get();
            List<AcmeChallengeView> acmeChallengeViewList = acmeOrderUtil.challengeListfrom(acmeOrder);

            final HttpHeaders additionalHeaders = new HttpHeaders();
            additionalHeaders.add("X-Total-Count", Long.toString(acmeChallengeViewList.size()));

            return ResponseEntity.ok().headers(additionalHeaders).body(acmeChallengeViewList);
        }else{
            log.info("challenges of acme order not found, order id '{}' unknown!", id);
        }
        return ResponseEntity.notFound().build();
    }

}
