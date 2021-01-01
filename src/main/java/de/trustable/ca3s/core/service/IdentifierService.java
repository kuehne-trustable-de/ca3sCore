package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Identifier}.
 */
public interface IdentifierService {

    /**
     * Save a identifier.
     *
     * @param identifier the entity to save.
     * @return the persisted entity.
     */
    Identifier save(Identifier identifier);

    /**
     * Get all the identifiers.
     *
     * @return the list of entities.
     */
    List<Identifier> findAll();

    /**
     * Get the "id" identifier.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Identifier> findOne(Long id);

    /**
     * Delete the "id" identifier.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
