package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.RequestProxyConfigService;
import de.trustable.ca3s.core.service.dto.RequestProxyConfigView;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
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
import java.net.URISyntaxException;
import java.util.*;

import static de.trustable.ca3s.core.service.util.ProtectedContentUtil.PLAIN_SECRET_PLACEHOLDER;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class RequestProxyConfigAdministration {

    private final Logger LOG = LoggerFactory.getLogger(RequestProxyConfigAdministration.class);

    private static final String ENTITY_NAME = "requestProxyConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;


    private final RequestProxyConfigService requestProxyConfigService;
    private final ProtectedContentUtil protUtil;
    private final ProtectedContentRepository protContentRepository;
    final private AuditService auditService;

    public RequestProxyConfigAdministration(RequestProxyConfigService requestProxyConfigService, ProtectedContentUtil protUtil, ProtectedContentRepository protContentRepository, AuditService auditService) {
        this.requestProxyConfigService = requestProxyConfigService;
        this.protUtil = protUtil;
        this.protContentRepository = protContentRepository;
        this.auditService = auditService;
    }


    /**
     * {@code POST  /request-proxy-config} : Create a new requestProxyConfig.
     *
     * @param requestProxyConfigView the requestProxyConfig to create.
     * @return Void with status {@code 201 (Created)} and with body the new requestProxyConfig, or with status {@code 400 (Bad Request)} if the requestProxyConfig has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/request-proxy-config")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Void> createRequestProxyConfig(@Valid @RequestBody RequestProxyConfigView requestProxyConfigView) throws URISyntaxException {
        LOG.debug("REST request to save RequestProxyConfig : {}", requestProxyConfigView);

        if (requestProxyConfigView.getId() != null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        RequestProxyConfig requestProxyConfig = from(requestProxyConfigView);

        logNewRequestProxyConfig(requestProxyConfig);

        requestProxyConfig = requestProxyConfigService.save(requestProxyConfig);


        if ((requestProxyConfigView.getPlainSecret() == null) || (requestProxyConfigView.getPlainSecret().trim().length() == 0)) {

        } else {
            LOG.debug("REST request to create RequestProxyConfig : PlainSecret != null");

            ProtectedContent protSecret = protUtil.createProtectedContent(requestProxyConfigView.getPlainSecret(),
                ProtectedContentType.PASSWORD,
                ContentRelationType.CONNECTION,
                requestProxyConfig.getId());

            protContentRepository.save(protSecret);

        }

        requestProxyConfig = requestProxyConfigService.save(requestProxyConfig);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, requestProxyConfig.getId().toString()))
            .build();
    }

    /**
     * {@code PUT  /request-proxy-config} : Updates an existing requestProxyConfig.
     *
     * @param requestProxyConfigView the requestProxyConfig to update.
     * @return Void with status {@code 200 (OK)} and with body the updated requestProxyConfig,
     * or with status {@code 400 (Bad Request)} if the requestProxyConfig is not valid,
     * or with status {@code 500 (Internal Server Error)} if the requestProxyConfig couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/request-proxy-config")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Void> updateRequestProxyConfigView(@Valid @RequestBody RequestProxyConfigView requestProxyConfigView) throws URISyntaxException {
        LOG.debug("REST request to update RequestProxyConfig : {}", requestProxyConfigView);
        if (requestProxyConfigView.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Optional<RequestProxyConfig> requestProxyConfigOptional = requestProxyConfigService.findOne(requestProxyConfigView.getId());
        if(requestProxyConfigOptional.isEmpty()){
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        RequestProxyConfig requestProxyConfig = requestProxyConfigOptional.get();
        requestProxyConfig.setName(requestProxyConfigView.getName());
        requestProxyConfig.setActive(requestProxyConfigView.getActive());
        requestProxyConfig.setRequestProxyUrl(requestProxyConfigView.getRequestProxyUrl());

        if ((requestProxyConfigView.getPlainSecret() == null) || (requestProxyConfigView.getPlainSecret().trim().length() == 0)) {

            LOG.warn("REST request to update RequestProxyConfig : cAConnectorConfig.getPlainSecret() == null no functionality!!");
        } else {
            String secret = requestProxyConfigView.getPlainSecret().trim();
            if (PLAIN_SECRET_PLACEHOLDER.equals(secret)) {
                LOG.debug("REST request to update RequestProxyConfig : PLAIN_SECRET_PLACEHOLDER.equals(RequestProxyConfig.getPlainSecret())");

                // no passphrase change received from the UI, just do nothing
                // leave the secret unchanged
            } else {
                LOG.debug("REST request to update RequestProxyConfig : PlainSecret modified");

                List<ProtectedContent> protectedContents = protUtil.retrieveProtectedContent(
                    ProtectedContentType.PASSWORD,
                    ContentRelationType.CONNECTION,
                    requestProxyConfigView.getId());

                if( protectedContents.stream().anyMatch(p -> secret.equals(protUtil.unprotectString(p.getContentBase64())))) {
                    LOG.debug("REST request to update RequestProxyConfig : PlainSecret already known");
                }else{
                    ProtectedContent protSecret = protUtil.createProtectedContent(requestProxyConfigView.getPlainSecret(),
                        ProtectedContentType.PASSWORD,
                        ContentRelationType.CONNECTION,
                        requestProxyConfigView.getId());
                    protContentRepository.save(protSecret);
                    LOG.debug("REST request to update RequestProxyConfig : new PlainSecret created");
                    auditService.saveAuditTrace(auditService.createAuditTraceRequestProxyConfigSecretChanged(requestProxyConfig));
                }
            }
        }

        logChangesRequestProxyConfig(requestProxyConfig);

        requestProxyConfigService.save(requestProxyConfig);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, requestProxyConfigView.getId().toString()))
            .build();
    }


    /**
     * {@code GET  /request-proxy-config} : get all the requestProxyConfig.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of requestProxyConfigViews in body.
     */
    @GetMapping("/request-proxy-config")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<RequestProxyConfigView> getAllRequestProxyConfig() {
        LOG.debug("REST request to get all RequestProxyConfig");

        List<RequestProxyConfigView> requestProxyConfigViewList = new ArrayList<>();
        for (RequestProxyConfig requestProxyConfig : requestProxyConfigService.findAll()) {
            requestProxyConfigViewList.add(from(requestProxyConfig));
        }
        return requestProxyConfigViewList;
    }


    /**
     * {@code GET  /request-proxy-config/:id} : get the "id" requestProxyConfig.
     *
     * @param id the id of the requestProxyConfig to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the requestProxyConfigView, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/request-proxy-config/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestProxyConfigView> getRequestProxyConfig(@PathVariable Long id) {
        LOG.debug("REST request to get RequestProxyConfigView : {}", id);
        Optional<RequestProxyConfig> requestProxyConfig = requestProxyConfigService.findOne(id);
        Optional<RequestProxyConfigView> optionalRequestProxyConfigView = Optional.empty();

        if (requestProxyConfig.isPresent()) {
            optionalRequestProxyConfigView = Optional.of(from(requestProxyConfig.get()));
        }
        return ResponseUtil.wrapOrNotFound(optionalRequestProxyConfigView);
    }


    private RequestProxyConfig from(RequestProxyConfigView requestProxyConfigView) {

        RequestProxyConfig requestProxyConfig = new RequestProxyConfig();
        requestProxyConfig.setId(requestProxyConfigView.getId());
        requestProxyConfig.setName(requestProxyConfigView.getName());
        requestProxyConfig.setActive(requestProxyConfigView.getActive());
        requestProxyConfig.setRequestProxyUrl(requestProxyConfigView.getRequestProxyUrl());

        return requestProxyConfig;
    }

    private RequestProxyConfigView from(RequestProxyConfig requestProxyConfig){
        RequestProxyConfigView requestProxyConfigView = new RequestProxyConfigView();
        requestProxyConfigView.setId(requestProxyConfig.getId());
        requestProxyConfigView.setName(requestProxyConfig.getName());
        requestProxyConfigView.setRequestProxyUrl(requestProxyConfig.getRequestProxyUrl());
        requestProxyConfigView.setActive(requestProxyConfig.getActive());
        requestProxyConfigView.setPlainSecret(PLAIN_SECRET_PLACEHOLDER);

        return requestProxyConfigView;
    }


    void logNewRequestProxyConfig(RequestProxyConfig requestProxyConfig) {

        logDiff("Name", "", requestProxyConfig.getName(), requestProxyConfig);
        logDiff("RequestProxyUrl", "", requestProxyConfig.getRequestProxyUrl(), requestProxyConfig);
        logDiff("Active",
            "",
            requestProxyConfig.getActive() == null ? "": requestProxyConfig.getActive().toString(),
            requestProxyConfig);
    }

    void logChangesRequestProxyConfig(RequestProxyConfig requestProxyConfig) {

        Optional<RequestProxyConfig> requestProxyConfigOptional = requestProxyConfigService.findOne(requestProxyConfig.getId());
        if (requestProxyConfigOptional.isPresent()) {
            RequestProxyConfig oldRequestProxyConfig = requestProxyConfigOptional.get();

            logDiff("Name", oldRequestProxyConfig.getName(), requestProxyConfig.getName(), requestProxyConfig);
            logDiff("RequestProxyUrl", oldRequestProxyConfig.getRequestProxyUrl(), requestProxyConfig.getRequestProxyUrl(), requestProxyConfig);
            logDiff("Active",
                oldRequestProxyConfig.getActive() == null ? "": oldRequestProxyConfig.getActive().toString(),
                requestProxyConfig.getActive() == null ? "": requestProxyConfig.getActive().toString(),
                requestProxyConfig);
        }
    }


    void logDiff( final String attributeName, final String oldVal, final String newVal, final RequestProxyConfig cAConnectorConfig){
        if( oldVal != null ){
            if( !oldVal.equals(newVal)) {
                auditService.saveAuditTrace(auditService.createAuditTraceRequestProxyConfigChange(attributeName, oldVal, newVal, cAConnectorConfig));
            }
        }else{
            if( newVal != null) {
                auditService.saveAuditTrace(auditService.createAuditTraceRequestProxyConfigChange(attributeName, "", newVal, cAConnectorConfig));
            }

        }
    }
}
