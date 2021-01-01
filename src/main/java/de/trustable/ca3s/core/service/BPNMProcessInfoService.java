package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.BPNMProcessInfo;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link BPNMProcessInfo}.
 */
public interface BPNMProcessInfoService {

    /**
     * Save a bPNMProcessInfo.
     *
     * @param bPNMProcessInfo the entity to save.
     * @return the persisted entity.
     */
    BPNMProcessInfo save(BPNMProcessInfo bPNMProcessInfo);

    /**
     * Get all the bPNMProcessInfos.
     *
     * @return the list of entities.
     */
    List<BPNMProcessInfo> findAll();

    /**
     * Get the "id" bPNMProcessInfo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<BPNMProcessInfo> findOne(Long id);

    /**
     * Delete the "id" bPNMProcessInfo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
