package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.service.AcmeChallengeService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeChallenge}.
 */
@RestController
@RequestMapping("/api")
public class AcmeChallengeResource {

    private final Logger log = LoggerFactory.getLogger(AcmeChallengeResource.class);

    private static final String ENTITY_NAME = "acmeChallenge";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeChallengeService acmeChallengeService;

    public AcmeChallengeResource(AcmeChallengeService acmeChallengeService) {
        this.acmeChallengeService = acmeChallengeService;
    }

    /**
     * {@code POST  /acme-challenges} : Create a new acmeChallenge.
     *
     * @param acmeChallenge the acmeChallenge to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeChallenge, or with status {@code 400 (Bad Request)} if the acmeChallenge has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-challenges")
    public ResponseEntity<AcmeChallenge> createAcmeChallenge(@Valid @RequestBody AcmeChallenge acmeChallenge) throws URISyntaxException {
        log.debug("REST request to save AcmeChallenge : {}", acmeChallenge);
        if (acmeChallenge.getId() != null) {
            throw new BadRequestAlertException("A new acmeChallenge cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeChallenge result = acmeChallengeService.save(acmeChallenge);
        return ResponseEntity.created(new URI("/api/acme-challenges/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-challenges} : Updates an existing acmeChallenge.
     *
     * @param acmeChallenge the acmeChallenge to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeChallenge,
     * or with status {@code 400 (Bad Request)} if the acmeChallenge is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeChallenge couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-challenges")
    public ResponseEntity<AcmeChallenge> updateAcmeChallenge(@Valid @RequestBody AcmeChallenge acmeChallenge) throws URISyntaxException {
        log.debug("REST request to update AcmeChallenge : {}", acmeChallenge);
        if (acmeChallenge.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeChallenge result = acmeChallengeService.save(acmeChallenge);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeChallenge.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-challenges} : get all the acmeChallenges.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeChallenges in body.
     */
    @GetMapping("/acme-challenges")
    public List<AcmeChallenge> getAllAcmeChallenges() {
        log.debug("REST request to get all AcmeChallenges");
        return acmeChallengeService.findAll();
    }

    /**
     * {@code GET  /acme-challenges/:id} : get the "id" acmeChallenge.
     *
     * @param id the id of the acmeChallenge to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeChallenge, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-challenges/{id}")
    public ResponseEntity<AcmeChallenge> getAcmeChallenge(@PathVariable Long id) {
        log.debug("REST request to get AcmeChallenge : {}", id);
        Optional<AcmeChallenge> acmeChallenge = acmeChallengeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeChallenge);
    }

    /**
     * {@code DELETE  /acme-challenges/:id} : delete the "id" acmeChallenge.
     *
     * @param id the id of the acmeChallenge to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-challenges/{id}")
    public ResponseEntity<Void> deleteAcmeChallenge(@PathVariable Long id) {
        log.debug("REST request to delete AcmeChallenge : {}", id);
        acmeChallengeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
