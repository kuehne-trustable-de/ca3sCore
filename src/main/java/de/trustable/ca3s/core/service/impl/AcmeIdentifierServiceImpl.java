package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AcmeIdentifierService;
import de.trustable.ca3s.core.domain.AcmeIdentifier;
import de.trustable.ca3s.core.repository.AcmeIdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeIdentifier}.
 */
@Service
@Transactional
public class AcmeIdentifierServiceImpl implements AcmeIdentifierService {

    private final Logger log = LoggerFactory.getLogger(AcmeIdentifierServiceImpl.class);

    private final AcmeIdentifierRepository acmeIdentifierRepository;

    public AcmeIdentifierServiceImpl(AcmeIdentifierRepository acmeIdentifierRepository) {
        this.acmeIdentifierRepository = acmeIdentifierRepository;
    }

    /**
     * Save a acmeIdentifier.
     *
     * @param acmeIdentifier the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeIdentifier save(AcmeIdentifier acmeIdentifier) {
        log.debug("Request to save AcmeIdentifier : {}", acmeIdentifier);
        return acmeIdentifierRepository.save(acmeIdentifier);
    }

    /**
     * Get all the acmeIdentifiers.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeIdentifier> findAll() {
        log.debug("Request to get all AcmeIdentifiers");
        return acmeIdentifierRepository.findAll();
    }

    /**
     * Get one acmeIdentifier by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeIdentifier> findOne(Long id) {
        log.debug("Request to get AcmeIdentifier : {}", id);
        return acmeIdentifierRepository.findById(id);
    }

    /**
     * Delete the acmeIdentifier by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeIdentifier : {}", id);
        acmeIdentifierRepository.deleteById(id);
    }
}
