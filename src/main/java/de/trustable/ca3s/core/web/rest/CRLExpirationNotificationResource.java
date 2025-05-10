package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.repository.CRLExpirationNotificationRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.CRLExpirationNotificationService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CRLExpirationNotification}.
 */
@RestController
@RequestMapping("/api")
public class CRLExpirationNotificationResource {

    private final Logger log = LoggerFactory.getLogger(CRLExpirationNotificationViewResource.class);

    private static final String ENTITY_NAME = "cRLExpirationNotification";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CRLExpirationNotificationService cRLExpirationNotificationService;

    private final CRLExpirationNotificationRepository cRLExpirationNotificationRepository;

    public CRLExpirationNotificationResource(
        CRLExpirationNotificationService cRLExpirationNotificationService,
        CRLExpirationNotificationRepository cRLExpirationNotificationRepository
    ) {
        this.cRLExpirationNotificationService = cRLExpirationNotificationService;
        this.cRLExpirationNotificationRepository = cRLExpirationNotificationRepository;
    }

    /**
     * {@code POST  /crl-expiration-notifications} : Create a new cRLExpirationNotification.
     *
     * @param cRLExpirationNotification the cRLExpirationNotification to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cRLExpirationNotification, or with status {@code 400 (Bad Request)} if the cRLExpirationNotification has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/crl-expiration-notifications")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CRLExpirationNotification> createCRLExpirationNotification(
        @Valid @RequestBody CRLExpirationNotification cRLExpirationNotification
    ) throws URISyntaxException {
        log.debug("REST request to save CRLExpirationNotification : {}", cRLExpirationNotification);
        if (cRLExpirationNotification.getId() != null) {
            throw new BadRequestAlertException("A new cRLExpirationNotification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CRLExpirationNotification result = cRLExpirationNotificationService.save(cRLExpirationNotification);
        return ResponseEntity
            .created(new URI("/api/crl-expiration-notifications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }
    /**
     * {@code POST  /crl-expiration-notifications/certificate} : Create a new cRLExpirationNotification.
     *
     * @param certificateId a certificate id to derive the details.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new cRLExpirationNotification, or with status {@code 400 (Bad Request)} if the cRLExpirationNotification has already an ID.
     */
    @PostMapping("/crl-expiration-notifications/certificate")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<List<CRLExpirationNotification>> createCRLExpirationNotification(
        @Valid @RequestBody Long certificateId
    ) {
        log.debug("REST request to create CRLExpirationNotification by certificate id: {}", certificateId);

        if (certificateId == null) {
            throw new BadRequestAlertException("Invalid certificate id", ENTITY_NAME, "idnull");
        }
        List<CRLExpirationNotification> result = cRLExpirationNotificationService.createByCertificateId(certificateId);

        return ResponseEntity
            .ok()
            .body(result);
    }

    /**
     * {@code PUT  /crl-expiration-notifications/:id} : Updates an existing cRLExpirationNotification.
     *
     * @param id the id of the cRLExpirationNotification to save.
     * @param cRLExpirationNotification the cRLExpirationNotification to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated cRLExpirationNotification,
     * or with status {@code 400 (Bad Request)} if the cRLExpirationNotification is not valid,
     * or with status {@code 500 (Internal Server Error)} if the cRLExpirationNotification couldn't be updated.
     */
    @PutMapping("/crl-expiration-notifications/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CRLExpirationNotification> updateCRLExpirationNotification(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CRLExpirationNotification cRLExpirationNotification
    ) {
        log.debug("REST request to update CRLExpirationNotification : {}, {}", id, cRLExpirationNotification);
        if (cRLExpirationNotification.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, cRLExpirationNotification.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!cRLExpirationNotificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CRLExpirationNotification result = cRLExpirationNotificationService.update(cRLExpirationNotification);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, cRLExpirationNotification.getId().toString()))
            .body(result);
    }


    /**
     * {@code GET  /crl-expiration-notifications} : get all the cRLExpirationNotifications.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of cRLExpirationNotifications in body.
     */
    @GetMapping("/crl-expiration-notifications")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<CRLExpirationNotification> getAllCRLExpirationNotifications() {
        log.debug("REST request to get all CRLExpirationNotifications");
        return cRLExpirationNotificationService.findAll();
    }

    /**
     * {@code GET  /crl-expiration-notifications/:id} : get the "id" cRLExpirationNotification.
     *
     * @param id the id of the cRLExpirationNotification to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cRLExpirationNotification, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/crl-expiration-notifications/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<CRLExpirationNotification> getCRLExpirationNotification(@PathVariable Long id) {
        log.debug("REST request to get CRLExpirationNotification : {}", id);
        Optional<CRLExpirationNotification> cRLExpirationNotification = cRLExpirationNotificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(cRLExpirationNotification);
    }

    /**
     * {@code DELETE  /crl-expiration-notifications/:id} : delete the "id" cRLExpirationNotification.
     *
     * @param id the id of the cRLExpirationNotification to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/crl-expiration-notifications/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteCRLExpirationNotification(@PathVariable Long id) {
        log.debug("REST request to delete CRLExpirationNotification : {}", id);
        cRLExpirationNotificationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
