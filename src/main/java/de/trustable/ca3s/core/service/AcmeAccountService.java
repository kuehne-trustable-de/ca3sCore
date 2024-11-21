package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AcmeAccount;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeAccount}.
 */
public interface AcmeAccountService {

    /**
     * Save a aCMEAccount.
     *
     * @param aCMEAccount the entity to save.
     * @return the persisted entity.
     */
    AcmeAccount save(AcmeAccount aCMEAccount);

    /**
     * Get all the aCMEAccounts.
     *
     * @return the list of entities.
     */
    List<AcmeAccount> findAll();


    /**
     * Get the "id" aCMEAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeAccount> findOne(Long id);

    /**
     * Get the "accountId" aCMEAccount.
     *
     * @param accountId the accountId of the account.
     * @return the entity.
     */
    Optional<AcmeAccount> findOneByAccountId(Long accountId);

    /**
     * Delete the "id" aCMEAccount.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
