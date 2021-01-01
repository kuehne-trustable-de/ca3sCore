package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeNonce;
import de.trustable.ca3s.core.service.AcmeNonceService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeNonce}.
 */
@RestController
@RequestMapping("/api")
public class AcmeNonceResource {

    private final Logger log = LoggerFactory.getLogger(AcmeNonceResource.class);

    private static final String ENTITY_NAME = "acmeNonce";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeNonceService acmeNonceService;

    public AcmeNonceResource(AcmeNonceService acmeNonceService) {
        this.acmeNonceService = acmeNonceService;
    }

    /**
     * {@code POST  /acme-nonces} : Create a new acmeNonce.
     *
     * @param acmeNonce the acmeNonce to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeNonce, or with status {@code 400 (Bad Request)} if the acmeNonce has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-nonces")
    public ResponseEntity<AcmeNonce> createAcmeNonce(@RequestBody AcmeNonce acmeNonce) throws URISyntaxException {
        log.debug("REST request to save AcmeNonce : {}", acmeNonce);
        if (acmeNonce.getId() != null) {
            throw new BadRequestAlertException("A new acmeNonce cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeNonce result = acmeNonceService.save(acmeNonce);
        return ResponseEntity.created(new URI("/api/acme-nonces/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-nonces} : Updates an existing acmeNonce.
     *
     * @param acmeNonce the acmeNonce to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeNonce,
     * or with status {@code 400 (Bad Request)} if the acmeNonce is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeNonce couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-nonces")
    public ResponseEntity<AcmeNonce> updateAcmeNonce(@RequestBody AcmeNonce acmeNonce) throws URISyntaxException {
        log.debug("REST request to update AcmeNonce : {}", acmeNonce);
        if (acmeNonce.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeNonce result = acmeNonceService.save(acmeNonce);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeNonce.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-nonces} : get all the acmeNonces.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeNonces in body.
     */
    @GetMapping("/acme-nonces")
    public List<AcmeNonce> getAllAcmeNonces() {
        log.debug("REST request to get all AcmeNonces");
        return acmeNonceService.findAll();
    }

    /**
     * {@code GET  /acme-nonces/:id} : get the "id" acmeNonce.
     *
     * @param id the id of the acmeNonce to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeNonce, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-nonces/{id}")
    public ResponseEntity<AcmeNonce> getAcmeNonce(@PathVariable Long id) {
        log.debug("REST request to get AcmeNonce : {}", id);
        Optional<AcmeNonce> acmeNonce = acmeNonceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeNonce);
    }

    /**
     * {@code DELETE  /acme-nonces/:id} : delete the "id" acmeNonce.
     *
     * @param id the id of the acmeNonce to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-nonces/{id}")
    public ResponseEntity<Void> deleteAcmeNonce(@PathVariable Long id) {
        log.debug("REST request to delete AcmeNonce : {}", id);
        acmeNonceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
