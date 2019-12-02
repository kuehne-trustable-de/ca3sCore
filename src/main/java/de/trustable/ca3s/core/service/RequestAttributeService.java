package de.trustable.ca3s.core.service;

import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.domain.RequestAttribute;

/**
 * Service Interface for managing {@link RequestAttribute}.
 */
public interface RequestAttributeService {

    /**
     * Save a requestAttribute.
     *
     * @param requestAttribute the entity to save.
     * @return the persisted entity.
     */
    RequestAttribute save(RequestAttribute requestAttribute);

    /**
     * Get all the requestAttributes.
     *
     * @return the list of entities.
     */
    List<RequestAttribute> findAll();


    /**
     * Get the "id" requestAttribute.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RequestAttribute> findOne(Long id);

    /**
     * Delete the "id" requestAttribute.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
