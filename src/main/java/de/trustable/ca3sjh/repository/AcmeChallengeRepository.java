package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.AcmeChallenge;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeChallenge entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeChallengeRepository extends JpaRepository<AcmeChallenge, Long> {

}
