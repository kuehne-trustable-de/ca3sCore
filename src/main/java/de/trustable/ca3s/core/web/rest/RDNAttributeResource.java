package de.trustable.ca3s.core.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.RDNAttribute;
import de.trustable.ca3s.core.service.RDNAttributeService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.RDNAttribute}.
 */
@RestController
@RequestMapping("/api")
public class RDNAttributeResource {

    private final Logger log = LoggerFactory.getLogger(RDNAttributeResource.class);

    private static final String ENTITY_NAME = "rDNAttribute";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RDNAttributeService rDNAttributeService;

    public RDNAttributeResource(RDNAttributeService rDNAttributeService) {
        this.rDNAttributeService = rDNAttributeService;
    }

    /**
     * {@code POST  /rdn-attributes} : Create a new rDNAttribute.
     *
     * @param rDNAttribute the rDNAttribute to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new rDNAttribute, or with status {@code 400 (Bad Request)} if the rDNAttribute has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/rdn-attributes")
    public ResponseEntity<RDNAttribute> createRDNAttribute(@Valid @RequestBody RDNAttribute rDNAttribute) throws URISyntaxException {
        log.debug("REST request to save RDNAttribute : {}", rDNAttribute);
        if (rDNAttribute.getId() != null) {
            throw new BadRequestAlertException("A new rDNAttribute cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RDNAttribute result = rDNAttributeService.save(rDNAttribute);
        return ResponseEntity.created(new URI("/api/rdn-attributes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /rdn-attributes} : Updates an existing rDNAttribute.
     *
     * @param rDNAttribute the rDNAttribute to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated rDNAttribute,
     * or with status {@code 400 (Bad Request)} if the rDNAttribute is not valid,
     * or with status {@code 500 (Internal Server Error)} if the rDNAttribute couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/rdn-attributes")
    public ResponseEntity<RDNAttribute> updateRDNAttribute(@Valid @RequestBody RDNAttribute rDNAttribute) throws URISyntaxException {
        log.debug("REST request to update RDNAttribute : {}", rDNAttribute);
        if (rDNAttribute.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RDNAttribute result = rDNAttributeService.save(rDNAttribute);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, rDNAttribute.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /rdn-attributes} : get all the rDNAttributes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of rDNAttributes in body.
     */
    @GetMapping("/rdn-attributes")
    public List<RDNAttribute> getAllRDNAttributes() {
        log.debug("REST request to get all RDNAttributes");
        return rDNAttributeService.findAll();
    }

    /**
     * {@code GET  /rdn-attributes/:id} : get the "id" rDNAttribute.
     *
     * @param id the id of the rDNAttribute to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the rDNAttribute, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/rdn-attributes/{id}")
    public ResponseEntity<RDNAttribute> getRDNAttribute(@PathVariable Long id) {
        log.debug("REST request to get RDNAttribute : {}", id);
        Optional<RDNAttribute> rDNAttribute = rDNAttributeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(rDNAttribute);
    }

    /**
     * {@code DELETE  /rdn-attributes/:id} : delete the "id" rDNAttribute.
     *
     * @param id the id of the rDNAttribute to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/rdn-attributes/{id}")
    public ResponseEntity<Void> deleteRDNAttribute(@PathVariable Long id) {
        log.debug("REST request to delete RDNAttribute : {}", id);
        rDNAttributeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
