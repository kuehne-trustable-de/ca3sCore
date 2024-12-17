package de.trustable.ca3s.core;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.*;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.dto.AcmeConfigItems;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.RDNRestriction;
import de.trustable.ca3s.core.service.dto.SCEPConfigItems;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;


@Service
public class PipelineTestConfiguration {

    private static final String INTERNAL_TEST_CA = "InternalTestCA";

    public static final Logger LOGGER = LogManager.getLogger(PipelineTestConfiguration.class);

    public static final String PIPELINE_NAME_WEB_DIRECT_ISSUANCE = "direct issuance";
    public static final String PIPELINE_NAME_WEB_RA_ISSUANCE = "ra issuance";

    private static final String PIPELINE_NAME_ACME = "acme";
    private static final String PIPELINE_NAME_ACME1CN = "acme1CN";
    private static final String PIPELINE_NAME_ACME1CNNOIP = "acme1CNNoIP";
    private static final String PIPELINE_NAME_SCEP = "scep";
    private static final String PIPELINE_NAME_SCEP1CN = "scep1CN";

    public static final String ACME_REALM = "acmeTest";
    public static final String ACME1CN_REALM = "acmeTest1CN";
    public static final String ACME1CNNOIP_REALM = "acmeTest1CNNoIP";
    public static final String SCEP_REALM = "scepTest";
    public static final String SCEP1CN_REALM = "scepTest1CN";

    public static final String SCEP_PASSWORD = "abc123#*/";

    @Autowired
    CAConnectorConfigRepository cacRepo;

    @Autowired
    PipelineRepository pipelineRepo;

    @Autowired
    PipelineUtil pipelineUtil;

    @Autowired
    private ProtectedContentRepository protectedContentRepository;

    @Autowired
    private ProtectedContentUtil protectedContentUtil;

    @Autowired
    private BPMNUtil bpmnUtil;

    private BPMNProcessInfo simpleBPMNProcessInfo;

