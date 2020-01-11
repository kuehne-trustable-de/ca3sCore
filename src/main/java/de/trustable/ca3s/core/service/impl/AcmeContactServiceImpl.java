package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AcmeContactService;
import de.trustable.ca3s.core.domain.AcmeContact;
import de.trustable.ca3s.core.repository.AcmeContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeContact}.
 */
@Service
@Transactional
public class AcmeContactServiceImpl implements AcmeContactService {

    private final Logger log = LoggerFactory.getLogger(AcmeContactServiceImpl.class);

    private final AcmeContactRepository acmeContactRepository;

    public AcmeContactServiceImpl(AcmeContactRepository acmeContactRepository) {
        this.acmeContactRepository = acmeContactRepository;
    }

    /**
     * Save a acmeContact.
     *
     * @param acmeContact the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeContact save(AcmeContact acmeContact) {
        log.debug("Request to save AcmeContact : {}", acmeContact);
        return acmeContactRepository.save(acmeContact);
    }

    /**
     * Get all the acmeContacts.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeContact> findAll() {
        log.debug("Request to get all AcmeContacts");
        return acmeContactRepository.findAll();
    }


    /**
     * Get one acmeContact by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeContact> findOne(Long id) {
        log.debug("Request to get AcmeContact : {}", id);
        return acmeContactRepository.findById(id);
    }

    /**
     * Delete the acmeContact by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeContact : {}", id);
        acmeContactRepository.deleteById(id);
    }
}
