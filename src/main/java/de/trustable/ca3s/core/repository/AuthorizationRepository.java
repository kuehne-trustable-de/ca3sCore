package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Authorization;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Authorization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, Long> {
}
