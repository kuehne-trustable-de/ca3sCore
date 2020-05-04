package de.trustable.ca3s.core;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.scep.ScepServiceIT;

@TestConfiguration
public class PipelineTestConfiguration {

	public static final Logger LOGGER = LogManager.getLogger(PipelineTestConfiguration.class);

	public static final long CONFIG_ID = 222L;
	public static final long PIPELINE_ID = 223L;

	@Autowired
	CAConnectorConfigRepository cacRepo;
	
	@Autowired
	PipelineRepository pipelineRepo;
	
	@Autowired
	CAConnectorConfig caConfig;
	
	
	@Bean
	CAConnectorConfig internalTestCAC() {
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
	
	@Bean
	@DependsOn("internalTestCAC")
	Pipeline getInternalACMETestPipeline() {

		
		Optional<Pipeline> existingPipelineOpt = pipelineRepo.findById(PIPELINE_ID);
		if( existingPipelineOpt.isPresent()) {
			LOGGER.debug("Pipeline already present");
			return existingPipelineOpt.get();
		}

//		Optional<CAConnectorConfig> existingConfigOpt = cacRepo.findById(CaConfigTestConfiguration.CONFIG_ID);
//		if( existingConfigOpt.isPresent()) {
			Pipeline pipelineACME = new Pipeline();
			pipelineACME.setId(PIPELINE_ID);
			pipelineACME.setApprovalRequired(false);
			
//			pipelineACME.setCaConnector(existingConfigOpt.get());
			pipelineACME.setCaConnector(internalTestCAC());
			pipelineACME.setName("ACME_TEST");
			pipelineACME.setType(PipelineType.ACME);
			pipelineACME.setUrlPart("test");
			pipelineRepo.save(pipelineACME);
			return pipelineACME;
//		}

//		LOGGER.error("CAConfig not existing!");
//		return null;
	}
}
