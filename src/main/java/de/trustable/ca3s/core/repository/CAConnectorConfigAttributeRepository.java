package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CAConnectorConfigAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CAConnectorConfigAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CAConnectorConfigAttributeRepository extends JpaRepository<CAConnectorConfigAttribute, Long> {}
