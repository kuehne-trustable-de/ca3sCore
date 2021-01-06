package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.PipelineService;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.PipelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Pipeline}.
 */
@Service
@Transactional
public class PipelineServiceImpl implements PipelineService {

    private final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private final PipelineRepository pipelineRepository;

    public PipelineServiceImpl(PipelineRepository pipelineRepository) {
        this.pipelineRepository = pipelineRepository;
    }

    /**
     * Save a pipeline.
     *
     * @param pipeline the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Pipeline save(Pipeline pipeline) {
        log.debug("Request to save Pipeline : {}", pipeline);
        return pipelineRepository.save(pipeline);
    }

    /**
     * Get all the pipelines.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pipeline> findAll() {
        log.debug("Request to get all Pipelines");
        return pipelineRepository.findAll();
    }

    /**
     * Get one pipeline by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Pipeline> findOne(Long id) {
        log.debug("Request to get Pipeline : {}", id);
        return pipelineRepository.findById(id);
    }

    /**
     * Delete the pipeline by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Pipeline : {}", id);
        pipelineRepository.deleteById(id);
    }
}
