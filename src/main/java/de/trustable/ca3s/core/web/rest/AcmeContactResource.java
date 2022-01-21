package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.service.AcmeContactService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

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
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeContact}.
 */
@RestController
@RequestMapping("/api")
public class AcmeContactResource {

    private final Logger log = LoggerFactory.getLogger(AcmeContactResource.class);

    private static final String ENTITY_NAME = "acmeContact";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeContactService acmeContactService;

    public AcmeContactResource(AcmeContactService acmeContactService) {
        this.acmeContactService = acmeContactService;
    }

    /**
     * {@code POST  /acme-contacts} : Create a new acmeContact.
     *
     * @param acmeContact the acmeContact to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new acmeContact, or with status {@code 400 (Bad Request)} if the acmeContact has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-contacts")
    public ResponseEntity<AcmeContact> createAcmeContact(@Valid @RequestBody AcmeContact acmeContact) throws URISyntaxException {
        log.debug("REST request to save AcmeContact : {}", acmeContact);
        if (acmeContact.getId() != null) {
            throw new BadRequestAlertException("A new acmeContact cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeContact result = acmeContactService.save(acmeContact);
        return ResponseEntity.created(new URI("/api/acme-contacts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-contacts} : Updates an existing acmeContact.
     *
     * @param acmeContact the acmeContact to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated acmeContact,
     * or with status {@code 400 (Bad Request)} if the acmeContact is not valid,
     * or with status {@code 500 (Internal Server Error)} if the acmeContact couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-contacts")
    public ResponseEntity<AcmeContact> updateAcmeContact(@Valid @RequestBody AcmeContact acmeContact) throws URISyntaxException {
        log.debug("REST request to update AcmeContact : {}", acmeContact);
        if (acmeContact.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeContact result = acmeContactService.save(acmeContact);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, acmeContact.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-contacts} : get all the acmeContacts.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of acmeContacts in body.
     */
    @GetMapping("/acme-contacts")
    public List<AcmeContact> getAllAcmeContacts() {
        log.debug("REST request to get all AcmeContacts");
        return acmeContactService.findAll();
    }

    /**
     * {@code GET  /acme-contacts/:id} : get the "id" acmeContact.
     *
     * @param id the id of the acmeContact to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the acmeContact, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-contacts/{id}")
    public ResponseEntity<AcmeContact> getAcmeContact(@PathVariable Long id) {
        log.debug("REST request to get AcmeContact : {}", id);
        Optional<AcmeContact> acmeContact = acmeContactService.findOne(id);
        return ResponseUtil.wrapOrNotFound(acmeContact);
    }

    /**
     * {@code DELETE  /acme-contacts/:id} : delete the "id" acmeContact.
     *
     * @param id the id of the acmeContact to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-contacts/{id}")
    public ResponseEntity<Void> deleteAcmeContact(@PathVariable Long id) {
        log.debug("REST request to delete AcmeContact : {}", id);
        acmeContactService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
