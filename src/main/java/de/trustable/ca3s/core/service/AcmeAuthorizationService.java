package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AcmeAuthorization;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeAuthorization}.
 */
public interface AcmeAuthorizationService {

    /**
     * Save a acmeAuthorization.
     *
     * @param acmeAuthorization the entity to save.
     * @return the persisted entity.
     */
    AcmeAuthorization save(AcmeAuthorization acmeAuthorization);

    /**
     * Get all the acmeAuthorizations.
     *
     * @return the list of entities.
     */
    List<AcmeAuthorization> findAll();


    /**
     * Get the "id" acmeAuthorization.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeAuthorization> findOne(Long id);

    /**
     * Delete the "id" acmeAuthorization.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
