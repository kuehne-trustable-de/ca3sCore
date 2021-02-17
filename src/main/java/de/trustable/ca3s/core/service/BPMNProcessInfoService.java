package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link BPMNProcessInfo}.
 */
public interface BPMNProcessInfoService {

    /**
     * Save a bPMNProcessInfo.
     *
     * @param bPMNProcessInfo the entity to save.
     * @return the persisted entity.
     */
    BPMNProcessInfo save(BPMNProcessInfo bPMNProcessInfo);

    /**
     * Get all the bPMNProcessInfos.
     *
     * @return the list of entities.
     */
    List<BPMNProcessInfo> findAll();


    /**
     * Get the "id" bPMNProcessInfo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BPMNProcessInfo> findOne(Long id);

    /**
     * Delete the "id" bPMNProcessInfo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
