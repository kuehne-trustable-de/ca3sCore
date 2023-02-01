package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link CRLExpirationNotification}.
 */
public interface CRLExpirationNotificationService {

    List<CRLExpirationNotification> createByCertificateId(Long certificateId);

        /**
         * Save a cRLExpirationNotification.
         *
         * @param cRLExpirationNotification the entity to save.
         * @return the persisted entity.
         */
    CRLExpirationNotification save(CRLExpirationNotification cRLExpirationNotification);

    /**
     * Updates a cRLExpirationNotification.
     *
     * @param cRLExpirationNotification the entity to update.
     * @return the persisted entity.
     */
    CRLExpirationNotification update(CRLExpirationNotification cRLExpirationNotification);

    /**
     * Partially updates a cRLExpirationNotification.
     *
     * @param cRLExpirationNotification the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CRLExpirationNotification> partialUpdate(CRLExpirationNotification cRLExpirationNotification);

    /**
     * Get all the cRLExpirationNotifications.
     *
     * @return the list of entities.
     */
    List<CRLExpirationNotification> findAll();

    /**
     * Get the "id" cRLExpirationNotification.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CRLExpirationNotification> findOne(Long id);

    /**
     * Delete the "id" cRLExpirationNotification.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
