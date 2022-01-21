package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.service.PipelineAttributeService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.PipelineAttribute}.
 */
@RestController
@RequestMapping("/api")
public class PipelineAttributeResource {

    private final Logger log = LoggerFactory.getLogger(PipelineAttributeResource.class);

    private static final String ENTITY_NAME = "pipelineAttribute";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PipelineAttributeService pipelineAttributeService;

    public PipelineAttributeResource(PipelineAttributeService pipelineAttributeService) {
        this.pipelineAttributeService = pipelineAttributeService;
    }

    /**
     * {@code POST  /pipeline-attributes} : Create a new pipelineAttribute.
     *
     * @param pipelineAttribute the pipelineAttribute to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pipelineAttribute, or with status {@code 400 (Bad Request)} if the pipelineAttribute has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pipeline-attributes")
    public ResponseEntity<PipelineAttribute> createPipelineAttribute(@Valid @RequestBody PipelineAttribute pipelineAttribute) throws URISyntaxException {
        log.debug("REST request to save PipelineAttribute : {}", pipelineAttribute);
        if (pipelineAttribute.getId() != null) {
            throw new BadRequestAlertException("A new pipelineAttribute cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PipelineAttribute result = pipelineAttributeService.save(pipelineAttribute);
        return ResponseEntity.created(new URI("/api/pipeline-attributes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /pipeline-attributes} : Updates an existing pipelineAttribute.
     *
     * @param pipelineAttribute the pipelineAttribute to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pipelineAttribute,
     * or with status {@code 400 (Bad Request)} if the pipelineAttribute is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pipelineAttribute couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pipeline-attributes")
    public ResponseEntity<PipelineAttribute> updatePipelineAttribute(@Valid @RequestBody PipelineAttribute pipelineAttribute) throws URISyntaxException {
        log.debug("REST request to update PipelineAttribute : {}", pipelineAttribute);
        if (pipelineAttribute.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        PipelineAttribute result = pipelineAttributeService.save(pipelineAttribute);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pipelineAttribute.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /pipeline-attributes} : get all the pipelineAttributes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pipelineAttributes in body.
     */
    @GetMapping("/pipeline-attributes")
    public List<PipelineAttribute> getAllPipelineAttributes() {
        log.debug("REST request to get all PipelineAttributes");
        return pipelineAttributeService.findAll();
    }

    /**
     * {@code GET  /pipeline-attributes/:id} : get the "id" pipelineAttribute.
     *
     * @param id the id of the pipelineAttribute to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pipelineAttribute, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pipeline-attributes/{id}")
    public ResponseEntity<PipelineAttribute> getPipelineAttribute(@PathVariable Long id) {
        log.debug("REST request to get PipelineAttribute : {}", id);
        Optional<PipelineAttribute> pipelineAttribute = pipelineAttributeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(pipelineAttribute);
    }

    /**
     * {@code DELETE  /pipeline-attributes/:id} : delete the "id" pipelineAttribute.
     *
     * @param id the id of the pipelineAttribute to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pipeline-attributes/{id}")
    public ResponseEntity<Void> deletePipelineAttribute(@PathVariable Long id) {
        log.debug("REST request to delete PipelineAttribute : {}", id);
        pipelineAttributeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
