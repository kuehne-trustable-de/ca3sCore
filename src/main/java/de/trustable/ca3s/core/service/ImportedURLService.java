package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.ImportedURL;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link ImportedURL}.
 */
public interface ImportedURLService {

    /**
     * Save a importedURL.
     *
     * @param importedURL the entity to save.
     * @return the persisted entity.
     */
    ImportedURL save(ImportedURL importedURL);

    /**
     * Get all the importedURLS.
     *
     * @return the list of entities.
     */
    List<ImportedURL> findAll();


    /**
     * Get the "id" importedURL.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ImportedURL> findOne(Long id);

    /**
     * Delete the "id" importedURL.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
