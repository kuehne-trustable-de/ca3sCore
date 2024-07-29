package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeNonce;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeNonce entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeNonceRepository extends JpaRepository<AcmeNonce, Long> {


	@Query(name = "AcmeNonce.findByNonceValue")
	List<AcmeNonce> findByNonceValue(@Param("nonceValue") String nonceValue);

	@Query(name = "AcmeNonce.findByNonceExpiredBefore")
    Page<AcmeNonce> findByNonceExpiryDate(Pageable pageable, @Param("expiredBefore") Instant expiredBefore);

}
