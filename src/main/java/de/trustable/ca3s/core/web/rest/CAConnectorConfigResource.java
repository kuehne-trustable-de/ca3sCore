package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.service.CAConnectorConfigService;
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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CAConnectorConfig}.
 */
@RestController
@RequestMapping("/api")
public class CAConnectorConfigResource {

    private final Logger log = LoggerFactory.getLogger(CAConnectorConfigResource.class);

    private static final String ENTITY_NAME = "cAConnectorConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CAConnectorConfigService cAConnectorConfigService;

    public CAConnectorConfigResource(CAConnectorConfigService cAConnectorConfigService) {
        this.cAConnectorConfigService = cAConnectorConfigService;
    }

    /**
     * {@code POST  /ca-connector-configs} : Create a new cAConnectorConfig.
     *
     * @param cAConnectorConfig the cAConnectorConfig to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cAConnectorConfig, or with status {@code 400 (Bad Request)} if the cAConnectorConfig has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ca-connector-configs")
    public ResponseEntity<CAConnectorConfig> createCAConnectorConfig(@Valid @RequestBody CAConnectorConfig cAConnectorConfig) throws URISyntaxException {
        log.debug("REST request to save CAConnectorConfig : {}", cAConnectorConfig);
        if (cAConnectorConfig.getId() != null) {
            throw new BadRequestAlertException("A new cAConnectorConfig cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CAConnectorConfig result = cAConnectorConfigService.save(cAConnectorConfig);
        return ResponseEntity.created(new URI("/api/ca-connector-configs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /ca-connector-configs} : Updates an existing cAConnectorConfig.
     *
     * @param cAConnectorConfig the cAConnectorConfig to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cAConnectorConfig,
     * or with status {@code 400 (Bad Request)} if the cAConnectorConfig is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cAConnectorConfig couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ca-connector-configs")
    public ResponseEntity<CAConnectorConfig> updateCAConnectorConfig(@Valid @RequestBody CAConnectorConfig cAConnectorConfig) throws URISyntaxException {
        log.debug("REST request to update CAConnectorConfig : {}", cAConnectorConfig);
        if (cAConnectorConfig.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CAConnectorConfig result = cAConnectorConfigService.save(cAConnectorConfig);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cAConnectorConfig.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /ca-connector-configs} : get all the cAConnectorConfigs.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cAConnectorConfigs in body.
     */
    @GetMapping("/ca-connector-configs")
    public List<CAConnectorConfig> getAllCAConnectorConfigs() {
        log.debug("REST request to get all CAConnectorConfigs");
        return cAConnectorConfigService.findAll();
    }

    /**
     * {@code GET  /ca-connector-configs/:id} : get the "id" cAConnectorConfig.
     *
     * @param id the id of the cAConnectorConfig to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cAConnectorConfig, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ca-connector-configs/{id}")
    public ResponseEntity<CAConnectorConfig> getCAConnectorConfig(@PathVariable Long id) {
        log.debug("REST request to get CAConnectorConfig : {}", id);
        Optional<CAConnectorConfig> cAConnectorConfig = cAConnectorConfigService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cAConnectorConfig);
    }

    /**
     * {@code DELETE  /ca-connector-configs/:id} : delete the "id" cAConnectorConfig.
     *
     * @param id the id of the cAConnectorConfig to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ca-connector-configs/{id}")
    public ResponseEntity<Void> deleteCAConnectorConfig(@PathVariable Long id) {
        log.debug("REST request to delete CAConnectorConfig : {}", id);
        cAConnectorConfigService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
