package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.service.BPMNProcessInfoService;
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
 * REST controller for managing {@link BPMNProcessInfo}.
 */
@RestController
@RequestMapping("/api")
public class BPMNProcessInfoResource {

    private final Logger log = LoggerFactory.getLogger(BPMNProcessInfoResource.class);

    private static final String ENTITY_NAME = "bPMNProcessInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BPMNProcessInfoService bPMNProcessInfoService;

    public BPMNProcessInfoResource(BPMNProcessInfoService bPMNProcessInfoService) {
        this.bPMNProcessInfoService = bPMNProcessInfoService;
    }

    /**
     * {@code POST  /bpmn-process-infos} : Create a new bPMNProcessInfo.
     *
     * @param bPMNProcessInfo the bPMNProcessInfo to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bPMNProcessInfo, or with status {@code 400 (Bad Request)} if the bPMNProcessInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bpmn-process-infos")
    public ResponseEntity<BPMNProcessInfo> createBPMNProcessInfo(@Valid @RequestBody BPMNProcessInfo bPMNProcessInfo) throws URISyntaxException {
        log.debug("REST request to save BPMNProcessInfo : {}", bPMNProcessInfo);
        if (bPMNProcessInfo.getId() != null) {
            throw new BadRequestAlertException("A new bPMNProcessInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BPMNProcessInfo result = bPMNProcessInfoService.save(bPMNProcessInfo);
        return ResponseEntity.created(new URI("/api/bpmn-process-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /bpmn-process-infos} : Updates an existing bPMNProcessInfo.
     *
     * @param bPMNProcessInfo the bPMNProcessInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bPMNProcessInfo,
     * or with status {@code 400 (Bad Request)} if the bPMNProcessInfo is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bPMNProcessInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/bpmn-process-infos")
    public ResponseEntity<BPMNProcessInfo> updateBPMNProcessInfo(@Valid @RequestBody BPMNProcessInfo bPMNProcessInfo) throws URISyntaxException {
        log.debug("REST request to update BPMNProcessInfo : {}", bPMNProcessInfo);
        if (bPMNProcessInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BPMNProcessInfo result = bPMNProcessInfoService.save(bPMNProcessInfo);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, bPMNProcessInfo.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /bpmn-process-infos} : get all the bPMNProcessInfos.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bPMNProcessInfos in body.
     */
    @GetMapping("/bpmn-process-infos")
    public List<BPMNProcessInfo> getAllBPMNProcessInfos() {
        log.debug("REST request to get all BPMNProcessInfos");
        return bPMNProcessInfoService.findAll();
    }

    /**
     * {@code GET  /bpmn-process-infos/:id} : get the "id" bPMNProcessInfo.
     *
     * @param id the id of the bPMNProcessInfo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bPMNProcessInfo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bpmn-process-infos/{id}")
    public ResponseEntity<BPMNProcessInfo> getBPMNProcessInfo(@PathVariable Long id) {
        log.debug("REST request to get BPMNProcessInfo : {}", id);
        Optional<BPMNProcessInfo> bPMNProcessInfo = bPMNProcessInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bPMNProcessInfo);
    }


    /**
     * {@code DELETE  /bpmn-process-infos/:id} : delete the "id" bPMNProcessInfo.
     *
     * @param id the id of the bPMNProcessInfo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bpmn-process-infos/{id}")
    public ResponseEntity<Void> deleteBPMNProcessInfo(@PathVariable Long id) {
        log.debug("REST request to delete BPMNProcessInfo : {}", id);
        bPMNProcessInfoService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
