package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.AcmeOrderService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeOrder}.
 */
@RestController
@RequestMapping("/api")
public class AcmeOrderResource {

    private final Logger log = LoggerFactory.getLogger(AcmeOrderResource.class);

    private static final String ENTITY_NAME = "acmeOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeOrderService acmeOrderService;

    public AcmeOrderResource(AcmeOrderService acmeOrderService) {
        this.acmeOrderService = acmeOrderService;
    }

    /**
     * {@code POST  /acme-orders} : Create a new acmeOrder.
     *
     * @param acmeOrder the acmeOrder to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeOrder, or with status {@code 400 (Bad Request)} if the acmeOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-orders")
    public ResponseEntity<AcmeOrder> createAcmeOrder(@Valid @RequestBody AcmeOrder acmeOrder) throws URISyntaxException {
        log.debug("REST request to save AcmeOrder : {}", acmeOrder);
        if (acmeOrder.getId() != null) {
            throw new BadRequestAlertException("A new acmeOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeOrder result = acmeOrderService.save(acmeOrder);
        return ResponseEntity.created(new URI("/api/acme-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-orders} : Updates an existing acmeOrder.
     *
     * @param acmeOrder the acmeOrder to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeOrder,
     * or with status {@code 400 (Bad Request)} if the acmeOrder is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeOrder couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-orders")
    public ResponseEntity<AcmeOrder> updateAcmeOrder(@Valid @RequestBody AcmeOrder acmeOrder) throws URISyntaxException {
        log.debug("REST request to update AcmeOrder : {}", acmeOrder);
        if (acmeOrder.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeOrder result = acmeOrderService.save(acmeOrder);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeOrder.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-orders} : get all the acmeOrders.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeOrders in body.
     */
    @GetMapping("/acme-orders")
    public List<AcmeOrder> getAllAcmeOrders() {
        log.debug("REST request to get all AcmeOrders");
        return acmeOrderService.findAll();
    }

    /**
     * {@code GET  /acme-orders/:id} : get the "id" acmeOrder.
     *
     * @param id the id of the acmeOrder to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeOrder, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-orders/{id}")
    public ResponseEntity<AcmeOrder> getAcmeOrder(@PathVariable Long id) {
        log.debug("REST request to get AcmeOrder : {}", id);
        Optional<AcmeOrder> acmeOrder = acmeOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeOrder);
    }

    /**
     * {@code DELETE  /acme-orders/:id} : delete the "id" acmeOrder.
     *
     * @param id the id of the acmeOrder to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-orders/{id}")
    public ResponseEntity<Void> deleteAcmeOrder(@PathVariable Long id) {
        log.debug("REST request to delete AcmeOrder : {}", id);
        acmeOrderService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
