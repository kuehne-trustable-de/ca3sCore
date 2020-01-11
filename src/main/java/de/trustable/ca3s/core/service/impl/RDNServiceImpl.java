package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.RDNService;
import de.trustable.ca3s.core.domain.RDN;
import de.trustable.ca3s.core.repository.RDNRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link RDN}.
 */
@Service
@Transactional
public class RDNServiceImpl implements RDNService {

    private final Logger log = LoggerFactory.getLogger(RDNServiceImpl.class);

    private final RDNRepository rDNRepository;

    public RDNServiceImpl(RDNRepository rDNRepository) {
        this.rDNRepository = rDNRepository;
    }

    /**
     * Save a rDN.
     *
     * @param rDN the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RDN save(RDN rDN) {
        log.debug("Request to save RDN : {}", rDN);
        return rDNRepository.save(rDN);
    }

    /**
     * Get all the rDNS.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RDN> findAll() {
        log.debug("Request to get all RDNS");
        return rDNRepository.findAll();
    }


    /**
     * Get one rDN by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RDN> findOne(Long id) {
        log.debug("Request to get RDN : {}", id);
        return rDNRepository.findById(id);
    }

    /**
     * Delete the rDN by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete RDN : {}", id);
        rDNRepository.deleteById(id);
    }
}
