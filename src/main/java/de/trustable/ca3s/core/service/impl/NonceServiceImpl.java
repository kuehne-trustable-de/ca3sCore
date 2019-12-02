package de.trustable.ca3s.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.Nonce;
import de.trustable.ca3s.core.repository.NonceRepository;
import de.trustable.ca3s.core.service.NonceService;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Nonce}.
 */
@Service
@Transactional
public class NonceServiceImpl implements NonceService {

    private final Logger log = LoggerFactory.getLogger(NonceServiceImpl.class);

    private final NonceRepository nonceRepository;

    public NonceServiceImpl(NonceRepository nonceRepository) {
        this.nonceRepository = nonceRepository;
    }

    /**
     * Save a nonce.
     *
     * @param nonce the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Nonce save(Nonce nonce) {
        log.debug("Request to save Nonce : {}", nonce);
        return nonceRepository.save(nonce);
    }

    /**
     * Get all the nonces.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Nonce> findAll() {
        log.debug("Request to get all Nonces");
        return nonceRepository.findAll();
    }


    /**
     * Get one nonce by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Nonce> findOne(Long id) {
        log.debug("Request to get Nonce : {}", id);
        return nonceRepository.findById(id);
    }

    /**
     * Delete the nonce by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Nonce : {}", id);
        nonceRepository.deleteById(id);
    }
}
