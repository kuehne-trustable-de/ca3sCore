package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Pipeline;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Pipeline}.
 */
public interface PipelineService {

    /**
     * Save a pipeline.
     *
     * @param pipeline the entity to save.
     * @return the persisted entity.
     */
    Pipeline save(Pipeline pipeline);

    /**
     * Get all the pipelines.
     *
     * @return the list of entities.
     */
    List<Pipeline> findAll();


    /**
     * Get the "id" pipeline.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Pipeline> findOne(Long id);

    /**
     * Delete the "id" pipeline.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
