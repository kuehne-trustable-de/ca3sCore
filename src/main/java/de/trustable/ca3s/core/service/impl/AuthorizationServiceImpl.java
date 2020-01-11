package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AuthorizationService;
import de.trustable.ca3s.core.domain.Authorization;
import de.trustable.ca3s.core.repository.AuthorizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Authorization}.
 */
@Service
@Transactional
public class AuthorizationServiceImpl implements AuthorizationService {

    private final Logger log = LoggerFactory.getLogger(AuthorizationServiceImpl.class);

    private final AuthorizationRepository authorizationRepository;

    public AuthorizationServiceImpl(AuthorizationRepository authorizationRepository) {
        this.authorizationRepository = authorizationRepository;
    }

    /**
     * Save a authorization.
     *
     * @param authorization the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Authorization save(Authorization authorization) {
        log.debug("Request to save Authorization : {}", authorization);
        return authorizationRepository.save(authorization);
    }

    /**
     * Get all the authorizations.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Authorization> findAll() {
        log.debug("Request to get all Authorizations");
        return authorizationRepository.findAll();
    }


    /**
     * Get one authorization by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Authorization> findOne(Long id) {
        log.debug("Request to get Authorization : {}", id);
        return authorizationRepository.findById(id);
    }

    /**
     * Delete the authorization by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Authorization : {}", id);
        authorizationRepository.deleteById(id);
    }
}
