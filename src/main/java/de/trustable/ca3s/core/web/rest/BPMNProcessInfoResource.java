package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.BPMNProcessInfoService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

import de.trustable.ca3s.core.service.dto.BPMNProcessInfoView;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
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
    private final BPMNUtil bpmnUtil;

    public BPMNProcessInfoResource(BPMNProcessInfoService bPMNProcessInfoService, BPMNUtil bpmnUtil) {
        this.bPMNProcessInfoService = bPMNProcessInfoService;
        this.bpmnUtil = bpmnUtil;
    }

    /**
     * {@code POST  /bpmn-process-infos} : Create a new bPMNProcessInfoView.
     *
     * @param bPMNProcessInfoView the bPMNProcessInfoView to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bPMNProcessInfoView, or with status {@code 400 (Bad Request)} if the bPMNProcessInfoView has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bpmn-process-infos")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<BPMNProcessInfo> createBPMNProcessInfo(@Valid @RequestBody BPMNProcessInfoView bPMNProcessInfoView) throws URISyntaxException {
        log.debug("REST request to save BPMNProcessInfoView : {}", bPMNProcessInfoView);
        if (bPMNProcessInfoView.getId() != null) {
            throw new BadRequestAlertException("A new bPMNProcessInfoView cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BPMNProcessInfo result = bPMNProcessInfoService.save(bPMNProcessInfoView);
        return ResponseEntity.created(new URI("/api/bpmn-process-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /bpmn-process-infos} : Updates an existing bPMNProcessInfoView.
     *
     * @param bPMNProcessInfoView the bPMNProcessInfoView to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bPMNProcessInfoView,
     * or with status {@code 400 (Bad Request)} if the bPMNProcessInfoView is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bPMNProcessInfoView couldn't be updated.
     */
    @PutMapping("/bpmn-process-infos")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<BPMNProcessInfo> updateBPMNProcessInfo(@Valid @RequestBody BPMNProcessInfoView bPMNProcessInfoView) {
        log.debug("REST request to update BPMNProcessInfo : {}", bPMNProcessInfoView);
        if (bPMNProcessInfoView.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BPMNProcessInfo result = bPMNProcessInfoService.save(bPMNProcessInfoView);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, bPMNProcessInfoView.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /bpmn-process-infos} : get all the bPMNProcessInfos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bPMNProcessInfos in body.
     */
    @GetMapping("/bpmn-process-infos")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
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
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<BPMNProcessInfo> getBPMNProcessInfo(@PathVariable Long id) {
        log.debug("REST request to get BPMNProcessInfo : {}", id);
        Optional<BPMNProcessInfo> bPMNProcessInfo = bPMNProcessInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bPMNProcessInfo);
    }

    /**
     * {@code GET  /bpmn-process-info-view/:id} : get the "id" bPMNProcessInfo.
     *
     * @param id the id of the bPMNProcessInfo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bPMNProcessInfo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bpmn-process-info-view/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<BPMNProcessInfoView> getBPMNProcessInfoView(@PathVariable Long id) {
        log.debug("REST request to get BPMNProcessInfo : {}", id);
        Optional<BPMNProcessInfo> bPMNProcessInfo = bPMNProcessInfoService.findOne(id);
        if(bPMNProcessInfo.isPresent()){
            return ResponseEntity.ok(bpmnUtil.toBPMNProcessInfoView(bPMNProcessInfo.get()));
        }else{
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * {@code DELETE  /bpmn-process-infos/:id} : delete the "id" bPMNProcessInfo.
     *
     * @param id the id of the bPMNProcessInfo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bpmn-process-infos/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteBPMNProcessInfo(@PathVariable Long id) {
        log.debug("REST request to delete BPMNProcessInfo : {}", id);
        bPMNProcessInfoService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
