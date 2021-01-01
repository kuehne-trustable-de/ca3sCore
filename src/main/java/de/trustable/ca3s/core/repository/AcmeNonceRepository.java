package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeNonce;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the AcmeNonce entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeNonceRepository extends JpaRepository<AcmeNonce, Long> {
}
