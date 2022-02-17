package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.ScepOrder;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link ScepOrder}.
 */
public interface ScepOrderService {

    /**
     * Save a scepOrder.
     *
     * @param scepOrder the entity to save.
     * @return the persisted entity.
     */
    ScepOrder save(ScepOrder scepOrder);

    /**
     * Get all the scepOrders.
     *
     * @return the list of entities.
     */
    List<ScepOrder> findAll();


    /**
     * Get the "id" scepOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<ScepOrder> findOne(Long id);

    /**
     * Delete the "id" scepOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
