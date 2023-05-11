package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.TimedElementNotification;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.repository.TimedElementNotificationRepository;
import de.trustable.ca3s.core.service.TimedElementNotificationService;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.TimedElementNotification}.
 */
@RestController
@RequestMapping("/api")
public class TimedElementNotificationResource {

    private final Logger log = LoggerFactory.getLogger(TimedElementNotificationResource.class);

    private static final String ENTITY_NAME = "timedElementNotification";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TimedElementNotificationService timedElementNotificationService;

    private final TimedElementNotificationRepository timedElementNotificationRepository;

    public TimedElementNotificationResource(
        TimedElementNotificationService timedElementNotificationService,
        TimedElementNotificationRepository timedElementNotificationRepository
    ) {
        this.timedElementNotificationService = timedElementNotificationService;
        this.timedElementNotificationRepository = timedElementNotificationRepository;
    }

    /**
     * {@code POST  /timed-element-notifications} : Create a new timedElementNotification.
     *
     * @param timedElementNotification the timedElementNotification to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new timedElementNotification, or with status {@code 400 (Bad Request)} if the timedElementNotification has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/timed-element-notifications")
    public ResponseEntity<TimedElementNotification> createTimedElementNotification(
        @Valid @RequestBody TimedElementNotification timedElementNotification
    ) throws URISyntaxException {
        log.debug("REST request to save TimedElementNotification : {}", timedElementNotification);
        if (timedElementNotification.getId() != null) {
            throw new BadRequestAlertException("A new timedElementNotification cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TimedElementNotification result = timedElementNotificationService.save(timedElementNotification);
        return ResponseEntity
            .created(new URI("/api/timed-element-notifications/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /timed-element-notifications/:id} : Updates an existing timedElementNotification.
     *
     * @param id the id of the timedElementNotification to save.
     * @param timedElementNotification the timedElementNotification to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated timedElementNotification,
     * or with status {@code 400 (Bad Request)} if the timedElementNotification is not valid,
     * or with status {@code 500 (Internal Server Error)} if the timedElementNotification couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/timed-element-notifications/{id}")
    public ResponseEntity<TimedElementNotification> updateTimedElementNotification(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TimedElementNotification timedElementNotification
    ) throws URISyntaxException {
        log.debug("REST request to update TimedElementNotification : {}, {}", id, timedElementNotification);
        if (timedElementNotification.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, timedElementNotification.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!timedElementNotificationRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        TimedElementNotification result = timedElementNotificationService.update(timedElementNotification);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, timedElementNotification.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /timed-element-notifications} : get all the timedElementNotifications.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of timedElementNotifications in body.
     */
    @GetMapping("/timed-element-notifications")
    public List<TimedElementNotification> getAllTimedElementNotifications() {
        log.debug("REST request to get all TimedElementNotifications");
        return timedElementNotificationService.findAll();
    }

    /**
     * {@code GET  /timed-element-notifications/:id} : get the "id" timedElementNotification.
     *
     * @param id the id of the timedElementNotification to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the timedElementNotification, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/timed-element-notifications/{id}")
    public ResponseEntity<TimedElementNotification> getTimedElementNotification(@PathVariable Long id) {
        log.debug("REST request to get TimedElementNotification : {}", id);
        Optional<TimedElementNotification> timedElementNotification = timedElementNotificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(timedElementNotification);
    }

    /**
     * {@code DELETE  /timed-element-notifications/:id} : delete the "id" timedElementNotification.
     *
     * @param id the id of the timedElementNotification to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/timed-element-notifications/{id}")
    public ResponseEntity<Void> deleteTimedElementNotification(@PathVariable Long id) {
        log.debug("REST request to delete TimedElementNotification : {}", id);
        timedElementNotificationService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
