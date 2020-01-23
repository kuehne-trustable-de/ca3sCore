package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.service.ProtectedContentService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.ProtectedContent}.
 */
@RestController
@RequestMapping("/api")
public class ProtectedContentResource {

    private final Logger log = LoggerFactory.getLogger(ProtectedContentResource.class);

    private static final String ENTITY_NAME = "protectedContent";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ProtectedContentService protectedContentService;

    public ProtectedContentResource(ProtectedContentService protectedContentService) {
        this.protectedContentService = protectedContentService;
    }

    /**
     * {@code POST  /protected-contents} : Create a new protectedContent.
     *
     * @param protectedContent the protectedContent to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new protectedContent, or with status {@code 400 (Bad Request)} if the protectedContent has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/protected-contents")
    public ResponseEntity<ProtectedContent> createProtectedContent(@Valid @RequestBody ProtectedContent protectedContent) throws URISyntaxException {
        log.debug("REST request to save ProtectedContent : {}", protectedContent);
        if (protectedContent.getId() != null) {
            throw new BadRequestAlertException("A new protectedContent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProtectedContent result = protectedContentService.save(protectedContent);
        return ResponseEntity.created(new URI("/api/protected-contents/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /protected-contents} : Updates an existing protectedContent.
     *
     * @param protectedContent the protectedContent to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated protectedContent,
     * or with status {@code 400 (Bad Request)} if the protectedContent is not valid,
     * or with status {@code 500 (Internal Server Error)} if the protectedContent couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/protected-contents")
    public ResponseEntity<ProtectedContent> updateProtectedContent(@Valid @RequestBody ProtectedContent protectedContent) throws URISyntaxException {
        log.debug("REST request to update ProtectedContent : {}", protectedContent);
        if (protectedContent.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ProtectedContent result = protectedContentService.save(protectedContent);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, protectedContent.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /protected-contents} : get all the protectedContents.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of protectedContents in body.
     */
    @GetMapping("/protected-contents")
    public List<ProtectedContent> getAllProtectedContents() {
        log.debug("REST request to get all ProtectedContents");
        return protectedContentService.findAll();
    }

    /**
     * {@code GET  /protected-contents/:id} : get the "id" protectedContent.
     *
     * @param id the id of the protectedContent to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the protectedContent, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/protected-contents/{id}")
    public ResponseEntity<ProtectedContent> getProtectedContent(@PathVariable Long id) {
        log.debug("REST request to get ProtectedContent : {}", id);
        Optional<ProtectedContent> protectedContent = protectedContentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(protectedContent);
    }

    /**
     * {@code DELETE  /protected-contents/:id} : delete the "id" protectedContent.
     *
     * @param id the id of the protectedContent to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/protected-contents/{id}")
    public ResponseEntity<Void> deleteProtectedContent(@PathVariable Long id) {
        log.debug("REST request to delete ProtectedContent : {}", id);
        protectedContentService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
