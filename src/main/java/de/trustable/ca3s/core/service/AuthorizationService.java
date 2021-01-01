package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Authorization;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Authorization}.
 */
public interface AuthorizationService {

    /**
     * Save a authorization.
     *
     * @param authorization the entity to save.
     * @return the persisted entity.
     */
    Authorization save(Authorization authorization);

    /**
     * Get all the authorizations.
     *
     * @return the list of entities.
     */
    List<Authorization> findAll();

    /**
     * Get the "id" authorization.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Authorization> findOne(Long id);

    /**
     * Delete the "id" authorization.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
