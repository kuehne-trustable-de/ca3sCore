package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.service.PipelineService;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Pipeline}.
 */
@RestController
@RequestMapping("/api")
public class PipelineViewResource {

    private final Logger log = LoggerFactory.getLogger(PipelineViewResource.class);

    private static final String ENTITY_NAME = "pipeline";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PipelineService pipelineService;

    @Autowired
    private PipelineUtil pipelineUtil;

    public PipelineViewResource(PipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    /**
     * {@code POST  /pipelineViews} : Create a new pipeline.
     *
     * @param pipelineView the pipeline to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pipeline, or with status {@code 400 (Bad Request)} if the pipeline has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/pipelineViews")
    public ResponseEntity<PipelineView> createPipeline(@Valid @RequestBody PipelineView pipelineView) throws URISyntaxException {
        log.debug("REST request to save PipelineView : {}", pipelineView);
        if (pipelineView.getId() != null) {
            throw new BadRequestAlertException("A new pipeline cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Pipeline p = pipelineUtil.toPipeline(pipelineView);
        return ResponseEntity.created(new URI("/api/pipelineViews/" + p.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, p.getId().toString()))
            .body(pipelineView);
    }

    /**
     * {@code PUT  /pipelineViews} : Updates an existing pipeline.
     *
     * @param pipelineView the pipeline to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated pipeline,
     * or with status {@code 400 (Bad Request)} if the pipeline is not valid,
     * or with status {@code 500 (Internal Server Error)} if the pipeline couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/pipelineViews")
    public ResponseEntity<PipelineView> updatePipeline(@Valid @RequestBody PipelineView pipelineView) throws URISyntaxException {
        log.debug("REST request to update PipelineView : {}", pipelineView);
        if (pipelineView.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Pipeline p = pipelineUtil.toPipeline(pipelineView);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, p.getId().toString()))
            .body(pipelineView);
    }

    /**
     * {@code GET  /pipelineViews} : get all the pipelines.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of pipelines in body.
     */
    @GetMapping("/pipelineViews")
    public List<PipelineView> getAllPipelines() {
        log.debug("REST request to get all PipelineViews");
        List<PipelineView> pvList = new ArrayList<PipelineView>();
        for( Pipeline p: pipelineService.findAll()){
            pvList.add(pipelineUtil.from(p));
        }
        return pvList;
    }

    /**
     * {@code GET  /pipelineViews/:id} : get the "id" pipeline.
     *
     * @param id the id of the pipeline to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pipeline, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/pipelineViews/{id}")
    public ResponseEntity<PipelineView> getPipeline(@PathVariable Long id) {
        log.debug("REST request to get PipelineView : {}", id);
        Optional<Pipeline> pipelineOpt = pipelineService.findOne(id);
        Optional<PipelineView> pvOpt = Optional.empty();
        if( pipelineOpt.isPresent()){
            pvOpt = Optional.of( pipelineUtil.from(pipelineOpt.get()));
        }
        return ResponseUtil.wrapOrNotFound(pvOpt);
    }

    /**
     * {@code DELETE  /pipelines/:id} : delete the "id" pipeline.
     *
     * @param id the id of the pipeline to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/pipelineViews/{id}")
    public ResponseEntity<Void> deletePipeline(@PathVariable Long id) {
        log.debug("REST request to delete Pipeline : {}", id);
        pipelineService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
