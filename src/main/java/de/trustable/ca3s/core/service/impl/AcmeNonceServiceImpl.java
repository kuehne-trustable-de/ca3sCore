package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AcmeNonceService;
import de.trustable.ca3s.core.domain.AcmeNonce;
import de.trustable.ca3s.core.repository.AcmeNonceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeNonce}.
 */
@Service
@Transactional
public class AcmeNonceServiceImpl implements AcmeNonceService {

    private final Logger log = LoggerFactory.getLogger(AcmeNonceServiceImpl.class);

    private final AcmeNonceRepository acmeNonceRepository;

    public AcmeNonceServiceImpl(AcmeNonceRepository acmeNonceRepository) {
        this.acmeNonceRepository = acmeNonceRepository;
    }

    /**
     * Save a acmeNonce.
     *
     * @param acmeNonce the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeNonce save(AcmeNonce acmeNonce) {
        log.debug("Request to save AcmeNonce : {}", acmeNonce);
        return acmeNonceRepository.save(acmeNonce);
    }

    /**
     * Get all the acmeNonces.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeNonce> findAll() {
        log.debug("Request to get all AcmeNonces");
        return acmeNonceRepository.findAll();
    }


    /**
     * Get one acmeNonce by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeNonce> findOne(Long id) {
        log.debug("Request to get AcmeNonce : {}", id);
        return acmeNonceRepository.findById(id);
    }

    /**
     * Delete the acmeNonce by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeNonce : {}", id);
        acmeNonceRepository.deleteById(id);
    }
}
