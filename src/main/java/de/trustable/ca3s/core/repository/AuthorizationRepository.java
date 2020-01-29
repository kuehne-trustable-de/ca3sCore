package de.trustable.ca3s.core.repository;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.AcmeAuthorization;


/**
 * Spring Data  repository for the Authorization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorizationRepository extends JpaRepository<AcmeAuthorization, Long> {
	
	
	@Query(name = "Authorization.findByAuthorizationId")
	List<AcmeAuthorization> findByAuthorizationId(@Param("authorizationId") long authorizationId);

}
