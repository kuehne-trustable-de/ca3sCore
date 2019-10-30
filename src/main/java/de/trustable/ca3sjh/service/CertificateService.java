package de.trustable.ca3sjh.service;

import de.trustable.ca3sjh.domain.Certificate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link Certificate}.
 */
public interface CertificateService {

    /**
     * Save a certificate.
     *
     * @param certificate the entity to save.
     * @return the persisted entity.
     */
    Certificate save(Certificate certificate);

    /**
     * Get all the certificates.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Certificate> findAll(Pageable pageable);


    /**
     * Get the "id" certificate.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Certificate> findOne(Long id);

    /**
     * Delete the "id" certificate.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
