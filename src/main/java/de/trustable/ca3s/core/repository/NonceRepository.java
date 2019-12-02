package de.trustable.ca3s.core.repository;


import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.Nonce;


/**
 * Spring Data  repository for the Nonce entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NonceRepository extends JpaRepository<Nonce, Long> {

	@Query(name = "Nonce.findByNonceValue")
	List<Nonce> findByNonceValue(@Param("nonceValue") String nonceValue);

	@Query(name = "Nonce.findByNonceExpiredBefore")
	List<Nonce> findByNonceExpiryDate(@Param("expiredBefore") Date expiredBefore);


}
