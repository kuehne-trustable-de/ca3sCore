package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.RequestAttributeValue;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link RequestAttributeValue}.
 */
public interface RequestAttributeValueService {

    /**
     * Save a requestAttributeValue.
     *
     * @param requestAttributeValue the entity to save.
     * @return the persisted entity.
     */
    RequestAttributeValue save(RequestAttributeValue requestAttributeValue);

    /**
     * Get all the requestAttributeValues.
     *
     * @return the list of entities.
     */
    List<RequestAttributeValue> findAll();

    /**
     * Get the "id" requestAttributeValue.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RequestAttributeValue> findOne(Long id);

    /**
     * Delete the "id" requestAttributeValue.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
