package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AlgorithmRestriction;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AlgorithmRestriction}.
 */
public interface AlgorithmRestrictionService {
    /**
     * Save a algorithmRestriction.
     *
     * @param algorithmRestriction the entity to save.
     * @return the persisted entity.
     */
    AlgorithmRestriction save(AlgorithmRestriction algorithmRestriction);

    /**
     * Partially updates a algorithmRestriction.
     *
     * @param algorithmRestriction the entity to update partially.
     * @return the persisted entity.
     */
    Optional<AlgorithmRestriction> partialUpdate(AlgorithmRestriction algorithmRestriction);

    /**
     * Get all the algorithmRestrictions.
     *
     * @return the list of entities.
     */
    List<AlgorithmRestriction> findAll();

    /**
     * Get the "id" algorithmRestriction.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AlgorithmRestriction> findOne(Long id);

    /**
     * Delete the "id" algorithmRestriction.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
