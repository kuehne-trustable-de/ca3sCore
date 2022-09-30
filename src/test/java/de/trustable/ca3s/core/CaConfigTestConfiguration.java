package de.trustable.ca3s.core;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.scep.ScepServiceIT;

@TestConfiguration
public class CaConfigTestConfiguration {

	public static final Logger LOGGER = LogManager.getLogger(CaConfigTestConfiguration.class);

	public static final long CONFIG_ID = 222L;
	
	@Autowired
	CAConnectorConfigRepository cacRepo;
	
	@Bean
	CAConnectorConfig getInternalTestCAC() {
		Optional<CAConnectorConfig> existingConfigOpt = cacRepo.findById(CONFIG_ID);
		if( existingConfigOpt.isPresent()) {
			LOGGER.debug("CAConnectorConfig for 'Internal' already present");

			return existingConfigOpt.get();
		}
		
		CAConnectorConfig newCAC = new CAConnectorConfig();
		newCAC.setId(CONFIG_ID);
		newCAC.setName("InternalTestCA");
		newCAC.setCaConnectorType(CAConnectorType.INTERNAL);
		newCAC.setDefaultCA(true);
		newCAC.setActive(true);
		cacRepo.save(newCAC);
		LOGGER.debug("CAConnectorConfig for 'Internal' created");
		return newCAC;
		
		
	}
}
