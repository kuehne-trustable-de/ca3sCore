package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.RequestAttributeValue;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the RequestAttributeValue entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RequestAttributeValueRepository extends JpaRepository<RequestAttributeValue, Long> {
}
