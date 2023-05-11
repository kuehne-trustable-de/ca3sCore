package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeChallenge;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the AcmeChallenge entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeChallengeRepository extends JpaRepository<AcmeChallenge, Long> {

    @Query(name = "AcmeChallenge.findByChallengeId")
    Optional<AcmeChallenge> findByChallengeId(@Param("challengeId") Long challengeId);

    @Query(name = "AcmeChallenge.findPendingByRealm")
    List<AcmeChallenge> findPendingByRealm(@Param("realm") String realm);

    @Query(name = "AcmeChallenge.findPendingByRequestProxy")
    List<AcmeChallenge> findPendingByRequestProxy(@Param("requestProxyId") Long requestProxyId);
}
