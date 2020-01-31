package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AcmeNonce;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeNonce}.
 */
public interface AcmeNonceService {

    /**
     * Save a acmeNonce.
     *
     * @param acmeNonce the entity to save.
     * @return the persisted entity.
     */
    AcmeNonce save(AcmeNonce acmeNonce);

    /**
     * Get all the acmeNonces.
     *
     * @return the list of entities.
     */
    List<AcmeNonce> findAll();


    /**
     * Get the "id" acmeNonce.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeNonce> findOne(Long id);

    /**
     * Delete the "id" acmeNonce.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
