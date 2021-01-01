package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the AcmeOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeOrderRepository extends JpaRepository<AcmeOrder, Long> {
}
