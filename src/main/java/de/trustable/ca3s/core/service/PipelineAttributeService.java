package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.PipelineAttribute;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link PipelineAttribute}.
 */
public interface PipelineAttributeService {

    /**
     * Save a pipelineAttribute.
     *
     * @param pipelineAttribute the entity to save.
     * @return the persisted entity.
     */
    PipelineAttribute save(PipelineAttribute pipelineAttribute);

    /**
     * Get all the pipelineAttributes.
     *
     * @return the list of entities.
     */
    List<PipelineAttribute> findAll();


    /**
     * Get the "id" pipelineAttribute.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<PipelineAttribute> findOne(Long id);

    /**
     * Delete the "id" pipelineAttribute.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
