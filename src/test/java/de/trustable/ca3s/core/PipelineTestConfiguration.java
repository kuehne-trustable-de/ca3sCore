package de.trustable.ca3s.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrUsage;
import de.trustable.ca3s.core.service.dto.SCEPConfigItems;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.RDNRestriction;
import de.trustable.ca3s.core.service.util.PipelineUtil;


@Service
public class PipelineTestConfiguration {

	private static final String INTERNAL_TEST_CA = "InternalTestCA";

	public static final Logger LOGGER = LogManager.getLogger(PipelineTestConfiguration.class);

	public static final String PIPELINE_NAME_WEB_DIRECT_ISSUANCE = "direct issuance";
	public static final String PIPELINE_NAME_WEB_RA_ISSUANCE = "ra issuance";

	private static final String PIPELINE_NAME_ACME = "acme";
	private static final String PIPELINE_NAME_ACME1CN = "acme1CN";
	private static final String PIPELINE_NAME_SCEP = "scep";
	private static final String PIPELINE_NAME_SCEP1CN = "scep1CN";

	public static final String ACME_REALM = "acmeTest";
	public static final String ACME1CN_REALM = "acmeTest1CN";
	public static final String SCEP_REALM = "scepTest";
	public static final String SCEP1CN_REALM = "scepTest1CN";

    public static final String SCEP_PASSWORD = "abc123#*/";

    @Autowired
	CAConnectorConfigRepository cacRepo;

	@Autowired
	PipelineRepository pipelineRepo;

	@Autowired
	PipelineUtil pipelineUtil;


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

