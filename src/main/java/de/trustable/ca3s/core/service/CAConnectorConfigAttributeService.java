package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.CAConnectorConfigAttribute;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link CAConnectorConfigAttribute}.
 */
public interface CAConnectorConfigAttributeService {
    /**
     * Save a cAConnectorConfigAttribute.
     *
     * @param cAConnectorConfigAttribute the entity to save.
     * @return the persisted entity.
     */
    CAConnectorConfigAttribute save(CAConnectorConfigAttribute cAConnectorConfigAttribute);

    /**
     * Updates a cAConnectorConfigAttribute.
     *
     * @param cAConnectorConfigAttribute the entity to update.
     * @return the persisted entity.
     */
    CAConnectorConfigAttribute update(CAConnectorConfigAttribute cAConnectorConfigAttribute);

    /**
     * Partially updates a cAConnectorConfigAttribute.
     *
     * @param cAConnectorConfigAttribute the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CAConnectorConfigAttribute> partialUpdate(CAConnectorConfigAttribute cAConnectorConfigAttribute);

    /**
     * Get all the cAConnectorConfigAttributes.
     *
     * @return the list of entities.
     */
    List<CAConnectorConfigAttribute> findAll();

    /**
     * Get the "id" cAConnectorConfigAttribute.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CAConnectorConfigAttribute> findOne(Long id);

    /**
     * Delete the "id" cAConnectorConfigAttribute.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
