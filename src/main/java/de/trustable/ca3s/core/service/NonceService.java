package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Nonce;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Nonce}.
 */
public interface NonceService {

    /**
     * Save a nonce.
     *
     * @param nonce the entity to save.
     * @return the persisted entity.
     */
    Nonce save(Nonce nonce);

    /**
     * Get all the nonces.
     *
     * @return the list of entities.
     */
    List<Nonce> findAll();

    /**
     * Get the "id" nonce.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Nonce> findOne(Long id);

    /**
     * Delete the "id" nonce.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
