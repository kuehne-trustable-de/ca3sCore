package de.trustable.ca3sjh.service.impl;

import de.trustable.ca3sjh.service.RDNAttributeService;
import de.trustable.ca3sjh.domain.RDNAttribute;
import de.trustable.ca3sjh.repository.RDNAttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link RDNAttribute}.
 */
@Service
@Transactional
public class RDNAttributeServiceImpl implements RDNAttributeService {

    private final Logger log = LoggerFactory.getLogger(RDNAttributeServiceImpl.class);

    private final RDNAttributeRepository rDNAttributeRepository;

    public RDNAttributeServiceImpl(RDNAttributeRepository rDNAttributeRepository) {
        this.rDNAttributeRepository = rDNAttributeRepository;
    }

    /**
     * Save a rDNAttribute.
     *
     * @param rDNAttribute the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RDNAttribute save(RDNAttribute rDNAttribute) {
        log.debug("Request to save RDNAttribute : {}", rDNAttribute);
        return rDNAttributeRepository.save(rDNAttribute);
    }

    /**
     * Get all the rDNAttributes.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RDNAttribute> findAll() {
        log.debug("Request to get all RDNAttributes");
        return rDNAttributeRepository.findAll();
    }


    /**
     * Get one rDNAttribute by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RDNAttribute> findOne(Long id) {
        log.debug("Request to get RDNAttribute : {}", id);
        return rDNAttributeRepository.findById(id);
    }

    /**
     * Delete the rDNAttribute by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete RDNAttribute : {}", id);
        rDNAttributeRepository.deleteById(id);
    }
}
