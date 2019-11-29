package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.ACMEAccount;
import de.trustable.ca3sjh.domain.CAConnectorConfig;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CAConnectorConfig entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CAConnectorConfigRepository extends JpaRepository<CAConnectorConfig, Long> {

	@Query(name = "CAConnectorConfig.findDefaultCA")
	List<CAConnectorConfig> findDefaultCA();

}
