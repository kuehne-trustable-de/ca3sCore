package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.CSR;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link CSR}.
 */
public interface CSRService {

    /**
     * Save a cSR.
     *
     * @param cSR the entity to save.
     * @return the persisted entity.
     */
    CSR save(CSR cSR);

    /**
     * Get all the cSRS.
     *
     * @return the list of entities.
     */
    List<CSR> findAll();
    /**
     * Get all the CSRDTO where Certificate is {@code null}.
     *
     * @return the list of entities.
     */
    List<CSR> findAllWhereCertificateIsNull();

    /**
     * Get the "id" cSR.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CSR> findOne(Long id);

    /**
     * Delete the "id" cSR.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
