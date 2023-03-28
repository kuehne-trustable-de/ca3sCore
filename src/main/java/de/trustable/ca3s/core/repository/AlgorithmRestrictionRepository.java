package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AlgorithmRestriction;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the AlgorithmRestriction entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AlgorithmRestrictionRepository extends JpaRepository<AlgorithmRestriction, Long> {}
