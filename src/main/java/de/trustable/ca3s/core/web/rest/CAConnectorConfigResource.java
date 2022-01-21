package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.Interval;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.CAConnectorConfigService;
import de.trustable.ca3s.core.service.dto.CAConnectorStatus;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CAConnectorConfig}.
 */
@RestController
@RequestMapping("/api")
public class CAConnectorConfigResource {

    public static final String PLAIN_SECRET_PLACEHOLDER = "******";

	private final Logger log = LoggerFactory.getLogger(CAConnectorConfigResource.class);

    private static final String ENTITY_NAME = "cAConnectorConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

	private ProtectedContentUtil protUtil;

	private ProtectedContentRepository protContentRepository;

	private CAConnectorConfigRepository caConfigRepository;

    private CaConnectorAdapter caConnectorAdapter;

    private AuditService auditService;


    private final CAConnectorConfigService cAConnectorConfigService;

    public CAConnectorConfigResource(CAConnectorConfigService cAConnectorConfigService,
        ProtectedContentUtil protUtil,
        ProtectedContentRepository protContentRepository,
        CAConnectorConfigRepository caConfigRepository,
        CaConnectorAdapter caConnectorAdapter,
        AuditService auditService
    ) {
        this.protUtil = protUtil;
        this.protContentRepository = protContentRepository;
        this.caConfigRepository = caConfigRepository;
        this.cAConnectorConfigService = cAConnectorConfigService;
        this.caConnectorAdapter = caConnectorAdapter;
        this.auditService = auditService;
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

        if((cAConnectorConfig.getPlainSecret() == null) || (cAConnectorConfig.getPlainSecret().trim().length() == 0))  {
            log.debug("REST request to save CAConnectorConfig : cAConnectorConfig.getPlainSecret() == null");
	        cAConnectorConfig.setSecret(null);
	        cAConnectorConfig.setPlainSecret("");
        }else {
        	if( protUtil == null) {
                System.err.println("Autowired 'protUtil' failed ...");
        	}

	        ProtectedContent protSecret = protUtil.createProtectedContent(cAConnectorConfig.getPlainSecret(), ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, -1L);
	        protContentRepository.save(protSecret);
	        cAConnectorConfig.setSecret(protSecret);
	        cAConnectorConfig.setPlainSecret(PLAIN_SECRET_PLACEHOLDER);
        }


        CAConnectorConfig result = cAConnectorConfigService.save(cAConnectorConfig);

        auditService.saveAuditTrace( auditService.createAuditTraceCAConfigCreated(cAConnectorConfig));

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
    @Transactional
    @PutMapping("/ca-connector-configs")
    public ResponseEntity<CAConnectorConfig> updateCAConnectorConfig(@Valid @RequestBody CAConnectorConfig cAConnectorConfig) throws URISyntaxException {
        log.debug("REST request to update CAConnectorConfig : {}", cAConnectorConfig);
        if (cAConnectorConfig.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }


        if((cAConnectorConfig.getPlainSecret() == null) || (cAConnectorConfig.getPlainSecret().trim().length() == 0))  {

        	log.debug("REST request to update CAConnectorConfig : cAConnectorConfig.getPlainSecret() == null");

    		if(cAConnectorConfig.getSecret() != null ) {

            	log.debug("REST request to update CAConnectorConfig : protContentRepository.delete() ");
                auditService.saveAuditTrace( auditService.createAuditTraceCAConfigSecretChanged(cAConnectorConfig));

                protContentRepository.delete(cAConnectorConfig.getSecret());
    		}

	        cAConnectorConfig.setSecret(null);
	        cAConnectorConfig.setPlainSecret("");
        } else {
        	if( PLAIN_SECRET_PLACEHOLDER.equals(cAConnectorConfig.getPlainSecret().trim())) {
	        	log.debug("REST request to update CAConnectorConfig : PLAIN_SECRET_PLACEHOLDER.equals(cAConnectorConfig.getPlainSecret())");

	        	// no passphrase change received from the UI, just do nothing
	        	// leave the secret unchanged
	        	cAConnectorConfig.setSecret(caConfigRepository.getOne(cAConnectorConfig.getId()).getSecret());
        	}else {
	        	log.debug("REST request to update CAConnectorConfig : PlainSecret modified");
                auditService.saveAuditTrace( auditService.createAuditTraceCAConfigSecretChanged(cAConnectorConfig));

        		if(cAConnectorConfig.getSecret() != null ) {
                	log.debug("REST request to update CAConnectorConfig : protContentRepository.delete() ");
        			protContentRepository.delete(cAConnectorConfig.getSecret());
        		}

                if( protUtil == null) {
                    System.err.println("Autowired 'protUtil' failed ...");
                }

                ProtectedContent protSecret = protUtil.createProtectedContent(cAConnectorConfig.getPlainSecret(), ProtectedContentType.PASSWORD, ContentRelationType.CONNECTION, cAConnectorConfig.getId());
                protContentRepository.save(protSecret);

                cAConnectorConfig.setSecret(protSecret);
    	        cAConnectorConfig.setPlainSecret(PLAIN_SECRET_PLACEHOLDER);
        	}
        }

        if( cAConnectorConfig.isDefaultCA()) {
        	for( CAConnectorConfig other: caConfigRepository.findAll() ) {
        		if( other.getId() != cAConnectorConfig.getId() &&
        				other.isDefaultCA()) {

                	log.debug("REST request to update CAConnectorConfig : remove 'defaultCA' flag from caConfig {} ", other.getId());
        			other.setDefaultCA(false);
        			caConfigRepository.save(other);
        		}
        	}
        }

        logChangesCAConnectorConfig(cAConnectorConfig);

        CAConnectorConfig result = cAConnectorConfigService.save(cAConnectorConfig);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cAConnectorConfig.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /ca-connector-configs/status} : get all elements able to create a certificate.
     *
     * @return list of {@link CAStatus} .
     */
    @GetMapping("/ca-connector-configs/status")
    @PreAuthorize("isAuthenticated()")
    public List<CAConnectorStatus> getCAConnectorStatus() {
        return caConnectorAdapter.getCAConnectorStatus();
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
        Optional<CAConnectorConfig> optConnector = cAConnectorConfigService.findOne(id);
        if( optConnector.isPresent()){
            auditService.saveAuditTrace( auditService.createAuditTraceCAConfigDeleted(optConnector.get()));
        }

        cAConnectorConfigService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    void logChangesCAConnectorConfig(CAConnectorConfig cAConnectorConfig) {

        Optional<CAConnectorConfig> optConnector = cAConnectorConfigService.findOne(cAConnectorConfig.getId());
        if (optConnector.isPresent()) {
            CAConnectorConfig oldConnector = optConnector.get();
            logDiff("Name", oldConnector.getName(), cAConnectorConfig.getName(), cAConnectorConfig);
            logDiff("CaConnectorType", oldConnector.getCaConnectorType().name(), cAConnectorConfig.getCaConnectorType().name(), cAConnectorConfig);
            logDiff("CaUrl", oldConnector.getCaUrl(), cAConnectorConfig.getCaUrl(), cAConnectorConfig);
            logDiff("Interval", oldConnector.getInterval(), cAConnectorConfig.getInterval(), cAConnectorConfig);
            logDiff("PollingOffset", "" + oldConnector.getPollingOffset(), "" + cAConnectorConfig.getPollingOffset(), cAConnectorConfig);
            logDiff("Selector", oldConnector.getSelector(), cAConnectorConfig.getSelector(), cAConnectorConfig);
            logDiff("TrustSelfSigned", oldConnector.getTrustSelfsignedCertificates().toString(), cAConnectorConfig.getTrustSelfsignedCertificates().toString(), cAConnectorConfig);
        }
    }

    void logDiff(final String attributeName, final Interval oldVal, final Interval newVal, final CAConnectorConfig cAConnectorConfig){
        if(( oldVal == null ) && ( newVal == null)){
            return;
        }
        String oldString = (oldVal == null) ?"":oldVal.toString();
        String newString = (newVal == null) ?"":newVal.toString();

        logDiff( attributeName, oldString, newString, cAConnectorConfig);
    }

    void logDiff( final String attributeName, final String oldVal, final String newVal, final CAConnectorConfig cAConnectorConfig){
        if( oldVal != null ){
            if( !oldVal.equals(newVal)) {
                auditService.saveAuditTrace(auditService.createAuditTraceCAConfigCreatedChange(attributeName, oldVal, newVal, cAConnectorConfig));
            }
        }else{
            if( newVal != null) {
                auditService.saveAuditTrace(auditService.createAuditTraceCAConfigCreatedChange(attributeName, "", newVal, cAConnectorConfig));
            }

        }
    }

}
