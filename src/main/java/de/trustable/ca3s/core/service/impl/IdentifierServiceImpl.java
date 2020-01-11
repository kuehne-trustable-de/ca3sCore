package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.IdentifierService;
import de.trustable.ca3s.core.domain.Identifier;
import de.trustable.ca3s.core.repository.IdentifierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Identifier}.
 */
@Service
@Transactional
public class IdentifierServiceImpl implements IdentifierService {

    private final Logger log = LoggerFactory.getLogger(IdentifierServiceImpl.class);

    private final IdentifierRepository identifierRepository;

    public IdentifierServiceImpl(IdentifierRepository identifierRepository) {
        this.identifierRepository = identifierRepository;
    }

    /**
     * Save a identifier.
     *
     * @param identifier the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Identifier save(Identifier identifier) {
        log.debug("Request to save Identifier : {}", identifier);
        return identifierRepository.save(identifier);
    }

    /**
     * Get all the identifiers.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Identifier> findAll() {
        log.debug("Request to get all Identifiers");
        return identifierRepository.findAll();
    }


    /**
     * Get one identifier by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Identifier> findOne(Long id) {
        log.debug("Request to get Identifier : {}", id);
        return identifierRepository.findById(id);
    }

    /**
     * Delete the identifier by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Identifier : {}", id);
        identifierRepository.deleteById(id);
    }
}