    static final String SIMPLE_CERTIFICATE_PROCESS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" id=\"_DdZocL47EeOQo_IRkjDF6w\" targetNamespace=\"http://camunda.org/schema/1.0/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"5.29.0\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n" +
        "  <bpmn2:process id=\"SimpleBPMNProcess\" name=\"Forward CA request to appropriate backend\" isExecutable=\"true\">\n" +
        "    <bpmn2:extensionElements />\n" +
        "    <bpmn2:startEvent id=\"StartEvent_1\" name=\"SimpleCertificateProcess\">\n" +
        "      <bpmn2:outgoing>SequenceFlow_1b8to73</bpmn2:outgoing>\n" +
        "    </bpmn2:startEvent>\n" +
        "    <bpmn2:sequenceFlow id=\"SequenceFlow_1b8to73\" sourceRef=\"StartEvent_1\" targetRef=\"Activity_12ghldc\" />\n" +
        "    <bpmn2:endEvent id=\"EndEvent_1\" name=\"CA Request&#10;Processed\">\n" +
        "      <bpmn2:extensionElements />\n" +
        "      <bpmn2:incoming>Flow_0mgnc32</bpmn2:incoming>\n" +
        "    </bpmn2:endEvent>\n" +
        "    <bpmn2:scriptTask id=\"Activity_12ghldc\" name=\"RetrieveCertificate\" scriptFormat=\"groovy\">\n" +
        "      <bpmn2:extensionElements>\n" +
        "        <camunda:inputOutput>\n" +
        "          <camunda:inputParameter name=\"certificateId\">${certificateId}</camunda:inputParameter>\n" +
        "        </camunda:inputOutput>\n" +
        "      </bpmn2:extensionElements>\n" +
        "      <bpmn2:incoming>SequenceFlow_1b8to73</bpmn2:incoming>\n" +
        "      <bpmn2:outgoing>Flow_0mgnc32</bpmn2:outgoing>\n" +
        "      <bpmn2:multiInstanceLoopCharacteristics>\n" +
        "        <bpmn2:loopCardinality xsi:type=\"bpmn2:tFormalExpression\">1</bpmn2:loopCardinality>\n" +
        "      </bpmn2:multiInstanceLoopCharacteristics>\n" +
        "      <bpmn2:script>\n" +
        "\n" +
        "          println(\"In SimpleCertificateProcess for certificateId: \" + certificateId)\n" +
        "\n" +
        "          Map&lt;String, String[]&gt; selection = new HashMap&lt;String,String[]&gt;(Map.ofEntries(\n" +
        "              Map.entry(\"limit\", [\"1\"] as String[]),\n" +
        "              Map.entry(\"offset\", [\"0\"] as String[]),\n" +
        "              Map.entry(\"attributeName_1\", [\"id\"] as String[]),\n" +
        "              Map.entry(\"attributeValue_1\", [certificateId] as String[]),\n" +
        "              Map.entry(\"attributeSelector_1\", [\"EQUAL\"] as String[]),\n" +
        "              Map.entry(\"filter\", [\"id,subject,issuer,type,keyLength,serialHex,validFrom,validTo,hashAlgorithm,paddingAlgorithm,revoked,revokedSince,revocationReason,sans,root,manager\"] as String[])\n" +
        "              ))\n" +
        "\n" +
        "          var cvList = certificateListResource.getFullCVList(selection)\n" +
        "\n" +
        "          String PATTERN_FORMAT = \"dd.MM.yyyy\"\n" +
        "          java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(PATTERN_FORMAT).withZone(java.time.ZoneId.systemDefault())\n" +
        "\n" +
        "          String payload = \"\"\n" +
        "          execution.setVariable(\"status\", \"NotFound\");\n" +
        "\n" +
        "          cvList.each { it -&gt;\n" +
        "\n" +
        "            for( de.trustable.ca3s.core.service.dto.NamedValue nv: it.getArArr()){\n" +
        "              println( \"add Attribute: \" + nv.getName() + \" -&gt; \" + nv.getValue());\n" +
        "            }\n" +
        "\n" +
        "            Map&lt;String, String&gt; params = new HashMap&lt;&gt;();\n" +
        "            params.put(\"seriennummer\", it.getSerial().toString());\n" +
        "            params.put(\"name\", it.getRdn_cn());\n" +
        "            params.put(\"commonName\", it.getRdn_cn());\n" +
        "\n" +
        "            params.put(\"subjectAlternativeName\", it.getTypedSansString());\n" +
        "            params.put(\"rootCAAnbieter\", it.getRoot_rdn_cn());\n" +
        "            params.put(\"aussteller\", it.getIssuer_rdn_cn());\n" +
        "\n" +
        "            params.put(\"organisationseinheit\", \"organisationseinheit\");\n" +
        "            params.put(\"ersteVerantwortlichePerson\", \"ersteVerantwortlichePerson\");\n" +
        "            params.put(\"zweiteVerantwortlichePerson\", \"zweiteVerantwortlichePerson\");\n" +
        "\n" +
        "            params.put(\"npBedarfstraeger\", it.getArValue(\"Manager\"));\n" +
        "\n" +
        "            params.put(\"npBedarfstraegerZwei\", \"npBedarfstraegerZwei\");\n" +
        "            params.put(\"npBedarfstraegerDrei\", \"npBedarfstraegerDrei\");\n" +
        "            params.put(\"verwendungszweck\", \"verwendungszweck\");\n" +
        "\n" +
        "            if( it.getValidFrom()==null){\n" +
        "              params.put(\"gueltigVon\", \"\");\n" +
        "            }else{\n" +
        "              params.put(\"gueltigVon\",  formatter.format(it.getValidFrom()))\n" +
        "            }\n" +
        "            if( it.getValidTo()==null){\n" +
        "              params.put(\"gueltigBis\", \"\");\n" +
        "            }else{\n" +
        "              params.put(\"gueltigBis\",  formatter.format(it.getValidTo()))\n" +
        "            }\n" +
        "            if( it.getRevokedSince()==null){\n" +
        "              params.put(\"wiederrufenSeit\", \"\");\n" +
        "            }else{\n" +
        "              params.put(\"wiederrufenSeit\",  formatter.format(it.getRevokedSince()))\n" +
        "            }\n" +
        "\n" +
        "            params.put(\"id\", it.getId().toString());\n" +
        "\n" +
        "            payload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(params)\n" +
        "            execution.setVariable(\"status\", \"Success\");\n" +
        "          }\n" +
        "          println(payload)\n" +
        "          execution.setVariable(\"payload\", payload);\n" +
        "</bpmn2:script>\n" +
        "    </bpmn2:scriptTask>\n" +
        "    <bpmn2:sequenceFlow id=\"Flow_0mgnc32\" sourceRef=\"Activity_12ghldc\" targetRef=\"EndEvent_1\" />\n" +
        "  </bpmn2:process>\n" +
        "  <bpmn2:error id=\"Error_0eb6jwi\" name=\"Error_3vlrpis\" />\n" +
        "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
        "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"SimpleBPMNProcess\">\n" +
        "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_3\" bpmnElement=\"StartEvent_1\">\n" +
        "        <dc:Bounds x=\"170\" y=\"114\" width=\"36\" height=\"36\" />\n" +
        "        <bpmndi:BPMNLabel>\n" +
        "          <dc:Bounds x=\"149\" y=\"77\" width=\"80\" height=\"27\" />\n" +
        "        </bpmndi:BPMNLabel>\n" +
        "      </bpmndi:BPMNShape>\n" +
        "      <bpmndi:BPMNShape id=\"EndEvent_0y7erx5_di\" bpmnElement=\"EndEvent_1\">\n" +
        "        <dc:Bounds x=\"952\" y=\"114\" width=\"36\" height=\"36\" />\n" +
        "        <bpmndi:BPMNLabel>\n" +
        "          <dc:Bounds x=\"940\" y=\"77\" width=\"60\" height=\"27\" />\n" +
        "        </bpmndi:BPMNLabel>\n" +
        "      </bpmndi:BPMNShape>\n" +
        "      <bpmndi:BPMNShape id=\"Activity_1paqwpb_di\" bpmnElement=\"Activity_12ghldc\">\n" +
        "        <dc:Bounds x=\"540\" y=\"92\" width=\"100\" height=\"80\" />\n" +
        "        <bpmndi:BPMNLabel />\n" +
        "      </bpmndi:BPMNShape>\n" +
        "      <bpmndi:BPMNEdge id=\"SequenceFlow_1b8to73_di\" bpmnElement=\"SequenceFlow_1b8to73\">\n" +
        "        <di:waypoint x=\"206\" y=\"132\" />\n" +
        "        <di:waypoint x=\"540\" y=\"132\" />\n" +
        "      </bpmndi:BPMNEdge>\n" +
        "      <bpmndi:BPMNEdge id=\"Flow_0mgnc32_di\" bpmnElement=\"Flow_0mgnc32\">\n" +
        "        <di:waypoint x=\"640\" y=\"132\" />\n" +
        "        <di:waypoint x=\"952\" y=\"132\" />\n" +
        "      </bpmndi:BPMNEdge>\n" +
        "    </bpmndi:BPMNPlane>\n" +
        "  </bpmndi:BPMNDiagram>\n" +
        "</bpmn2:definitions>\n";

