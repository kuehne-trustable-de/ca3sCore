package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.service.AuthorizationService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Authorization}.
 */
@RestController
@RequestMapping("/api")
public class AuthorizationResource {

    private final Logger log = LoggerFactory.getLogger(AuthorizationResource.class);

    private static final String ENTITY_NAME = "authorization";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AuthorizationService authorizationService;

    public AuthorizationResource(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * {@code POST  /authorizations} : Create a new authorization.
     *
     * @param authorization the authorization to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authorization, or with status {@code 400 (Bad Request)} if the authorization has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/authorizations")
    public ResponseEntity<AcmeAuthorization> createAuthorization(@Valid @RequestBody AcmeAuthorization authorization) throws URISyntaxException {
        log.debug("REST request to save Authorization : {}", authorization);
        if (authorization.getId() != null) {
            throw new BadRequestAlertException("A new authorization cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeAuthorization result = authorizationService.save(authorization);
        return ResponseEntity.created(new URI("/api/authorizations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /authorizations} : Updates an existing authorization.
     *
     * @param authorization the authorization to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated authorization,
     * or with status {@code 400 (Bad Request)} if the authorization is not valid,
     * or with status {@code 500 (Internal Server Error)} if the authorization couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/authorizations")
    public ResponseEntity<AcmeAuthorization> updateAuthorization(@Valid @RequestBody AcmeAuthorization authorization) throws URISyntaxException {
        log.debug("REST request to update Authorization : {}", authorization);
        if (authorization.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeAuthorization result = authorizationService.save(authorization);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, authorization.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /authorizations} : get all the authorizations.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authorizations in body.
     */
    @GetMapping("/authorizations")
    public List<AcmeAuthorization> getAllAuthorizations() {
        log.debug("REST request to get all Authorizations");
        return authorizationService.findAll();
    }

    /**
     * {@code GET  /authorizations/:id} : get the "id" authorization.
     *
     * @param id the id of the authorization to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authorization, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/authorizations/{id}")
    public ResponseEntity<AcmeAuthorization> getAuthorization(@PathVariable Long id) {
        log.debug("REST request to get Authorization : {}", id);
        Optional<AcmeAuthorization> authorization = authorizationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(authorization);
    }

    /**
     * {@code DELETE  /authorizations/:id} : delete the "id" authorization.
     *
     * @param id the id of the authorization to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/authorizations/{id}")
    public ResponseEntity<Void> deleteAuthorization(@PathVariable Long id) {
        log.debug("REST request to delete Authorization : {}", id);
        authorizationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
