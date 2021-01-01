package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CsrAttribute;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the CsrAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CsrAttributeRepository extends JpaRepository<CsrAttribute, Long> {
}
