package de.trustable.ca3s.core.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.service.CsrAttributeService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CsrAttribute}.
 */
@RestController
@RequestMapping("/api")
public class CsrAttributeResource {

    private final Logger log = LoggerFactory.getLogger(CsrAttributeResource.class);

    private static final String ENTITY_NAME = "csrAttribute";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CsrAttributeService csrAttributeService;

    public CsrAttributeResource(CsrAttributeService csrAttributeService) {
        this.csrAttributeService = csrAttributeService;
    }

    /**
     * {@code POST  /csr-attributes} : Create a new csrAttribute.
     *
     * @param csrAttribute the csrAttribute to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new csrAttribute, or with status {@code 400 (Bad Request)} if the csrAttribute has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/csr-attributes")
    public ResponseEntity<CsrAttribute> createCsrAttribute(@Valid @RequestBody CsrAttribute csrAttribute) throws URISyntaxException {
        log.debug("REST request to save CsrAttribute : {}", csrAttribute);
        if (csrAttribute.getId() != null) {
            throw new BadRequestAlertException("A new csrAttribute cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CsrAttribute result = csrAttributeService.save(csrAttribute);
        return ResponseEntity.created(new URI("/api/csr-attributes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /csr-attributes} : Updates an existing csrAttribute.
     *
     * @param csrAttribute the csrAttribute to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated csrAttribute,
     * or with status {@code 400 (Bad Request)} if the csrAttribute is not valid,
     * or with status {@code 500 (Internal Server Error)} if the csrAttribute couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/csr-attributes")
    public ResponseEntity<CsrAttribute> updateCsrAttribute(@Valid @RequestBody CsrAttribute csrAttribute) throws URISyntaxException {
        log.debug("REST request to update CsrAttribute : {}", csrAttribute);
        if (csrAttribute.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CsrAttribute result = csrAttributeService.save(csrAttribute);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, csrAttribute.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /csr-attributes} : get all the csrAttributes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of csrAttributes in body.
     */
    @GetMapping("/csr-attributes")
    public List<CsrAttribute> getAllCsrAttributes() {
        log.debug("REST request to get all CsrAttributes");
        return csrAttributeService.findAll();
    }

    /**
     * {@code GET  /csr-attributes/:id} : get the "id" csrAttribute.
     *
     * @param id the id of the csrAttribute to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the csrAttribute, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/csr-attributes/{id}")
    public ResponseEntity<CsrAttribute> getCsrAttribute(@PathVariable Long id) {
        log.debug("REST request to get CsrAttribute : {}", id);
        Optional<CsrAttribute> csrAttribute = csrAttributeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(csrAttribute);
    }

    /**
     * {@code DELETE  /csr-attributes/:id} : delete the "id" csrAttribute.
     *
     * @param id the id of the csrAttribute to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/csr-attributes/{id}")
    public ResponseEntity<Void> deleteCsrAttribute(@PathVariable Long id) {
        log.debug("REST request to delete CsrAttribute : {}", id);
        csrAttributeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
