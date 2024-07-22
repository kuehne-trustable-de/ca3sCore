package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AlgorithmRestriction;
import de.trustable.ca3s.core.repository.AlgorithmRestrictionRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AlgorithmRestrictionService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AlgorithmRestriction}.
 */
@RestController
@RequestMapping("/api")
public class AlgorithmRestrictionResource {

    private final Logger log = LoggerFactory.getLogger(AlgorithmRestrictionResource.class);

    private static final String ENTITY_NAME = "algorithmRestriction";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AlgorithmRestrictionService algorithmRestrictionService;

    private final AlgorithmRestrictionRepository algorithmRestrictionRepository;

    public AlgorithmRestrictionResource(
        AlgorithmRestrictionService algorithmRestrictionService,
        AlgorithmRestrictionRepository algorithmRestrictionRepository
    ) {
        this.algorithmRestrictionService = algorithmRestrictionService;
        this.algorithmRestrictionRepository = algorithmRestrictionRepository;
    }

    /**
     * {@code POST  /algorithm-restrictions} : Create a new algorithmRestriction.
     *
     * @param algorithmRestriction the algorithmRestriction to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new algorithmRestriction, or with status {@code 400 (Bad Request)} if the algorithmRestriction has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/algorithm-restrictions")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlgorithmRestriction> createAlgorithmRestriction(@Valid @RequestBody AlgorithmRestriction algorithmRestriction)
        throws URISyntaxException {
        log.debug("REST request to save AlgorithmRestriction : {}", algorithmRestriction);
        if (algorithmRestriction.getId() != null) {
            throw new BadRequestAlertException("A new algorithmRestriction cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AlgorithmRestriction result = algorithmRestrictionService.save(algorithmRestriction);
        return ResponseEntity
            .created(new URI("/api/algorithm-restrictions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /algorithm-restrictions/:id} : Updates an existing algorithmRestriction.
     *
     * @param id the id of the algorithmRestriction to save.
     * @param algorithmRestriction the algorithmRestriction to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated algorithmRestriction,
     * or with status {@code 400 (Bad Request)} if the algorithmRestriction is not valid,
     * or with status {@code 500 (Internal Server Error)} if the algorithmRestriction couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/algorithm-restrictions/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlgorithmRestriction> updateAlgorithmRestriction(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody AlgorithmRestriction algorithmRestriction
    ) throws URISyntaxException {
        log.debug("REST request to update AlgorithmRestriction : {}, {}", id, algorithmRestriction);
        if (algorithmRestriction.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, algorithmRestriction.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!algorithmRestrictionRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        AlgorithmRestriction result = algorithmRestrictionService.save(algorithmRestriction);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, algorithmRestriction.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /algorithm-restrictions} : get all the algorithmRestrictions.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of algorithmRestrictions in body.
     */
    @GetMapping("/algorithm-restrictions")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<AlgorithmRestriction> getAllAlgorithmRestrictions() {
        log.debug("REST request to get all AlgorithmRestrictions");
        return algorithmRestrictionService.findAll();
    }

    /**
     * {@code GET  /algorithm-restrictions/:id} : get the "id" algorithmRestriction.
     *
     * @param id the id of the algorithmRestriction to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the algorithmRestriction, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/algorithm-restrictions/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AlgorithmRestriction> getAlgorithmRestriction(@PathVariable Long id) {
        log.debug("REST request to get AlgorithmRestriction : {}", id);
        Optional<AlgorithmRestriction> algorithmRestriction = algorithmRestrictionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(algorithmRestriction);
    }

    /**
     * {@code DELETE  /algorithm-restrictions/:id} : delete the "id" algorithmRestriction.
     *
     * @param id the id of the algorithmRestriction to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/algorithm-restrictions/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAlgorithmRestriction(@PathVariable Long id) {
        log.debug("REST request to delete AlgorithmRestriction : {}", id);
        algorithmRestrictionService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
