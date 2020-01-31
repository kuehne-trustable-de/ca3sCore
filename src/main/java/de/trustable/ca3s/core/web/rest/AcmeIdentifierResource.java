package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeIdentifier;
import de.trustable.ca3s.core.service.AcmeIdentifierService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeIdentifier}.
 */
@RestController
@RequestMapping("/api")
public class AcmeIdentifierResource {

    private final Logger log = LoggerFactory.getLogger(AcmeIdentifierResource.class);

    private static final String ENTITY_NAME = "acmeIdentifier";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeIdentifierService acmeIdentifierService;

    public AcmeIdentifierResource(AcmeIdentifierService acmeIdentifierService) {
        this.acmeIdentifierService = acmeIdentifierService;
    }

    /**
     * {@code POST  /acme-identifiers} : Create a new acmeIdentifier.
     *
     * @param acmeIdentifier the acmeIdentifier to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeIdentifier, or with status {@code 400 (Bad Request)} if the acmeIdentifier has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-identifiers")
    public ResponseEntity<AcmeIdentifier> createAcmeIdentifier(@Valid @RequestBody AcmeIdentifier acmeIdentifier) throws URISyntaxException {
        log.debug("REST request to save AcmeIdentifier : {}", acmeIdentifier);
        if (acmeIdentifier.getId() != null) {
            throw new BadRequestAlertException("A new acmeIdentifier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeIdentifier result = acmeIdentifierService.save(acmeIdentifier);
        return ResponseEntity.created(new URI("/api/acme-identifiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-identifiers} : Updates an existing acmeIdentifier.
     *
     * @param acmeIdentifier the acmeIdentifier to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeIdentifier,
     * or with status {@code 400 (Bad Request)} if the acmeIdentifier is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeIdentifier couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-identifiers")
    public ResponseEntity<AcmeIdentifier> updateAcmeIdentifier(@Valid @RequestBody AcmeIdentifier acmeIdentifier) throws URISyntaxException {
        log.debug("REST request to update AcmeIdentifier : {}", acmeIdentifier);
        if (acmeIdentifier.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeIdentifier result = acmeIdentifierService.save(acmeIdentifier);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeIdentifier.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-identifiers} : get all the acmeIdentifiers.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeIdentifiers in body.
     */
    @GetMapping("/acme-identifiers")
    public List<AcmeIdentifier> getAllAcmeIdentifiers() {
        log.debug("REST request to get all AcmeIdentifiers");
        return acmeIdentifierService.findAll();
    }

    /**
     * {@code GET  /acme-identifiers/:id} : get the "id" acmeIdentifier.
     *
     * @param id the id of the acmeIdentifier to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeIdentifier, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-identifiers/{id}")
    public ResponseEntity<AcmeIdentifier> getAcmeIdentifier(@PathVariable Long id) {
        log.debug("REST request to get AcmeIdentifier : {}", id);
        Optional<AcmeIdentifier> acmeIdentifier = acmeIdentifierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeIdentifier);
    }

    /**
     * {@code DELETE  /acme-identifiers/:id} : delete the "id" acmeIdentifier.
     *
     * @param id the id of the acmeIdentifier to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-identifiers/{id}")
    public ResponseEntity<Void> deleteAcmeIdentifier(@PathVariable Long id) {
        log.debug("REST request to delete AcmeIdentifier : {}", id);
        acmeIdentifierService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