    public BPMNProcessInfo getSimpleBPMNProcessInfo() {

        if( simpleBPMNProcessInfo == null ) {
            simpleBPMNProcessInfo = addSimpleProcess(SIMPLE_CERTIFICATE_PROCESS,
                "SimpleCertificateProcess",
                BPMNProcessType.CERTIFICATE_NOTIFY);
        }
        return simpleBPMNProcessInfo;
    }

    public CAConnectorConfig internalTestCAC() {

        CAConnectorConfig exampleCCC = new CAConnectorConfig();
        exampleCCC.setName(INTERNAL_TEST_CA);
        Example<CAConnectorConfig> example = Example.of(exampleCCC);

        List<CAConnectorConfig> existingConfigList = cacRepo.findAll(example);

        if (!existingConfigList.isEmpty()) {
            LOGGER.info("CAConnectorConfig for 'Internal' already present");

            return existingConfigList.get(0);
        }

        CAConnectorConfig newCAC = new CAConnectorConfig();
//		newCAC.setId(CONFIG_ID);
        newCAC.setName(INTERNAL_TEST_CA);
        newCAC.setCaConnectorType(CAConnectorType.INTERNAL);
        newCAC.setDefaultCA(true);
        newCAC.setActive(true);
        newCAC.setCheckActive(false);
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

        if (!existingPLList.isEmpty()) {
            LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME);

            return existingPLList.get(0);
        }

