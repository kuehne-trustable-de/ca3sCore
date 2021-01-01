package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Nonce;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the Nonce entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NonceRepository extends JpaRepository<Nonce, Long> {
}
