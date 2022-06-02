package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

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
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.UserPreference}.
 */
@RestController
@RequestMapping("/api")
public class UserPreferenceResource {

    private final Logger log = LoggerFactory.getLogger(UserPreferenceResource.class);

    private static final String ENTITY_NAME = "userPreference";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceResource(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    /**
     * {@code POST  /user-preferences} : Create a new userPreference.
     *
     * @param userPreference the userPreference to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new userPreference, or with status {@code 400 (Bad Request)} if the userPreference has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/user-preferences")
    public ResponseEntity<UserPreference> createUserPreference(@Valid @RequestBody UserPreference userPreference) throws URISyntaxException {
        log.debug("REST request to save UserPreference : {}", userPreference);
        if (userPreference.getId() != null) {
            throw new BadRequestAlertException("A new userPreference cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UserPreference result = userPreferenceService.save(userPreference);
        return ResponseEntity.created(new URI("/api/user-preferences/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /user-preferences} : Updates an existing userPreference.
     *
     * @param userPreference the userPreference to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated userPreference,
     * or with status {@code 400 (Bad Request)} if the userPreference is not valid,
     * or with status {@code 500 (Internal Server Error)} if the userPreference couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/user-preferences")
    public ResponseEntity<UserPreference> updateUserPreference(@Valid @RequestBody UserPreference userPreference) throws URISyntaxException {
        log.debug("REST request to update UserPreference : {}", userPreference);
        if (userPreference.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        UserPreference result = userPreferenceService.save(userPreference);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userPreference.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /user-preferences/:id} : get the "id" userPreference.
     *
     * @param id the id of the userPreference to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the userPreference, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/user-preferences/{id}")
    public ResponseEntity<UserPreference> getUserPreference(@PathVariable Long id) {
        log.debug("REST request to get UserPreference : {}", id);
        Optional<UserPreference> userPreference = userPreferenceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(userPreference);
    }

    /**
     * {@code DELETE  /user-preferences/:id} : delete the "id" userPreference.
     *
     * @param id the id of the userPreference to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/user-preferences/{id}")
    public ResponseEntity<Void> deleteUserPreference(@PathVariable Long id) {
        log.debug("REST request to delete UserPreference : {}", id);
        userPreferenceService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
