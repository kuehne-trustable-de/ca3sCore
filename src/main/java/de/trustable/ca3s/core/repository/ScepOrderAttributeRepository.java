package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ScepOrderAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ScepOrderAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ScepOrderAttributeRepository extends JpaRepository<ScepOrderAttribute, Long> {}
