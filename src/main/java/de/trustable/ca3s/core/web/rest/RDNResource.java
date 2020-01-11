package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.RDN;
import de.trustable.ca3s.core.service.RDNService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.RDN}.
 */
@RestController
@RequestMapping("/api")
public class RDNResource {

    private final Logger log = LoggerFactory.getLogger(RDNResource.class);

    private static final String ENTITY_NAME = "rDN";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RDNService rDNService;

    public RDNResource(RDNService rDNService) {
        this.rDNService = rDNService;
    }

    /**
     * {@code POST  /rdns} : Create a new rDN.
     *
     * @param rDN the rDN to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rDN, or with status {@code 400 (Bad Request)} if the rDN has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/rdns")
    public ResponseEntity<RDN> createRDN(@RequestBody RDN rDN) throws URISyntaxException {
        log.debug("REST request to save RDN : {}", rDN);
        if (rDN.getId() != null) {
            throw new BadRequestAlertException("A new rDN cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RDN result = rDNService.save(rDN);
        return ResponseEntity.created(new URI("/api/rdns/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /rdns} : Updates an existing rDN.
     *
     * @param rDN the rDN to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rDN,
     * or with status {@code 400 (Bad Request)} if the rDN is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rDN couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/rdns")
    public ResponseEntity<RDN> updateRDN(@RequestBody RDN rDN) throws URISyntaxException {
        log.debug("REST request to update RDN : {}", rDN);
        if (rDN.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RDN result = rDNService.save(rDN);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rDN.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /rdns} : get all the rDNS.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rDNS in body.
     */
    @GetMapping("/rdns")
    public List<RDN> getAllRDNS() {
        log.debug("REST request to get all RDNS");
        return rDNService.findAll();
    }

    /**
     * {@code GET  /rdns/:id} : get the "id" rDN.
     *
     * @param id the id of the rDN to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rDN, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rdns/{id}")
    public ResponseEntity<RDN> getRDN(@PathVariable Long id) {
        log.debug("REST request to get RDN : {}", id);
        Optional<RDN> rDN = rDNService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rDN);
    }

    /**
     * {@code DELETE  /rdns/:id} : delete the "id" rDN.
     *
     * @param id the id of the rDN to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/rdns/{id}")
    public ResponseEntity<Void> deleteRDN(@PathVariable Long id) {
        log.debug("REST request to delete RDN : {}", id);
        rDNService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
