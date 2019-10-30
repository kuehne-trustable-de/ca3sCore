package de.trustable.ca3sjh.service;

import de.trustable.ca3sjh.domain.AcmeContact;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeContact}.
 */
public interface AcmeContactService {

    /**
     * Save a acmeContact.
     *
     * @param acmeContact the entity to save.
     * @return the persisted entity.
     */
    AcmeContact save(AcmeContact acmeContact);

    /**
     * Get all the acmeContacts.
     *
     * @return the list of entities.
     */
    List<AcmeContact> findAll();


    /**
     * Get the "id" acmeContact.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeContact> findOne(Long id);

    /**
     * Delete the "id" acmeContact.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
