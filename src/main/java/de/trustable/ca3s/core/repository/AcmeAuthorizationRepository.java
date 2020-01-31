package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeAuthorization;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeAuthorization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeAuthorizationRepository extends JpaRepository<AcmeAuthorization, Long> {

	@Query(name = "Authorization.findByAcmeAuthorizationId")
	List<AcmeAuthorization> findByAcmeAuthorizationId(@Param("acmeAuthorizationId") long acmeAuthorizationId);

}
