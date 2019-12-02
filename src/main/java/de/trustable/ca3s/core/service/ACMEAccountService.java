package de.trustable.ca3s.core.service;

import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.domain.ACMEAccount;

/**
 * Service Interface for managing {@link ACMEAccount}.
 */
public interface ACMEAccountService {

    /**
     * Save a aCMEAccount.
     *
     * @param aCMEAccount the entity to save.
     * @return the persisted entity.
     */
    ACMEAccount save(ACMEAccount aCMEAccount);

    /**
     * Get all the aCMEAccounts.
     *
     * @return the list of entities.
     */
    List<ACMEAccount> findAll();


    /**
     * Get the "id" aCMEAccount.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ACMEAccount> findOne(Long id);

    /**
     * Delete the "id" aCMEAccount.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
