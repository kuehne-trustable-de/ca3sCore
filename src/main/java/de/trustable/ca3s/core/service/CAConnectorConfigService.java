package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link CAConnectorConfig}.
 */
public interface CAConnectorConfigService {

    /**
     * Save a cAConnectorConfig.
     *
     * @param cAConnectorConfig the entity to save.
     * @return the persisted entity.
     */
    CAConnectorConfig save(CAConnectorConfig cAConnectorConfig);

    /**
     * Get all the cAConnectorConfigs.
     *
     * @return the list of entities.
     */
    List<CAConnectorConfig> findAll();

    /**
     * Get the "id" cAConnectorConfig.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CAConnectorConfig> findOne(Long id);

    /**
     * Delete the "id" cAConnectorConfig.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
