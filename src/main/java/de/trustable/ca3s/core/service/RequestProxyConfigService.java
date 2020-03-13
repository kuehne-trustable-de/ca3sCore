package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.RequestProxyConfig;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link RequestProxyConfig}.
 */
public interface RequestProxyConfigService {

    /**
     * Save a requestProxyConfig.
     *
     * @param requestProxyConfig the entity to save.
     * @return the persisted entity.
     */
    RequestProxyConfig save(RequestProxyConfig requestProxyConfig);

    /**
     * Get all the requestProxyConfigs.
     *
     * @return the list of entities.
     */
    List<RequestProxyConfig> findAll();

    /**
     * Get the "id" requestProxyConfig.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<RequestProxyConfig> findOne(Long id);

    /**
     * Delete the "id" requestProxyConfig.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
