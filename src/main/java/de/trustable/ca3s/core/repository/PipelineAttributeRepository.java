package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.PipelineAttribute;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the PipelineAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PipelineAttributeRepository extends JpaRepository<PipelineAttribute, Long> {
}
