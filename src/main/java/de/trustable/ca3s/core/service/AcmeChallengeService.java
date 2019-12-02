package de.trustable.ca3s.core.service;

import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.domain.AcmeChallenge;

/**
 * Service Interface for managing {@link AcmeChallenge}.
 */
public interface AcmeChallengeService {

    /**
     * Save a acmeChallenge.
     *
     * @param acmeChallenge the entity to save.
     * @return the persisted entity.
     */
    AcmeChallenge save(AcmeChallenge acmeChallenge);

    /**
     * Get all the acmeChallenges.
     *
     * @return the list of entities.
     */
    List<AcmeChallenge> findAll();


    /**
     * Get the "id" acmeChallenge.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeChallenge> findOne(Long id);

    /**
     * Delete the "id" acmeChallenge.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
