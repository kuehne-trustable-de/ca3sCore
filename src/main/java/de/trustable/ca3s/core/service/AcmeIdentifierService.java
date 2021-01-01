package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AcmeIdentifier;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeIdentifier}.
 */
public interface AcmeIdentifierService {

    /**
     * Save a acmeIdentifier.
     *
     * @param acmeIdentifier the entity to save.
     * @return the persisted entity.
     */
    AcmeIdentifier save(AcmeIdentifier acmeIdentifier);

    /**
     * Get all the acmeIdentifiers.
     *
     * @return the list of entities.
     */
    List<AcmeIdentifier> findAll();

    /**
     * Get the "id" acmeIdentifier.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeIdentifier> findOne(Long id);

    /**
     * Delete the "id" acmeIdentifier.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
