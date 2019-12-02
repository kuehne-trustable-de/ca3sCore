package de.trustable.ca3s.core.repository;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.domain.CAConnectorConfig;


/**
 * Spring Data  repository for the CAConnectorConfig entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CAConnectorConfigRepository extends JpaRepository<CAConnectorConfig, Long> {

	@Query(name = "CAConnectorConfig.findDefaultCA")
	List<CAConnectorConfig> findDefaultCA();

}
