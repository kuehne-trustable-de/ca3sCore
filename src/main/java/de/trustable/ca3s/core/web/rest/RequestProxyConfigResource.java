package de.trustable.ca3s.core.web.rest;

import com.nimbusds.jose.JOSEException;
import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.RequestProxyConfigService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

import de.trustable.ca3s.core.service.dto.RemoteRequestProxyConfigView;
import de.trustable.ca3s.core.service.util.JWSService;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.RequestProxyConfig}.
 */
@RestController
@RequestMapping("/api")
public class RequestProxyConfigResource {

    private final Logger log = LoggerFactory.getLogger(RequestProxyConfigResource.class);

    private static final String ENTITY_NAME = "requestProxyConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RequestProxyConfigService requestProxyConfigService;
    private final JWSService jwsService;

    public RequestProxyConfigResource(RequestProxyConfigService requestProxyConfigService, JWSService jwsService) {
        this.requestProxyConfigService = requestProxyConfigService;
        this.jwsService = jwsService;
    }

    /**
     * {@code POST  /request-proxy-configs} : Create a new requestProxyConfig.
     *
     * @param requestProxyConfig the requestProxyConfig to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new requestProxyConfig, or with status {@code 400 (Bad Request)} if the requestProxyConfig has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/request-proxy-configs")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestProxyConfig> createRequestProxyConfig(@Valid @RequestBody RequestProxyConfig requestProxyConfig) throws URISyntaxException {
        log.debug("REST request to save RequestProxyConfig : {}", requestProxyConfig);
        if (requestProxyConfig.getId() != null) {
            throw new BadRequestAlertException("A new requestProxyConfig cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RequestProxyConfig result = requestProxyConfigService.save(requestProxyConfig);
        return ResponseEntity.created(new URI("/api/request-proxy-configs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /request-proxy-configs} : Updates an existing requestProxyConfig.
     *
     * @param requestProxyConfig the requestProxyConfig to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated requestProxyConfig,
     * or with status {@code 400 (Bad Request)} if the requestProxyConfig is not valid,
     * or with status {@code 500 (Internal Server Error)} if the requestProxyConfig couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/request-proxy-configs")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestProxyConfig> updateRequestProxyConfig(@Valid @RequestBody RequestProxyConfig requestProxyConfig) throws URISyntaxException {
        log.debug("REST request to update RequestProxyConfig : {}", requestProxyConfig);
        if (requestProxyConfig.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RequestProxyConfig result = requestProxyConfigService.save(requestProxyConfig);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, requestProxyConfig.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /request-proxy-configs} : get all the requestProxyConfigs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of requestProxyConfigs in body.
     */
    @GetMapping("/request-proxy-configs")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<RequestProxyConfig> getAllRequestProxyConfigs() {
        log.debug("REST request to get all RequestProxyConfigs");
        return requestProxyConfigService.findAll();
    }

    /**
     * {@code GET  /request-proxy-configs/:id} : get the "id" requestProxyConfig.
     *
     * @param id the id of the requestProxyConfig to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the requestProxyConfig, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/request-proxy-configs/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestProxyConfig> getRequestProxyConfig(@PathVariable Long id) {
        log.debug("REST request to get RequestProxyConfig : {}", id);
        Optional<RequestProxyConfig> requestProxyConfig = requestProxyConfigService.findOne(id);
        return ResponseUtil.wrapOrNotFound(requestProxyConfig);
    }

    /**
     * {@code GET  /request-proxy-configs/remote-config/:id} : get the "id" requestProxyConfig.
     *
     * @param requestProxyId the id of the requestProxyConfig to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the requestProxyConfig, or with status {@code 404 (Not Found)}.
     */
    @PostMapping("/request-proxy-configs/remote-config/{requestProxyId}")
    @Transactional
    public ResponseEntity<RemoteRequestProxyConfigView> postRequestProxyConfig(@Parameter(description = "AcmeChallengeValidation wrapped in a JWS") @Valid @RequestBody String body,
                                                                               @RequestHeader(value= HttpHeaders.ACCEPT, required=false) String accept,
                                                                               @PathVariable Long requestProxyId) {
        log.debug("REST request to get RequestProxyConfig : {}", requestProxyId);
        if (accept != null && accept.contains("application/json")) {
            try {

                log.debug("incoming JWT " + body);
                jwsService.checkJWT(body, requestProxyId.intValue());
                Optional<RequestProxyConfig> requestProxyConfigOptional = requestProxyConfigService.findOne(requestProxyId);
                Optional<RemoteRequestProxyConfigView> remoteRequestProxyConfigViewOptional = Optional.empty();
                if (requestProxyConfigOptional.isPresent()) {
                    remoteRequestProxyConfigViewOptional = Optional.of(
                        new RemoteRequestProxyConfigView(requestProxyConfigOptional.get()));
                }
                return ResponseUtil.wrapOrNotFound(remoteRequestProxyConfigViewOptional);

            } catch (GeneralSecurityException e) {
                log.error("Couldn't process request", e);
                return ResponseEntity.internalServerError().build();
            } catch (JOSEException | ParseException e) {
                log.error("Couldn't process JOSE element", e);
                return ResponseEntity.badRequest().build();
            }
        } else {
            return new ResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }

    /**
     * {@code POST  /acme-challenges/pending/request-proxy-configs/{requestProxyId} : get all pending AcmeChallenges for a given realm.
     *
     * @param requestProxyId of the pending acmeChallenges to retrieve.
     * @param body a JWS containing an AcmeChallengeValidation object.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeChallenges in body.

    @PostMapping("/request-proxy-configs/{requestProxyId}")
    public ResponseEntity<?> postPendingAcmeChallenges(@Parameter(description = "AcmeChallengeValidation wrapped in a JWS") @Valid @RequestBody String body,
                                                       @RequestHeader(value= HttpHeaders.ACCEPT, required=false) String accept,
                                                       @PathVariable Long requestProxyId) {

        log.debug("REST request to retrieve request proxy config");

        if (accept != null && accept.contains("application/json")) {
            try {

                log.debug("incoming JWT " + body);
                jwsService.checkJWT(body, requestProxyId.intValue());
                Optional<RequestProxyConfig> requestProxyConfig = requestProxyConfigService.findOne(requestProxyId);
                return ResponseUtil.wrapOrNotFound(requestProxyConfig);

            } catch (GeneralSecurityException e) {
                log.error("Couldn't process request", e);
                return ResponseEntity.internalServerError().build();
            } catch (JOSEException | ParseException e) {
                log.error("Couldn't process JOSE element", e);
                return ResponseEntity.badRequest().build();
            }
        } else {
            return new ResponseEntity(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }
    }
*/

    /**
     * {@code DELETE  /request-proxy-configs/:id} : delete the "id" requestProxyConfig.
     *
     * @param id the id of the requestProxyConfig to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/request-proxy-configs/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteRequestProxyConfig(@PathVariable Long id) {
        log.debug("REST request to delete RequestProxyConfig : {}", id);
        requestProxyConfigService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
