package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CAConnectorConfig entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CAConnectorConfigRepository extends JpaRepository<CAConnectorConfig, Long> {

	
	@Query(name = "CAConnectorConfig.findAllCertGenerators")
	List<CAConnectorConfig> findAllCertGenerators();

	@Query(name = "CAConnectorConfig.findbyName")
	List<CAConnectorConfig> findByName(@Param("name") String name);


}
