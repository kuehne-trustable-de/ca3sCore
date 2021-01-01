package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Identifier;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Identifier entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IdentifierRepository extends JpaRepository<Identifier, Long> {
}
