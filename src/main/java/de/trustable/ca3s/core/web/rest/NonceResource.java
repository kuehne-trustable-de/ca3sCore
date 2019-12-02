package de.trustable.ca3s.core.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.Nonce;
import de.trustable.ca3s.core.service.NonceService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Nonce}.
 */
@RestController
@RequestMapping("/api")
public class NonceResource {

    private final Logger log = LoggerFactory.getLogger(NonceResource.class);

    private static final String ENTITY_NAME = "nonce";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NonceService nonceService;

    public NonceResource(NonceService nonceService) {
        this.nonceService = nonceService;
    }

    /**
     * {@code POST  /nonces} : Create a new nonce.
     *
     * @param nonce the nonce to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new nonce, or with status {@code 400 (Bad Request)} if the nonce has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/nonces")
    public ResponseEntity<Nonce> createNonce(@RequestBody Nonce nonce) throws URISyntaxException {
        log.debug("REST request to save Nonce : {}", nonce);
        if (nonce.getId() != null) {
            throw new BadRequestAlertException("A new nonce cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Nonce result = nonceService.save(nonce);
        return ResponseEntity.created(new URI("/api/nonces/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /nonces} : Updates an existing nonce.
     *
     * @param nonce the nonce to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated nonce,
     * or with status {@code 400 (Bad Request)} if the nonce is not valid,
     * or with status {@code 500 (Internal Server Error)} if the nonce couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/nonces")
    public ResponseEntity<Nonce> updateNonce(@RequestBody Nonce nonce) throws URISyntaxException {
        log.debug("REST request to update Nonce : {}", nonce);
        if (nonce.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Nonce result = nonceService.save(nonce);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, nonce.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /nonces} : get all the nonces.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of nonces in body.
     */
    @GetMapping("/nonces")
    public List<Nonce> getAllNonces() {
        log.debug("REST request to get all Nonces");
        return nonceService.findAll();
    }

    /**
     * {@code GET  /nonces/:id} : get the "id" nonce.
     *
     * @param id the id of the nonce to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the nonce, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/nonces/{id}")
    public ResponseEntity<Nonce> getNonce(@PathVariable Long id) {
        log.debug("REST request to get Nonce : {}", id);
        Optional<Nonce> nonce = nonceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(nonce);
    }

    /**
     * {@code DELETE  /nonces/:id} : delete the "id" nonce.
     *
     * @param id the id of the nonce to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/nonces/{id}")
    public ResponseEntity<Void> deleteNonce(@PathVariable Long id) {
        log.debug("REST request to delete Nonce : {}", id);
        nonceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
