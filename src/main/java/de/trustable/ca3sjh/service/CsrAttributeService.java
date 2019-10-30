package de.trustable.ca3sjh.service;

import de.trustable.ca3sjh.domain.CsrAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link CsrAttribute}.
 */
public interface CsrAttributeService {

    /**
     * Save a csrAttribute.
     *
     * @param csrAttribute the entity to save.
     * @return the persisted entity.
     */
    CsrAttribute save(CsrAttribute csrAttribute);

    /**
     * Get all the csrAttributes.
     *
     * @return the list of entities.
     */
    List<CsrAttribute> findAll();


    /**
     * Get the "id" csrAttribute.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CsrAttribute> findOne(Long id);

    /**
     * Delete the "id" csrAttribute.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
