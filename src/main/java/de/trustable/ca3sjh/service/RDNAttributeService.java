package de.trustable.ca3sjh.service;

import de.trustable.ca3sjh.domain.RDNAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link RDNAttribute}.
 */
public interface RDNAttributeService {

    /**
     * Save a rDNAttribute.
     *
     * @param rDNAttribute the entity to save.
     * @return the persisted entity.
     */
    RDNAttribute save(RDNAttribute rDNAttribute);

    /**
     * Get all the rDNAttributes.
     *
     * @return the list of entities.
     */
    List<RDNAttribute> findAll();


    /**
     * Get the "id" rDNAttribute.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RDNAttribute> findOne(Long id);

    /**
     * Delete the "id" rDNAttribute.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
