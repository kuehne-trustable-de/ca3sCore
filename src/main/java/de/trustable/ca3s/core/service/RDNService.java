package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.RDN;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link RDN}.
 */
public interface RDNService {

    /**
     * Save a rDN.
     *
     * @param rDN the entity to save.
     * @return the persisted entity.
     */
    RDN save(RDN rDN);

    /**
     * Get all the rDNS.
     *
     * @return the list of entities.
     */
    List<RDN> findAll();

    /**
     * Get the "id" rDN.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RDN> findOne(Long id);

    /**
     * Delete the "id" rDN.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
