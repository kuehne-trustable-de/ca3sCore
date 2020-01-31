package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AcmeAuthorizationService;
import de.trustable.ca3s.core.domain.AcmeAuthorization;
import de.trustable.ca3s.core.repository.AcmeAuthorizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeAuthorization}.
 */
@Service
@Transactional
public class AcmeAuthorizationServiceImpl implements AcmeAuthorizationService {

    private final Logger log = LoggerFactory.getLogger(AcmeAuthorizationServiceImpl.class);

    private final AcmeAuthorizationRepository acmeAuthorizationRepository;

    public AcmeAuthorizationServiceImpl(AcmeAuthorizationRepository acmeAuthorizationRepository) {
        this.acmeAuthorizationRepository = acmeAuthorizationRepository;
    }

    /**
     * Save a acmeAuthorization.
     *
     * @param acmeAuthorization the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeAuthorization save(AcmeAuthorization acmeAuthorization) {
        log.debug("Request to save AcmeAuthorization : {}", acmeAuthorization);
        return acmeAuthorizationRepository.save(acmeAuthorization);
    }

    /**
     * Get all the acmeAuthorizations.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeAuthorization> findAll() {
        log.debug("Request to get all AcmeAuthorizations");
        return acmeAuthorizationRepository.findAll();
    }


    /**
     * Get one acmeAuthorization by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeAuthorization> findOne(Long id) {
        log.debug("Request to get AcmeAuthorization : {}", id);
        return acmeAuthorizationRepository.findById(id);
    }

    /**
     * Delete the acmeAuthorization by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeAuthorization : {}", id);
        acmeAuthorizationRepository.deleteById(id);
    }
}
