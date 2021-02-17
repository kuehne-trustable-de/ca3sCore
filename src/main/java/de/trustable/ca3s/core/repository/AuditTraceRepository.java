package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AuditTrace;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the AuditTrace entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuditTraceRepository extends JpaRepository<AuditTrace, Long> {
}
