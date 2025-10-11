package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.adcsCertUtil.ADCSInstanceDetails;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.exception.IntegrityException;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.CAConnectorConfigService;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;
import de.trustable.ca3s.core.service.dto.CaConnectorConfigView;
import de.trustable.ca3s.core.service.dto.adcs.ADCSInstanceDetailsView;
import de.trustable.ca3s.core.service.util.CaConnectorConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link Pipeline}.
 */
@RestController
@Transactional
@RequestMapping("/api")
public class CAConnectorConfigViewResource {

    private final Logger log = LoggerFactory.getLogger(CAConnectorConfigViewResource.class);

    private static final String ENTITY_NAME = "cAConnectorConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CAConnectorConfigService cAConnectorConfigService;

    private final CaConnectorConfigUtil caConnectorConfigUtil;

    private final ADCSConnector adcsConnector;

    public CAConnectorConfigViewResource(CAConnectorConfigService cAConnectorConfigService, CaConnectorConfigUtil caConnectorConfigUtil, ADCSConnector adcsConnector) {
        this.cAConnectorConfigService = cAConnectorConfigService;
        this.caConnectorConfigUtil = caConnectorConfigUtil;
        this.adcsConnector = adcsConnector;
    }


    /**
     * {@code POST  /pipelineViews} : Create a new CAConnectorConfig.
     *
     * @param caConnectorConfigView the CaConnectorConfigView to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new pipeline, or with status {@code 400 (Bad Request)} if the pipeline has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ca-connector-configViews")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CaConnectorConfigView> createPipeline(@Valid @RequestBody CaConnectorConfigView caConnectorConfigView) throws URISyntaxException {
        log.debug("REST request to save CAConnectorConfigView : {}", caConnectorConfigView);
        if (caConnectorConfigView.getId() != null) {
            throw new BadRequestAlertException("A new pipeline request cannot have an ID", ENTITY_NAME, "idexists");
        }

        CAConnectorConfig cfg = caConnectorConfigUtil.to(caConnectorConfigView);
        cAConnectorConfigService.save(cfg);
        return ResponseEntity.created(new URI("/api/ca-connector-configViews/" + cfg.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, cfg.getId().toString()))
            .body(caConnectorConfigView);
    }

    /**
     * {@code PUT  /ca-connector-configViews} : Updates an existing CaConnectorConfig.
     *
     * @param caConnectorConfigView the pipeline to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated CaConnectorConfig,
     * or with status {@code 400 (Bad Request)} if the CaConnectorConfig is not valid,
     * or with status {@code 500 (Internal Server Error)} if the CaConnectorConfig couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ca-connector-configViews")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CaConnectorConfigView> updatePipeline(@Valid @RequestBody CaConnectorConfigView caConnectorConfigView) throws URISyntaxException {
        log.debug("REST request to update CaConnectorConfig : {}", caConnectorConfigView);
        if (caConnectorConfigView.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        CAConnectorConfig cfg = caConnectorConfigUtil.to(caConnectorConfigView);

        cAConnectorConfigService.save(cfg);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cfg.getId().toString()))
            .body(caConnectorConfigView);
    }

    /**
     * {@code GET  /ca-connector-configViews} : get all the CaConnectorConfigs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of CaConnectorConfigViews in body.
     */
    @GetMapping("/ca-connector-configViews")
    public List<CaConnectorConfigView> getAllCaConnectorConfigs() {
        log.debug("REST request to get all CaConnectorConfigViews");
        List<CaConnectorConfigView> cvList = new ArrayList<>();
        for( CAConnectorConfig cfg: cAConnectorConfigService.findAll()){
            cvList.add(caConnectorConfigUtil.from(cfg));
        }
        return cvList;
    }

    /**
     * {@code GET  /ca-connector-configViews/:id} : get the "id" CaConnectorConfigView.
     *
     * @param id the id of the CaConnectorConfigView to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the pipeline, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ca-connector-configViews/{id}")
    public ResponseEntity<CaConnectorConfigView> getCaConnectorConfig(@PathVariable Long id) {
        log.debug("REST request to get CaConnectorConfigView : {}", id);
        Optional<CAConnectorConfig> caConnectorConfigOpt = cAConnectorConfigService.findOne(id);
        Optional<CaConnectorConfigView> cvOpt = Optional.empty();
        if( caConnectorConfigOpt.isPresent()){
            CAConnectorConfig cfg = caConnectorConfigOpt.get();
            cvOpt = Optional.of(caConnectorConfigUtil.from(cfg));
        }
        return ResponseUtil.wrapOrNotFound(cvOpt);
    }

    /**
     * {@code DELETE  /ca-connector-configViews/:id} : delete the "id" CaConnectorConfigView.
     *
     * @param id the id of the CaConnectorConfig to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ca-connector-configViews/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteCaConnectorConfig(@PathVariable Long id) {
        log.debug("REST request to delete CaConnectorConfig : {}", id);
        try {
            cAConnectorConfigService.delete(id);
        } catch( RuntimeException dive){
            log.debug("CaConnectorConfig deletion failed", dive);
            throw new IntegrityException("CaConnectorConfig already used");
        }
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }


    /**
     * {@code GET  /ca-connector-configViews/adcs/templates} : get all the cAConnectorConfigs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cAConnectorConfigs in body.
     */
    @PostMapping("/ca-connector-configViews/adcs/templates")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ADCSInstanceDetailsView> getADCSTemplates(@Valid @RequestBody CaConnectorConfigView caConnectorConfigView) {
        log.debug("REST request to get getADCSTemplates");
        if( caConnectorConfigView.getCaUrl() == null || caConnectorConfigView.getCaUrl().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        if( caConnectorConfigView.getAuthenticationParameter().getPlainSecret() == null || caConnectorConfigView.getAuthenticationParameter().getPlainSecret().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        ADCSInstanceDetails adcsInstanceDetails = adcsConnector.getInstanceDetails(
            caConnectorConfigView.getCaUrl(),
            caConnectorConfigView.getAuthenticationParameter().getPlainSecret(),
            caConnectorConfigView.getAuthenticationParameter().getSalt(),
            (int)caConnectorConfigView.getAuthenticationParameter().getCycles(),
            caConnectorConfigView.getAuthenticationParameter().getApiKeySalt(),
            (int)caConnectorConfigView.getAuthenticationParameter().getApiKeyCycles());

        if( adcsInstanceDetails == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(new ADCSInstanceDetailsView(adcsInstanceDetails));
    }
}