	@Transactional
	public Pipeline getInternalACMETestPipelineLaxRestrictions() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_ACME);
        examplePipeline.setActive(true);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME);

			return existingPLList.get(0);
		}


		PipelineView pv_LaxRestrictions = new PipelineView();
    	pv_LaxRestrictions.setRestriction_C(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_CN(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_L(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_O(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_OU(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
    	pv_LaxRestrictions.setRestriction_S(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);

    	pv_LaxRestrictions.setRestriction_SAN(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

		pv_LaxRestrictions.setApprovalRequired(false);

		pv_LaxRestrictions.setCaConnectorName(internalTestCAC().getName());
		pv_LaxRestrictions.setName(PIPELINE_NAME_ACME);
        pv_LaxRestrictions.setActive(true);
		pv_LaxRestrictions.setType(PipelineType.ACME);
		pv_LaxRestrictions.setUrlPart(ACME_REALM);

		Pipeline pipelineLaxRestrictions = pipelineUtil.toPipeline(pv_LaxRestrictions);
		pipelineRepo.save(pipelineLaxRestrictions);
		return pipelineLaxRestrictions;
	}

	@Transactional
	public Pipeline getInternalACMETestPipeline_1_CN_ONLY_Restrictions() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_ACME1CN);
        examplePipeline.setActive(true);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME1CN);

			return existingPLList.get(0);
		}


		PipelineView pv_1CNRestrictions = new PipelineView();
    	pv_1CNRestrictions.setRestriction_C(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
    	pv_1CNRestrictions.setRestriction_CN(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_L(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_O(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_OU(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_S(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

		pv_1CNRestrictions.setRestriction_SAN(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);


		pv_1CNRestrictions.setApprovalRequired(false);

		pv_1CNRestrictions.setCaConnectorName(internalTestCAC().getName());
		pv_1CNRestrictions.setName(PIPELINE_NAME_ACME1CN);
        pv_1CNRestrictions.setActive(true);
		pv_1CNRestrictions.setType(PipelineType.ACME);
		pv_1CNRestrictions.setUrlPart(ACME1CN_REALM);

		Pipeline pipelineLaxRestrictions = pipelineUtil.toPipeline(pv_1CNRestrictions);
		pipelineRepo.save(pipelineLaxRestrictions);
		return pipelineLaxRestrictions;
	}


	@Transactional
	public Pipeline getInternalWebDirectTestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        examplePipeline.setActive(true);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

			return existingPLList.get(0);
		}


		Pipeline pipelineWeb = new Pipeline();
        pipelineWeb.setActive(true);
		pipelineWeb.setApprovalRequired(false);

		pipelineWeb.setCaConnector(internalTestCAC());
		pipelineWeb.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		pipelineWeb.setType(PipelineType.WEB);
		pipelineWeb.setUrlPart("test");

        addPipelineAttribute(pipelineWeb, PipelineUtil.ALLOW_IP_AS_SAN, "false");

        pipelineRepo.save(pipelineWeb);

		return pipelineWeb;
	}

    public void addPipelineAttribute(Pipeline p, String name, String value) {

        PipelineAttribute pAtt = new PipelineAttribute();
        pAtt.setPipeline(p);
        pAtt.setName(name);
        pAtt.setValue(value);

        if( p.getPipelineAttributes() == null){
            p.setPipelineAttributes( new HashSet<PipelineAttribute>() );
        }
        p.getPipelineAttributes().add(pAtt);

    }

    @Transactional
	public Pipeline getInternalWebRACheckTestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_WEB_RA_ISSUANCE);
        examplePipeline.setActive(true);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_RA_ISSUANCE);

			return existingPLList.get(0);
		}


		Pipeline pipelineWeb = new Pipeline();
		pipelineWeb.setApprovalRequired(true);
        pipelineWeb.setActive(true);

		pipelineWeb.setCaConnector(internalTestCAC());
		pipelineWeb.setName(PIPELINE_NAME_WEB_RA_ISSUANCE);
		pipelineWeb.setType(PipelineType.WEB);
		pipelineWeb.setUrlPart("test");
		pipelineRepo.save(pipelineWeb);
		return pipelineWeb;
	}

	@Transactional
	public Pipeline getInternalWebRATestPipeline() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        examplePipeline.setActive(true);
		Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

			return existingPLList.get(0);
		}


		Pipeline pipelineWeb = new Pipeline();
		pipelineWeb.setApprovalRequired(false);
        pipelineWeb.setActive(true);

		pipelineWeb.setCaConnector(internalTestCAC());
		pipelineWeb.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
		pipelineWeb.setType(PipelineType.WEB);
		pipelineWeb.setUrlPart("test");
		pipelineRepo.save(pipelineWeb);
		return pipelineWeb;
	}


	@Transactional
	public Pipeline getInternalSCEPTestPipelineLaxRestrictions() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_SCEP);
        examplePipeline.setActive(true);

        Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_SCEP);

			return existingPLList.get(0);
		}

		PipelineView pv_LaxRestrictions = new PipelineView();
    	pv_LaxRestrictions.setRestriction_C(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_CN(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_L(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_O(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv_LaxRestrictions.setRestriction_OU(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
    	pv_LaxRestrictions.setRestriction_S(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);

		pv_LaxRestrictions.setRestriction_SAN(new RDNRestriction());
		pv_LaxRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

		pv_LaxRestrictions.setApprovalRequired(false);

		pv_LaxRestrictions.setCaConnectorName(internalTestCAC().getName());
		pv_LaxRestrictions.setName(PIPELINE_NAME_SCEP);
		pv_LaxRestrictions.setType(PipelineType.SCEP);
        pv_LaxRestrictions.setActive(true);
		pv_LaxRestrictions.setUrlPart(SCEP_REALM);

        SCEPConfigItems scepConfigItems = new SCEPConfigItems();
        scepConfigItems.setScepSecret(SCEP_PASSWORD);
        pv_LaxRestrictions.setScepConfigItems(scepConfigItems);

		Pipeline pipelineLaxRestrictions = pipelineUtil.toPipeline(pv_LaxRestrictions);
		pipelineRepo.save(pipelineLaxRestrictions);
		return pipelineLaxRestrictions;

	}

	@Transactional
	public Pipeline getInternalSCEPTestPipelineCN1Restrictions() {

		Pipeline examplePipeline = new Pipeline();
		examplePipeline.setName(PIPELINE_NAME_SCEP1CN);
        examplePipeline.setActive(true);

        Example<Pipeline> example = Example.of(examplePipeline);
		List<Pipeline> existingPLList = pipelineRepo.findAll(example);

		if( !existingPLList.isEmpty()) {
			LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_SCEP1CN);

			return existingPLList.get(0);
		}

		PipelineView pv_1CNRestrictions = new PipelineView();
    	pv_1CNRestrictions.setRestriction_C(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_CN(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
    	pv_1CNRestrictions.setRestriction_L(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_O(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_OU(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv_1CNRestrictions.setRestriction_S(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

		pv_1CNRestrictions.setRestriction_SAN(new RDNRestriction());
		pv_1CNRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

		pv_1CNRestrictions.setApprovalRequired(false);

		pv_1CNRestrictions.setCaConnectorName(internalTestCAC().getName());
		pv_1CNRestrictions.setName(PIPELINE_NAME_SCEP1CN);
		pv_1CNRestrictions.setType(PipelineType.SCEP);
        pv_1CNRestrictions.setActive(true);
		pv_1CNRestrictions.setUrlPart(SCEP1CN_REALM);

        SCEPConfigItems scepConfigItems = new SCEPConfigItems();
        scepConfigItems.setScepSecret(SCEP_PASSWORD);
        pv_1CNRestrictions.setScepConfigItems(scepConfigItems);

        Pipeline pipelineLaxRestrictions = pipelineUtil.toPipeline(pv_1CNRestrictions);
		pipelineRepo.save(pipelineLaxRestrictions);
		return pipelineLaxRestrictions;

	}
}
