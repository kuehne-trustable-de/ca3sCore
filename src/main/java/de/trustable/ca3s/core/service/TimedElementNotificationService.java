package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.TimedElementNotification;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link TimedElementNotification}.
 */
public interface TimedElementNotificationService {
    /**
     * Save a timedElementNotification.
     *
     * @param timedElementNotification the entity to save.
     * @return the persisted entity.
     */
    TimedElementNotification save(TimedElementNotification timedElementNotification);

    /**
     * Updates a timedElementNotification.
     *
     * @param timedElementNotification the entity to update.
     * @return the persisted entity.
     */
    TimedElementNotification update(TimedElementNotification timedElementNotification);

    /**
     * Partially updates a timedElementNotification.
     *
     * @param timedElementNotification the entity to update partially.
     * @return the persisted entity.
     */
    Optional<TimedElementNotification> partialUpdate(TimedElementNotification timedElementNotification);

    /**
     * Get all the timedElementNotifications.
     *
     * @return the list of entities.
     */
    List<TimedElementNotification> findAll();

    /**
     * Get the "id" timedElementNotification.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<TimedElementNotification> findOne(Long id);

    /**
     * Delete the "id" timedElementNotification.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
