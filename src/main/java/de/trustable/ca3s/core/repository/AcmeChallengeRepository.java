package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeChallenge;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the AcmeChallenge entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeChallengeRepository extends JpaRepository<AcmeChallenge, Long> {
}
