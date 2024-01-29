package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for the Tenant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    @Query(name = "Tenant.findByName")
    Optional<Tenant> findByName(@Param("name") String name);

}
