package de.trustable.ca3s.core.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AcmeChallengeService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

import de.trustable.ca3s.core.service.util.JWSService;
import de.trustable.ca3s.core.web.rest.acme.ChallengeController;
import de.trustable.ca3s.core.web.rest.data.AcmeChallengeValidation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeChallenge}.
 */
@RestController
@RequestMapping("/api")
public class AcmeChallengeResource {

    private final Logger log = LoggerFactory.getLogger(AcmeChallengeResource.class);

    private static final String ENTITY_NAME = "acmeChallenge";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeChallengeService acmeChallengeService;
    private final ChallengeController challengeController;
    private final JWSService jwsService;
    private final ObjectMapper objectMapper;

    public AcmeChallengeResource(AcmeChallengeService acmeChallengeService, ChallengeController challengeController, JWSService jwsService, ObjectMapper objectMapper) {
        this.acmeChallengeService = acmeChallengeService;
        this.challengeController = challengeController;
        this.jwsService = jwsService;
        this.objectMapper = objectMapper;
    }

    /**
     * {@code POST  /acme-challenges} : Create a new acmeChallenge.
     *
     * @param acmeChallenge the acmeChallenge to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeChallenge, or with status {@code 400 (Bad Request)} if the acmeChallenge has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-challenges")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeChallenge> createAcmeChallenge(@Valid @RequestBody AcmeChallenge acmeChallenge) throws URISyntaxException {
        log.debug("REST request to save AcmeChallenge : {}", acmeChallenge);
        if (acmeChallenge.getId() != null) {
            throw new BadRequestAlertException("A new acmeChallenge cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeChallenge result = acmeChallengeService.save(acmeChallenge);
        return ResponseEntity.created(new URI("/api/acme-challenges/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-challenges} : Updates an existing acmeChallenge.
     *
     * @param acmeChallenge the acmeChallenge to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeChallenge,
     * or with status {@code 400 (Bad Request)} if the acmeChallenge is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeChallenge couldn't be updated.
     */
    @PutMapping("/acme-challenges")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeChallenge> updateAcmeChallenge(@Valid @RequestBody AcmeChallenge acmeChallenge) {
        log.debug("REST request to update AcmeChallenge : {}", acmeChallenge);
        if (acmeChallenge.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeChallenge result = acmeChallengeService.save(acmeChallenge);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeChallenge.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-challenges} : get all the acmeChallenges.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeChallenges in body.
     */
    @GetMapping("/acme-challenges")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<AcmeChallenge> getAllAcmeChallenges() {
        log.debug("REST request to get all AcmeChallenges");
        return acmeChallengeService.findAll();
    }

    /**
     * {@code GET  /acme-challenges/:id} : get the "id" acmeChallenge.
     *
     * @param id the id of the acmeChallenge to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeChallenge, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-challenges/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeChallenge> getAcmeChallenge(@PathVariable Long id) {
        log.debug("REST request to get AcmeChallenge : {}", id);
        Optional<AcmeChallenge> acmeChallenge = acmeChallengeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeChallenge);
    }

    /**
     * {@code DELETE  /acme-challenges/:id} : delete the "id" acmeChallenge.
     *
     * @param id the id of the acmeChallenge to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-challenges/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAcmeChallenge(@PathVariable Long id) {
        log.debug("REST request to delete AcmeChallenge : {}", id);
        acmeChallengeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }


    /**
     * {@code POST  /acme-challenges/pending/request-proxy-configs/{requestProxyId}} : get all pending AcmeChallenges for a given realm.
     * @param requestProxyId of the pending acmeChallenges to retrieve.
     * @param body a JWS containing an AcmeChallengeValidation object.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeChallenges in body.
     */
    @PostMapping("/acme-challenges/pending/request-proxy-configs/{requestProxyId}")
    public ResponseEntity<?> postPendingAcmeChallenges(@Parameter(description = "AcmeChallengeValidation wrapped in a JWS") @Valid @RequestBody String body,
                                                         @RequestHeader(value= HttpHeaders.ACCEPT, required=false) String accept,
                                                         @PathVariable Long requestProxyId) {

        log.debug("REST request to retrieve all pending AcmeChallenges for a given proxy");

        if (accept != null && accept.contains("application/json")) {
            try {

                log.debug("incoming JWT " + body);
                jwsService.checkJWT(body, requestProxyId.intValue());
                return ResponseEntity.ok(acmeChallengeService.findPendingByRequestProxy(requestProxyId));

            } catch (GeneralSecurityException e) {
                log.error("Couldn't process request", e);
                return ResponseEntity.internalServerError().build();
            } catch (JOSEException | ParseException e) {
                log.error("Couldn't process JOSE element", e);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }else {
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

    }


    /**
     * {@code POST  /acme-challenges/validation} : process a proxy-validated acmeChallenge.
     *
     * @param body a JWS containing an AcmeChallengeValidation object.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeChallenge, or with status {@code 400 (Bad Request)} if the acmeChallenge has already an ID.
     */
    @PostMapping("/acme-challenges/validation")
    public ResponseEntity<Void> processChallengeValidation(@Parameter(description = "AcmeChallengeValidation wrapped in a JWS") @Valid @RequestBody String body,
                                                           @RequestHeader(value= HttpHeaders.ACCEPT, required=false) String accept) {
        if (accept != null && accept.contains("application/json")) {
            try {

                log.debug("incoming JWS " + body);
                String payload = jwsService.getJWSPayload(body);
                AcmeChallengeValidation acmeChallengeValidation = objectMapper.readValue(payload, AcmeChallengeValidation.class);

                return challengeController.checkChallengeValidation(acmeChallengeValidation);

            } catch (IOException | GeneralSecurityException e) {
                log.error("Couldn't process request", e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } catch (JOSEException | ParseException e) {
                log.error("Couldn't process JOSE element", e);
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);            }
        }else {
            return new ResponseEntity<>(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        }

    }

}