        LOGGER.info("------------ Creating pipeline '{}' ... ", PIPELINE_NAME_ACME);

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
        pv_LaxRestrictions.setRestriction_E(new RDNRestriction());
        pv_LaxRestrictions.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);

        pv_LaxRestrictions.setRestriction_SAN(new RDNRestriction());
        pv_LaxRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

        pv_LaxRestrictions.setIpAsSubjectAllowed(true);
        pv_LaxRestrictions.setIpAsSANAllowed(true);

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

        if (!existingPLList.isEmpty()) {
            LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME1CN);

            return existingPLList.get(0);
        }

        LOGGER.info("------------ Creating pipeline '{}' ... ", PIPELINE_NAME_ACME1CN);

        PipelineView pv_1CNRestrictions = new PipelineView();
        pv_1CNRestrictions.setRestriction_C(new RDNRestriction());
        pv_1CNRestrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
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
        pv_1CNRestrictions.setRestriction_E(new RDNRestriction());
        pv_1CNRestrictions.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

        pv_1CNRestrictions.setRestriction_SAN(new RDNRestriction());
        pv_1CNRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

        pv_1CNRestrictions.setApprovalRequired(false);

        pv_1CNRestrictions.setCaConnectorName(internalTestCAC().getName());
        pv_1CNRestrictions.setName(PIPELINE_NAME_ACME1CN);
        pv_1CNRestrictions.setActive(true);
        pv_1CNRestrictions.setType(PipelineType.ACME);
        pv_1CNRestrictions.setUrlPart(ACME1CN_REALM);

        pv_1CNRestrictions.setTosAgreementRequired(true);
        pv_1CNRestrictions.setTosAgreementLink("http://to.agreement.link/index.html");

        Pipeline pipelineRestrictions = pipelineUtil.toPipeline(pv_1CNRestrictions);
        pipelineRepo.save(pipelineRestrictions);
        return pipelineRestrictions;
    }

    @Transactional
    public Pipeline getInternalACMETestPipeline_1_CN_ONLY_NO_IP_Restrictions() {

        Pipeline examplePipeline = new Pipeline();
        examplePipeline.setName(PIPELINE_NAME_ACME1CNNOIP);
        examplePipeline.setActive(true);
        Example<Pipeline> example = Example.of(examplePipeline);
        List<Pipeline> existingPLList = pipelineRepo.findAll(example);

        if (!existingPLList.isEmpty()) {
            LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_ACME1CNNOIP);
            return existingPLList.get(0);
        }

        LOGGER.info("------------ Creating pipeline '{}' ... ", PIPELINE_NAME_ACME1CNNOIP);

        PipelineView pv_1CN_NoIP_Restrictions = new PipelineView();
        pv_1CN_NoIP_Restrictions.setRestriction_C(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
        pv_1CN_NoIP_Restrictions.setRestriction_CN(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ONE);
        pv_1CN_NoIP_Restrictions.setRestriction_L(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
        pv_1CN_NoIP_Restrictions.setRestriction_O(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
        pv_1CN_NoIP_Restrictions.setRestriction_OU(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
        pv_1CN_NoIP_Restrictions.setRestriction_S(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
        pv_1CN_NoIP_Restrictions.setRestriction_E(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

        pv_1CN_NoIP_Restrictions.setRestriction_SAN(new RDNRestriction());
        pv_1CN_NoIP_Restrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

        pv_1CN_NoIP_Restrictions.setIpAsSubjectAllowed(false);
        pv_1CN_NoIP_Restrictions.setIpAsSANAllowed(false);

        pv_1CN_NoIP_Restrictions.setApprovalRequired(false);

        pv_1CN_NoIP_Restrictions.setCaConnectorName(internalTestCAC().getName());
        pv_1CN_NoIP_Restrictions.setName(PIPELINE_NAME_ACME1CNNOIP);
        pv_1CN_NoIP_Restrictions.setActive(true);
        pv_1CN_NoIP_Restrictions.setType(PipelineType.ACME);
        pv_1CN_NoIP_Restrictions.setUrlPart(ACME1CNNOIP_REALM);

        AcmeConfigItems acmeConfigItems = new AcmeConfigItems();
        acmeConfigItems.setContactEMailRegEx("(mailto:.*@ca3s\\.org)");
        acmeConfigItems.setContactEMailRejectRegEx("(mailto:root@ca3s\\.org|mailto:mail@ca3s\\.org|mailto:service.*@ca3s\\.org)");

        pv_1CN_NoIP_Restrictions.setAcmeConfigItems(acmeConfigItems);

        Pipeline pipelineRestrictions = pipelineUtil.toPipeline(pv_1CN_NoIP_Restrictions);
        pipelineRepo.save(pipelineRestrictions);
        return pipelineRestrictions;
    }


    @Transactional
    public Pipeline getInternalWebDirectTestPipeline() {

        Pipeline examplePipeline = new Pipeline();
        examplePipeline.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        examplePipeline.setActive(true);
        Example<Pipeline> example = Example.of(examplePipeline);
        List<Pipeline> existingPLList = pipelineRepo.findAll(example);

        if (!existingPLList.isEmpty()) {
            LOGGER.info("Pipeline '{}' already present", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);

            return existingPLList.get(0);
        }

        LOGGER.info("------------ Creating pipeline '{}' ... ", PIPELINE_NAME_WEB_DIRECT_ISSUANCE);


        Pipeline pipelineWeb = new Pipeline();
        pipelineWeb.setActive(true);
        pipelineWeb.setApprovalRequired(false);

        pipelineWeb.setCaConnector(internalTestCAC());
        pipelineWeb.setName(PIPELINE_NAME_WEB_DIRECT_ISSUANCE);
        pipelineWeb.setType(PipelineType.WEB);
        pipelineWeb.setUrlPart("test");

        addPipelineAttribute(pipelineWeb, PipelineUtil.ALLOW_IP_AS_SAN, "false");

        pipelineWeb.setProcessInfoNotify(getSimpleBPMNProcessInfo());

        pipelineRepo.save(pipelineWeb);

        return pipelineWeb;
    }

    public void addPipelineAttribute(Pipeline p, String name, String value) {

        PipelineAttribute pAtt = new PipelineAttribute();
        pAtt.setPipeline(p);
        pAtt.setName(name);
        pAtt.setValue(value);

        if (p.getPipelineAttributes() == null) {
            p.setPipelineAttributes(new HashSet<>());
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

        if (!existingPLList.isEmpty()) {
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

        if (!existingPLList.isEmpty()) {
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

        if (!existingPLList.isEmpty()) {
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
        pv_LaxRestrictions.setRestriction_E(new RDNRestriction());
        pv_LaxRestrictions.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);

        pv_LaxRestrictions.setRestriction_SAN(new RDNRestriction());
        pv_LaxRestrictions.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);

        pv_LaxRestrictions.setApprovalRequired(false);

        pv_LaxRestrictions.setCaConnectorName(internalTestCAC().getName());
        pv_LaxRestrictions.setName(PIPELINE_NAME_SCEP);
        pv_LaxRestrictions.setType(PipelineType.SCEP);
        pv_LaxRestrictions.setActive(true);
        pv_LaxRestrictions.setUrlPart(SCEP_REALM);


        ProtectedContent pc = new ProtectedContent();
        pc.setType(ProtectedContentType.PASSWORD);
        pc.setRelationType(ContentRelationType.SCEP_PW);
        pc.setCreatedOn(Instant.now());
        pc.setLeftUsages(-1);
        pc.setValidTo(ProtectedContentUtil.MAX_INSTANT);
        pc.setDeleteAfter(ProtectedContentUtil.MAX_INSTANT);
        pc.setContentBase64(protectedContentUtil.protectString(SCEP_PASSWORD));
        protectedContentRepository.save(pc);

        SCEPConfigItems scepConfigItems = new SCEPConfigItems();
        scepConfigItems.setScepSecret(SCEP_PASSWORD);
        scepConfigItems.setScepSecretPCId(String.valueOf(pc.getId()));
        pv_LaxRestrictions.setScepConfigItems(scepConfigItems);

        Pipeline pipelineLaxRestrictions = pipelineUtil.toPipeline(pv_LaxRestrictions);
        pipelineRepo.save(pipelineLaxRestrictions);

        pc.setRelatedId(pipelineLaxRestrictions.getId());
        protectedContentRepository.save(pc);

        return pipelineLaxRestrictions;

    }

    @Transactional
    public Pipeline getInternalSCEPTestPipelineCN1Restrictions() {

        Pipeline examplePipeline = new Pipeline();
        examplePipeline.setName(PIPELINE_NAME_SCEP1CN);
        examplePipeline.setActive(true);

        Example<Pipeline> example = Example.of(examplePipeline);
        List<Pipeline> existingPLList = pipelineRepo.findAll(example);

        if (!existingPLList.isEmpty()) {
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
        pv_1CNRestrictions.setRestriction_E(new RDNRestriction());
        pv_1CNRestrictions.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);

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

    public BPMNProcessInfo addSimpleProcess(String contentXML, String name, BPMNProcessType type) {


        String processDefinitionId = bpmnUtil.addModel(contentXML, name);
        LOGGER.debug("Deployed bpmn document with processDefinitionId {} successfully", processDefinitionId);

        BPMNProcessInfo bpmnProcessInfo = bpmnUtil.buildBPMNProcessInfoByProcessId(processDefinitionId, name, type);

        return bpmnProcessInfo;
    }

}
