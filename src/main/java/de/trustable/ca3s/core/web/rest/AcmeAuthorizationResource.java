package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.service.AcmeAuthorizationService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeAuthorization}.
 */
@RestController
@RequestMapping("/api")
public class AcmeAuthorizationResource {

    private final Logger log = LoggerFactory.getLogger(AcmeAuthorizationResource.class);

    private static final String ENTITY_NAME = "acmeAuthorization";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeAuthorizationService acmeAuthorizationService;

    public AcmeAuthorizationResource(AcmeAuthorizationService acmeAuthorizationService) {
        this.acmeAuthorizationService = acmeAuthorizationService;
    }

    /**
     * {@code POST  /acme-authorizations} : Create a new acmeAuthorization.
     *
     * @param acmeAuthorization the acmeAuthorization to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeAuthorization, or with status {@code 400 (Bad Request)} if the acmeAuthorization has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-authorizations")
    public ResponseEntity<AcmeAuthorization> createAcmeAuthorization(@Valid @RequestBody AcmeAuthorization acmeAuthorization) throws URISyntaxException {
        log.debug("REST request to save AcmeAuthorization : {}", acmeAuthorization);
        if (acmeAuthorization.getId() != null) {
            throw new BadRequestAlertException("A new acmeAuthorization cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeAuthorization result = acmeAuthorizationService.save(acmeAuthorization);
        return ResponseEntity.created(new URI("/api/acme-authorizations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-authorizations} : Updates an existing acmeAuthorization.
     *
     * @param acmeAuthorization the acmeAuthorization to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeAuthorization,
     * or with status {@code 400 (Bad Request)} if the acmeAuthorization is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeAuthorization couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-authorizations")
    public ResponseEntity<AcmeAuthorization> updateAcmeAuthorization(@Valid @RequestBody AcmeAuthorization acmeAuthorization) throws URISyntaxException {
        log.debug("REST request to update AcmeAuthorization : {}", acmeAuthorization);
        if (acmeAuthorization.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeAuthorization result = acmeAuthorizationService.save(acmeAuthorization);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeAuthorization.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-authorizations} : get all the acmeAuthorizations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeAuthorizations in body.
     */
    @GetMapping("/acme-authorizations")
    public List<AcmeAuthorization> getAllAcmeAuthorizations() {
        log.debug("REST request to get all AcmeAuthorizations");
        return acmeAuthorizationService.findAll();
    }

    /**
     * {@code GET  /acme-authorizations/:id} : get the "id" acmeAuthorization.
     *
     * @param id the id of the acmeAuthorization to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeAuthorization, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-authorizations/{id}")
    public ResponseEntity<AcmeAuthorization> getAcmeAuthorization(@PathVariable Long id) {
        log.debug("REST request to get AcmeAuthorization : {}", id);
        Optional<AcmeAuthorization> acmeAuthorization = acmeAuthorizationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeAuthorization);
    }

    /**
     * {@code DELETE  /acme-authorizations/:id} : delete the "id" acmeAuthorization.
     *
     * @param id the id of the acmeAuthorization to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-authorizations/{id}")
    public ResponseEntity<Void> deleteAcmeAuthorization(@PathVariable Long id) {
        log.debug("REST request to delete AcmeAuthorization : {}", id);
        acmeAuthorizationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
