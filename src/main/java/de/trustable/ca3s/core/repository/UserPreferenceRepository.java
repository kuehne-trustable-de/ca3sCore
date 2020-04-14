package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.UserPreference;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the UserPreference entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {

	@Query(name = "UserPreference.findByNameforUser")
	Optional<UserPreference> findByNameforUser(@Param("name") String name, @Param("userId") Long userId);

}
