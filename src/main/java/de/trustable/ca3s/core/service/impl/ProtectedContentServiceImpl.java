package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.ProtectedContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.trustable.ca3s.core.domain.ProtectedContent.USER_CONTENT_RELATION_TYPE_LIST;

/**
 * Service Implementation for managing {@link ProtectedContent}.
 */
@Service
@Transactional
public class ProtectedContentServiceImpl implements ProtectedContentService {

    private final Logger log = LoggerFactory.getLogger(ProtectedContentServiceImpl.class);

    private final ProtectedContentRepository protectedContentRepository;

    public ProtectedContentServiceImpl(ProtectedContentRepository protectedContentRepository) {
        this.protectedContentRepository = protectedContentRepository;
    }

    /**
     * Save a protectedContent.
     *
     * @param protectedContent the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ProtectedContent save(ProtectedContent protectedContent) {
        log.debug("Request to save ProtectedContent : {}", protectedContent);
        return protectedContentRepository.save(protectedContent);
    }

    /**
     * Get all the protectedContents.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProtectedContent> findAll() {
        log.debug("Request to get all ProtectedContents");
        return protectedContentRepository.findAll();
    }


    /**
     * Get one protectedContent by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ProtectedContent> findOne(Long id) {
        log.debug("Request to get ProtectedContent : {}", id);
        return protectedContentRepository.findById(id);
    }

    /**
     * Delete the protectedContent by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ProtectedContent : {}", id);
        protectedContentRepository.deleteById(id);
    }


}
