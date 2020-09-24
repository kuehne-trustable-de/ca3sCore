package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.UserPreferenceService;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link UserPreference}.
 */
@Service
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final Logger log = LoggerFactory.getLogger(UserPreferenceServiceImpl.class);

    private final UserPreferenceRepository userPreferenceRepository;

    public UserPreferenceServiceImpl(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }

    /**
     * Save a userPreference.
     *
     * @param userPreference the entity to save.
     * @return the persisted entity.
     */
    @Override
    public UserPreference save(UserPreference userPreference) {
        log.debug("Request to save UserPreference : {}", userPreference);
        return userPreferenceRepository.save(userPreference);
    }

    /**
     * Get all the userPreferences.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserPreference> findAll() {
        log.debug("Request to get all UserPreferences");
        return userPreferenceRepository.findAll();
    }

    /**
     * Get all the userPreferences.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserPreference> findAllForUserId(Long id){
	    log.debug("Request to get all UserPreferences for user id {}", id);
	    return userPreferenceRepository.findByUser(id);
	}

    
    /**
     * Get a specific userPreferences for a user id.
     *
     * @return the list of entities.
     */
    @Override
    public Optional<UserPreference> findPreferenceForUserId(String name, Long userId){
        log.debug("Request to get UserPreference for name {} and user {}", name, userId);
        return userPreferenceRepository.findByNameforUser(name, userId);
    }

    /**
     * Get one userPreference by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<UserPreference> findOne(Long id) {
        log.debug("Request to get UserPreference : {}", id);
        return userPreferenceRepository.findById(id);
    }

    /**
     * Delete the userPreference by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete UserPreference : {}", id);
        userPreferenceRepository.deleteById(id);
    }
}
