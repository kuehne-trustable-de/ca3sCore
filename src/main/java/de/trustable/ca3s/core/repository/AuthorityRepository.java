package de.trustable.ca3s.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import de.trustable.ca3s.core.domain.Authority;

/**
 * Spring Data JPA repository for the {@link Authority} entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
	

}
