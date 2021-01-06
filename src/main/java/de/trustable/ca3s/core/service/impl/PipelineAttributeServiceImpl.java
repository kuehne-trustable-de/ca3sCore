package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.PipelineAttributeService;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.repository.PipelineAttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link PipelineAttribute}.
 */
@Service
@Transactional
public class PipelineAttributeServiceImpl implements PipelineAttributeService {

    private final Logger log = LoggerFactory.getLogger(PipelineAttributeServiceImpl.class);

    private final PipelineAttributeRepository pipelineAttributeRepository;

    public PipelineAttributeServiceImpl(PipelineAttributeRepository pipelineAttributeRepository) {
        this.pipelineAttributeRepository = pipelineAttributeRepository;
    }

    /**
     * Save a pipelineAttribute.
     *
     * @param pipelineAttribute the entity to save.
     * @return the persisted entity.
     */
    @Override
    public PipelineAttribute save(PipelineAttribute pipelineAttribute) {
        log.debug("Request to save PipelineAttribute : {}", pipelineAttribute);
        return pipelineAttributeRepository.save(pipelineAttribute);
    }

    /**
     * Get all the pipelineAttributes.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PipelineAttribute> findAll() {
        log.debug("Request to get all PipelineAttributes");
        return pipelineAttributeRepository.findAll();
    }

    /**
     * Get one pipelineAttribute by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PipelineAttribute> findOne(Long id) {
        log.debug("Request to get PipelineAttribute : {}", id);
        return pipelineAttributeRepository.findById(id);
    }

    /**
     * Delete the pipelineAttribute by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete PipelineAttribute : {}", id);
        pipelineAttributeRepository.deleteById(id);
    }
}
