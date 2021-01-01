package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.service.BPNMProcessInfoService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.BPNMProcessInfo}.
 */
@RestController
@RequestMapping("/api")
public class BPNMProcessInfoResource {

    private final Logger log = LoggerFactory.getLogger(BPNMProcessInfoResource.class);

    private static final String ENTITY_NAME = "bPNMProcessInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BPNMProcessInfoService bPNMProcessInfoService;

    public BPNMProcessInfoResource(BPNMProcessInfoService bPNMProcessInfoService) {
        this.bPNMProcessInfoService = bPNMProcessInfoService;
    }

    /**
     * {@code POST  /bpnm-process-infos} : Create a new bPNMProcessInfo.
     *
     * @param bPNMProcessInfo the bPNMProcessInfo to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new bPNMProcessInfo, or with status {@code 400 (Bad Request)} if the bPNMProcessInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/bpnm-process-infos")
    public ResponseEntity<BPNMProcessInfo> createBPNMProcessInfo(@Valid @RequestBody BPNMProcessInfo bPNMProcessInfo) throws URISyntaxException {
        log.debug("REST request to save BPNMProcessInfo : {}", bPNMProcessInfo);
        if (bPNMProcessInfo.getId() != null) {
            throw new BadRequestAlertException("A new bPNMProcessInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BPNMProcessInfo result = bPNMProcessInfoService.save(bPNMProcessInfo);
        return ResponseEntity.created(new URI("/api/bpnm-process-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /bpnm-process-infos} : Updates an existing bPNMProcessInfo.
     *
     * @param bPNMProcessInfo the bPNMProcessInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated bPNMProcessInfo,
     * or with status {@code 400 (Bad Request)} if the bPNMProcessInfo is not valid,
     * or with status {@code 500 (Internal Server Error)} if the bPNMProcessInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/bpnm-process-infos")
    public ResponseEntity<BPNMProcessInfo> updateBPNMProcessInfo(@Valid @RequestBody BPNMProcessInfo bPNMProcessInfo) throws URISyntaxException {
        log.debug("REST request to update BPNMProcessInfo : {}", bPNMProcessInfo);
        if (bPNMProcessInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BPNMProcessInfo result = bPNMProcessInfoService.save(bPNMProcessInfo);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, bPNMProcessInfo.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /bpnm-process-infos} : get all the bPNMProcessInfos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bPNMProcessInfos in body.
     */
    @GetMapping("/bpnm-process-infos")
    public List<BPNMProcessInfo> getAllBPNMProcessInfos() {
        log.debug("REST request to get all BPNMProcessInfos");
        return bPNMProcessInfoService.findAll();
    }

    /**
     * {@code GET  /bpnm-process-infos/:id} : get the "id" bPNMProcessInfo.
     *
     * @param id the id of the bPNMProcessInfo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the bPNMProcessInfo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/bpnm-process-infos/{id}")
    public ResponseEntity<BPNMProcessInfo> getBPNMProcessInfo(@PathVariable Long id) {
        log.debug("REST request to get BPNMProcessInfo : {}", id);
        Optional<BPNMProcessInfo> bPNMProcessInfo = bPNMProcessInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(bPNMProcessInfo);
    }

    /**
     * {@code DELETE  /bpnm-process-infos/:id} : delete the "id" bPNMProcessInfo.
     *
     * @param id the id of the bPNMProcessInfo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/bpnm-process-infos/{id}")
    public ResponseEntity<Void> deleteBPNMProcessInfo(@PathVariable Long id) {
        log.debug("REST request to delete BPNMProcessInfo : {}", id);
        bPNMProcessInfoService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
