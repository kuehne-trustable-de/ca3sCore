package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.UserPreference;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link UserPreference}.
 */
public interface UserPreferenceService {

    /**
     * Save a userPreference.
     *
     * @param userPreference the entity to save.
     * @return the persisted entity.
     */
    UserPreference save(UserPreference userPreference);

    /**
     * Get all the userPreferences.
     *
     * @return the list of entities.
     */
    List<UserPreference> findAll();


    /**
     * Get all the userPreferences for a user id.
     *
     * @return the list of entities.
     */
    List<UserPreference> findAllForUserId(Long userId);

    /**
     * Get a specific userPreferences for a user id.
     *
     * @return the list of entities.
     */
    Optional<UserPreference> findPreferenceForUserId(String name, Long userId);

    /**
     * Get the "id" userPreference.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<UserPreference> findOne(Long id);

    /**
     * Delete the "id" userPreference.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
