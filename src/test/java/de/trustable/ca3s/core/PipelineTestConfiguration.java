package de.trustable.ca3s.core;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.domain.Example;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;


@Service
public class PipelineTestConfiguration {

	private static final String INTERNAL_TEST_CA = "InternalTestCA";

	public static final Logger LOGGER = LogManager.getLogger(PipelineTestConfiguration.class);

	public static final String PIPELINE_NAME_WEB_DIRECT_ISSUANCE = "direct issuance";
	public static final String PIPELINE_NAME_WEB_RA_ISSUANCE = "ra issuance";

	private static final String PIPELINE_NAME_ACME = "acme";

	@Autowired
	CAConnectorConfigRepository cacRepo;
	
	@Autowired
	PipelineRepository pipelineRepo;
	

	public CAConnectorConfig internalTestCAC() {
		
		CAConnectorConfig exampleCCC = new CAConnectorConfig();
		exampleCCC.setName(INTERNAL_TEST_CA);
		Example<CAConnectorConfig> example = Example.of(exampleCCC);
		
		List<CAConnectorConfig> existingConfigList = cacRepo.findAll(example);
		
		if( !existingConfigList.isEmpty()) {
			LOGGER.info("CAConnectorConfig for 'Internal' already present");

			return existingConfigList.get(0);
		}
		
		CAConnectorConfig newCAC = new CAConnectorConfig();
//		newCAC.setId(CONFIG_ID);
		newCAC.setName(INTERNAL_TEST_CA);
		newCAC.setCaConnectorType(CAConnectorType.INTERNAL);
		newCAC.setDefaultCA(true);
		newCAC.setActive(true);
		cacRepo.save(newCAC);
		LOGGER.info("CAConnectorConfig for 'Internal' created");
		return newCAC;
		
		
	}
	

	public Pipeline getInternalACMETestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_ACME);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);
		
		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME);

			return existingPLList.get(0);
		}
		

		Pipeline pipelineACME = new Pipeline();
		pipelineACME.setApprovalRequired(false);
		
		pipelineACME.setCaConnector(internalTestCAC());
		pipelineACME.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		pipelineACME.setType(PipelineType.ACME);
		pipelineACME.setUrlPart("test");
		pipelineRepo.save(pipelineACME);
		return pipelineACME;
	}
	
	public Pipeline getInternalWebDirectTestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);
		
		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

			return existingPLList.get(0);
		}
		

		Pipeline pipelineWeb = new Pipeline();
		pipelineWeb.setApprovalRequired(false);
		
		pipelineWeb.setCaConnector(internalTestCAC());
		pipelineWeb.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		pipelineWeb.setType(PipelineType.WEB);
		pipelineWeb.setUrlPart("test");
		pipelineRepo.save(pipelineWeb);
		return pipelineWeb;
	}
	
	public Pipeline getInternalWebRATestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);
		
		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

			return existingPLList.get(0);
		}
		

		Pipeline pipelineWeb = new Pipeline();
		pipelineWeb.setApprovalRequired(false);
		
		pipelineWeb.setCaConnector(internalTestCAC());
		pipelineWeb.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		pipelineWeb.setType(PipelineType.WEB);
		pipelineWeb.setUrlPart("test");
		pipelineRepo.save(pipelineWeb);
		return pipelineWeb;
	}
}
