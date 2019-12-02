package de.trustable.ca3s.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.CSRService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service Implementation for managing {@link CSR}.
 */
@Service
@Transactional
public class CSRServiceImpl implements CSRService {

    private final Logger log = LoggerFactory.getLogger(CSRServiceImpl.class);

    private final CSRRepository cSRRepository;

    public CSRServiceImpl(CSRRepository cSRRepository) {
        this.cSRRepository = cSRRepository;
    }

    /**
     * Save a cSR.
     *
     * @param cSR the entity to save.
     * @return the persisted entity.
     */
    @Override
    public CSR save(CSR cSR) {
        log.debug("Request to save CSR : {}", cSR);
        return cSRRepository.save(cSR);
    }

    /**
     * Get all the cSRS.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CSR> findAll() {
        log.debug("Request to get all CSRS");
        return cSRRepository.findAll();
    }



    /**
    *  Get all the cSRS where Certificate is {@code null}.
     *  @return the list of entities.
     */
    @Transactional(readOnly = true) 
    public List<CSR> findAllWhereCertificateIsNull() {
        log.debug("Request to get all cSRS where Certificate is null");
        return StreamSupport
            .stream(cSRRepository.findAll().spliterator(), false)
            .filter(cSR -> cSR.getCertificate() == null)
            .collect(Collectors.toList());
    }

    /**
     * Get one cSR by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CSR> findOne(Long id) {
        log.debug("Request to get CSR : {}", id);
        return cSRRepository.findById(id);
    }

    /**
     * Delete the cSR by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete CSR : {}", id);
        cSRRepository.deleteById(id);
    }
}
