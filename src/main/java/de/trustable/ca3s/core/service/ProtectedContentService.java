package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.ProtectedContent;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link ProtectedContent}.
 */
public interface ProtectedContentService {

    /**
     * Save a protectedContent.
     *
     * @param protectedContent the entity to save.
     * @return the persisted entity.
     */
    ProtectedContent save(ProtectedContent protectedContent);

    /**
     * Get all the protectedContents.
     *
     * @return the list of entities.
     */
    List<ProtectedContent> findAll();

    /**
     * Get the "id" protectedContent.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ProtectedContent> findOne(Long id);

    /**
     * Delete the "id" protectedContent.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
