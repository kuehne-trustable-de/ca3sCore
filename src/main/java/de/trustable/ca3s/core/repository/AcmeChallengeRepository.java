package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeChallenge;
import de.trustable.ca3s.core.domain.CSR;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the AcmeChallenge entity.
 */
@Repository
public interface AcmeChallengeRepository extends JpaRepository<AcmeChallenge, Long> {

    @Query(name = "AcmeChallenge.findPendingByProxyId")
    List<AcmeChallenge> findPendingByProxyId(@Param("proxyId") long proxyId);

}
