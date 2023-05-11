package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.service.AcmeChallengeService;
import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.repository.AcmeChallengeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeChallenge}.
 */
@Service
@Transactional
public class AcmeChallengeServiceImpl implements AcmeChallengeService {

    private final Logger log = LoggerFactory.getLogger(AcmeChallengeServiceImpl.class);

    private final AcmeChallengeRepository acmeChallengeRepository;

    public AcmeChallengeServiceImpl(AcmeChallengeRepository acmeChallengeRepository) {
        this.acmeChallengeRepository = acmeChallengeRepository;
    }

    /**
     * Save a acmeChallenge.
     *
     * @param acmeChallenge the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeChallenge save(AcmeChallenge acmeChallenge) {
        log.debug("Request to save AcmeChallenge : {}", acmeChallenge);
        return acmeChallengeRepository.save(acmeChallenge);
    }

    /**
     * Get all the acmeChallenges.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeChallenge> findAll() {
        log.debug("Request to get all AcmeChallenges");
        return acmeChallengeRepository.findAll();
    }


    /**
     * Get one acmeChallenge by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeChallenge> findOne(Long id) {
        log.debug("Request to get AcmeChallenge : {}", id);
        return acmeChallengeRepository.findById(id);
    }

    /**
     * Delete the acmeChallenge by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeChallenge : {}", id);
        acmeChallengeRepository.deleteById(id);
    }

    @Override
    public List<AcmeChallenge> findPendingByRequestProxy(Long requestProxyId) {

        return acmeChallengeRepository.findPendingByRequestProxy(requestProxyId);
    }

}
