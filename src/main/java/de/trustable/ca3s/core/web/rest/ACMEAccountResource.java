package de.trustable.ca3s.core.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.service.ACMEAccountService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.ACMEAccount}.
 */
@RestController
@RequestMapping("/api")
public class ACMEAccountResource {

    private final Logger log = LoggerFactory.getLogger(ACMEAccountResource.class);

    private static final String ENTITY_NAME = "aCMEAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ACMEAccountService aCMEAccountService;

    public ACMEAccountResource(ACMEAccountService aCMEAccountService) {
        this.aCMEAccountService = aCMEAccountService;
    }

    /**
     * {@code POST  /acme-accounts} : Create a new aCMEAccount.
     *
     * @param aCMEAccount the aCMEAccount to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new aCMEAccount, or with status {@code 400 (Bad Request)} if the aCMEAccount has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/acme-accounts")
    public ResponseEntity<ACMEAccount> createACMEAccount(@Valid @RequestBody ACMEAccount aCMEAccount) throws URISyntaxException {
        log.debug("REST request to save ACMEAccount : {}", aCMEAccount);
        if (aCMEAccount.getId() != null) {
            throw new BadRequestAlertException("A new aCMEAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ACMEAccount result = aCMEAccountService.save(aCMEAccount);
        return ResponseEntity.created(new URI("/api/acme-accounts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /acme-accounts} : Updates an existing aCMEAccount.
     *
     * @param aCMEAccount the aCMEAccount to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated aCMEAccount,
     * or with status {@code 400 (Bad Request)} if the aCMEAccount is not valid,
     * or with status {@code 500 (Internal Server Error)} if the aCMEAccount couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/acme-accounts")
    public ResponseEntity<ACMEAccount> updateACMEAccount(@Valid @RequestBody ACMEAccount aCMEAccount) throws URISyntaxException {
        log.debug("REST request to update ACMEAccount : {}", aCMEAccount);
        if (aCMEAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        ACMEAccount result = aCMEAccountService.save(aCMEAccount);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, aCMEAccount.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /acme-accounts} : get all the aCMEAccounts.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of aCMEAccounts in body.
     */
    @GetMapping("/acme-accounts")
    public List<ACMEAccount> getAllACMEAccounts() {
        log.debug("REST request to get all ACMEAccounts");
        return aCMEAccountService.findAll();
    }

    /**
     * {@code GET  /acme-accounts/:id} : get the "id" aCMEAccount.
     *
     * @param id the id of the aCMEAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the aCMEAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-accounts/{id}")
    public ResponseEntity<ACMEAccount> getACMEAccount(@PathVariable Long id) {
        log.debug("REST request to get ACMEAccount : {}", id);
        Optional<ACMEAccount> aCMEAccount = aCMEAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(aCMEAccount);
    }

    /**
     * {@code DELETE  /acme-accounts/:id} : delete the "id" aCMEAccount.
     *
     * @param id the id of the aCMEAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-accounts/{id}")
    public ResponseEntity<Void> deleteACMEAccount(@PathVariable Long id) {
        log.debug("REST request to delete ACMEAccount : {}", id);
        aCMEAccountService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
