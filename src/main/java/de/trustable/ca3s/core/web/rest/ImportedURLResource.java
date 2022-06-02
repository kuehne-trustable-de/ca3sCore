package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.service.ImportedURLService;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.ImportedURL}.
 */
@RestController
@RequestMapping("/api")
public class ImportedURLResource {

    private final Logger log = LoggerFactory.getLogger(ImportedURLResource.class);

    private static final String ENTITY_NAME = "importedURL";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ImportedURLService importedURLService;

    public ImportedURLResource(ImportedURLService importedURLService) {
        this.importedURLService = importedURLService;
    }

    /**
     * {@code POST  /imported-urls} : Create a new importedURL.
     *
     * @param importedURL the importedURL to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new importedURL, or with status {@code 400 (Bad Request)} if the importedURL has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/imported-urls")
    public ResponseEntity<ImportedURL> createImportedURL(@Valid @RequestBody ImportedURL importedURL) throws URISyntaxException {
        log.debug("REST request to save ImportedURL : {}", importedURL);
        if (importedURL.getId() != null) {
            throw new BadRequestAlertException("A new importedURL cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ImportedURL result = importedURLService.save(importedURL);
        return ResponseEntity.created(new URI("/api/imported-urls/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /imported-urls} : Updates an existing importedURL.
     *
     * @param importedURL the importedURL to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated importedURL,
     * or with status {@code 400 (Bad Request)} if the importedURL is not valid,
     * or with status {@code 500 (Internal Server Error)} if the importedURL couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/imported-urls")
    public ResponseEntity<ImportedURL> updateImportedURL(@Valid @RequestBody ImportedURL importedURL) throws URISyntaxException {
        log.debug("REST request to update ImportedURL : {}", importedURL);
        if (importedURL.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ImportedURL result = importedURLService.save(importedURL);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, importedURL.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /imported-urls} : get all the importedURLS.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of importedURLS in body.
     */
    @GetMapping("/imported-urls")
    public List<ImportedURL> getAllImportedURLS() {
        log.debug("REST request to get all ImportedURLS");
        return importedURLService.findAll();
    }

    /**
     * {@code GET  /imported-urls/:id} : get the "id" importedURL.
     *
     * @param id the id of the importedURL to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the importedURL, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/imported-urls/{id}")
    public ResponseEntity<ImportedURL> getImportedURL(@PathVariable Long id) {
        log.debug("REST request to get ImportedURL : {}", id);
        Optional<ImportedURL> importedURL = importedURLService.findOne(id);
        return ResponseUtil.wrapOrNotFound(importedURL);
    }

    /**
     * {@code DELETE  /imported-urls/:id} : delete the "id" importedURL.
     *
     * @param id the id of the importedURL to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/imported-urls/{id}")
    public ResponseEntity<Void> deleteImportedURL(@PathVariable Long id) {
        log.debug("REST request to delete ImportedURL : {}", id);
        importedURLService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
