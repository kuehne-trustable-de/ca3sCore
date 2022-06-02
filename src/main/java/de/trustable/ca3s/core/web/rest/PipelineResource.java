package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.service.PipelineService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Pipeline}.
 */
@RestController
@RequestMapping("/api")
public class PipelineResource {

    private final Logger log = LoggerFactory.getLogger(PipelineResource.class);

    private static final String ENTITY_NAME = "pipeline";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PipelineService pipelineService;

    public PipelineResource(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * {@code POST  /pipelines} : Create a new pipeline.
     *
     * @param pipeline the pipeline to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pipeline, or with status {@code 400 (Bad Request)} if the pipeline has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pipelines")
    public ResponseEntity<Pipeline> createPipeline(@Valid @RequestBody Pipeline pipeline) throws URISyntaxException {
        log.debug("REST request to save Pipeline : {}", pipeline);
        if (pipeline.getId() != null) {
            throw new BadRequestAlertException("A new pipeline cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Pipeline result = pipelineService.save(pipeline);
        return ResponseEntity.created(new URI("/api/pipelines/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /pipelines} : Updates an existing pipeline.
     *
     * @param pipeline the pipeline to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pipeline,
     * or with status {@code 400 (Bad Request)} if the pipeline is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pipeline couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pipelines")
    public ResponseEntity<Pipeline> updatePipeline(@Valid @RequestBody Pipeline pipeline) throws URISyntaxException {
        log.debug("REST request to update Pipeline : {}", pipeline);
        if (pipeline.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        Pipeline result = pipelineService.save(pipeline);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, pipeline.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /pipelines} : get all the pipelines.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pipelines in body.
     */
    @GetMapping("/pipelines")
    public List<Pipeline> getAllPipelines() {
        log.debug("REST request to get all Pipelines");
        return pipelineService.findAll();
    }

    /**
     * {@code GET  /pipelines/:id} : get the "id" pipeline.
     *
     * @param id the id of the pipeline to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pipeline, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pipelines/{id}")
    public ResponseEntity<Pipeline> getPipeline(@PathVariable Long id) {
        log.debug("REST request to get Pipeline : {}", id);
        Optional<Pipeline> pipeline = pipelineService.findOne(id);
        return ResponseUtil.wrapOrNotFound(pipeline);
    }

    /**
     * {@code DELETE  /pipelines/:id} : delete the "id" pipeline.
     *
     * @param id the id of the pipeline to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pipelines/{id}")
    public ResponseEntity<Void> deletePipeline(@PathVariable Long id) {
        log.debug("REST request to delete Pipeline : {}", id);
        pipelineService.delete(id);

        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
