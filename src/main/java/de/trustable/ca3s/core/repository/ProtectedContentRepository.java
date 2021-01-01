package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ProtectedContent;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the ProtectedContent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProtectedContentRepository extends JpaRepository<ProtectedContent, Long> {
}
