package de.trustable.ca3s.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CsrAttributeRepository;
import de.trustable.ca3s.core.service.CsrAttributeService;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link CsrAttribute}.
 */
@Service
@Transactional
public class CsrAttributeServiceImpl implements CsrAttributeService {

    private final Logger log = LoggerFactory.getLogger(CsrAttributeServiceImpl.class);

    private final CsrAttributeRepository csrAttributeRepository;

    public CsrAttributeServiceImpl(CsrAttributeRepository csrAttributeRepository) {
        this.csrAttributeRepository = csrAttributeRepository;
    }

    /**
     * Save a csrAttribute.
     *
     * @param csrAttribute the entity to save.
     * @return the persisted entity.
     */
    @Override
    public CsrAttribute save(CsrAttribute csrAttribute) {
        log.debug("Request to save CsrAttribute : {}", csrAttribute);
        return csrAttributeRepository.save(csrAttribute);
    }

    /**
     * Get all the csrAttributes.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CsrAttribute> findAll() {
        log.debug("Request to get all CsrAttributes");
        return csrAttributeRepository.findAll();
    }


    /**
     * Get one csrAttribute by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CsrAttribute> findOne(Long id) {
        log.debug("Request to get CsrAttribute : {}", id);
        return csrAttributeRepository.findById(id);
    }

    /**
     * Delete the csrAttribute by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete CsrAttribute : {}", id);
        csrAttributeRepository.deleteById(id);
    }
}
