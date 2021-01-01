package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AcmeOrder;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AcmeOrder}.
 */
public interface AcmeOrderService {

    /**
     * Save a acmeOrder.
     *
     * @param acmeOrder the entity to save.
     * @return the persisted entity.
     */
    AcmeOrder save(AcmeOrder acmeOrder);

    /**
     * Get all the acmeOrders.
     *
     * @return the list of entities.
     */
    List<AcmeOrder> findAll();

    /**
     * Get the "id" acmeOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AcmeOrder> findOne(Long id);

    /**
     * Delete the "id" acmeOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
