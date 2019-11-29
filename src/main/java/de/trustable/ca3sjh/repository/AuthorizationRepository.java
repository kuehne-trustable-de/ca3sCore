package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.Authority;
import de.trustable.ca3sjh.domain.Authorization;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Authorization entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuthorizationRepository extends JpaRepository<Authorization, Long> {
	
	
	@Query(name = "Authorization.findByAuthorizationId")
	List<Authorization> findByAuthorizationId(@Param("authorizationId") long authorizationId);

}
