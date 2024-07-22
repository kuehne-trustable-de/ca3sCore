package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AcmeAccountService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

import de.trustable.ca3s.core.web.rest.data.AcmeAccountStatusAdministration;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.AcmeAccount}.
 */
@RestController
@RequestMapping("/api")
public class AcmeAccountResource {

    private final Logger log = LoggerFactory.getLogger(AcmeAccountResource.class);

    private static final String ENTITY_NAME = "aCMEAccount";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final AcmeAccountService aCMEAccountService;

    public AcmeAccountResource(AcmeAccountService aCMEAccountService) {
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
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeAccount> createAcmeAccount(@Valid @RequestBody AcmeAccount aCMEAccount) throws URISyntaxException {
        log.debug("REST request to save AcmeAccount : {}", aCMEAccount);
        if (aCMEAccount.getId() != null) {
            throw new BadRequestAlertException("A new aCMEAccount cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AcmeAccount result = aCMEAccountService.save(aCMEAccount);
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
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeAccount> updateAcmeAccount(@Valid @RequestBody AcmeAccount aCMEAccount) throws URISyntaxException {
        log.debug("REST request to update AcmeAccount : {}", aCMEAccount);
        if (aCMEAccount.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        AcmeAccount result = aCMEAccountService.save(aCMEAccount);
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
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<AcmeAccount> getAllAcmeAccounts() {
        log.debug("REST request to get all AcmeAccounts");
        return aCMEAccountService.findAll();
    }

    /**
     * {@code GET  /acme-accounts/:id} : get the "id" aCMEAccount.
     *
     * @param id the id of the aCMEAccount to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the aCMEAccount, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acme-accounts/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<AcmeAccount> getAcmeAccount(@PathVariable Long id) {
        log.debug("REST request to get AcmeAccount : {}", id);
        Optional<AcmeAccount> aCMEAccount = aCMEAccountService.findOne(id);
        return ResponseUtil.wrapOrNotFound(aCMEAccount);
    }

    /**
     * {@code DELETE  /acme-accounts/:id} : delete the "id" aCMEAccount.
     *
     * @param id the id of the aCMEAccount to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/acme-accounts/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteAcmeAccount(@PathVariable Long id) {
        log.debug("REST request to delete AcmeAccount : {}", id);
        aCMEAccountService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code POST  /acme-accounts/:id/status} : update the status of the "id" aCMEAccount.
     *
     * @param id the id of the aCMEAccount to be updated.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @PostMapping("/acme-accounts/{id}/status")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Void> updateAcmeAccountStatus(@PathVariable Long id, @RequestBody AcmeAccountStatusAdministration statusAdministration) {
        log.debug("REST request to update the status of AcmeAccount : {} to {}", id, statusAdministration.getStatus());
        Optional<AcmeAccount> acmeAccountOptional = aCMEAccountService.findOne(id);
        if( acmeAccountOptional.isPresent()){
            AcmeAccount acmeAccount = acmeAccountOptional.get();
            if(AccountStatus.REVOKED.equals(acmeAccount.getStatus())){
                log.info("AcmeAccount : {} has final state {}. Update to {} ignored", id, acmeAccount.getStatus(), statusAdministration.getStatus());
            }else{
                log.info("AcmeAccount : {} updated from state {} to {}.", id, acmeAccount.getStatus(), statusAdministration.getStatus());
                acmeAccount.setStatus(statusAdministration.getStatus());
            }
            return ResponseEntity.noContent().build();

        }else{
            return ResponseEntity.notFound().build();
        }
    }


}
