package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Tenant;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link Tenant}.
 */
public interface TenantService {
    /**
     * Save a tenant.
     *
     * @param tenant the entity to save.
     * @return the persisted entity.
     */
    Tenant save(Tenant tenant);

    /**
     * Updates a tenant.
     *
     * @param tenant the entity to update.
     * @return the persisted entity.
     */
    Tenant update(Tenant tenant);

    /**
     * Partially updates a tenant.
     *
     * @param tenant the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Tenant> partialUpdate(Tenant tenant);

    /**
     * Get all the tenants.
     *
     * @return the list of entities.
     */
    List<Tenant> findAll();

    /**
     * Get the "id" tenant.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Tenant> findOne(Long id);

    /**
     * Delete the "id" tenant.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
