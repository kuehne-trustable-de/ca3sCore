package de.trustable.ca3s.core.service;

import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.service.dto.PipelineView;

/**
 * Service Interface for managing {@link PipelineView}.
 */
public interface PipelineViewService {

    /**
     * Save a pipeline.
     *
     * @param pipeline the entity to save.
     * @return the persisted entity.
     */
	PipelineView save(PipelineView pipeline);

    /**
     * Get all the pipelines.
     *
     * @return the list of entities.
     */
    List<PipelineView> findAll();


    /**
     * Get the "id" pipeline.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PipelineView> findOne(Long id);

    /**
     * Delete the "id" pipeline.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
