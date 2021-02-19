package de.trustable.ca3s.core.service.util;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;


@Service
public class PipelineUtil {


	public static final String RESTR_C_CARDINALITY = "RESTR_C_CARDINALITY";
	public static final String RESTR_C_TEMPLATE = "RESTR_C_TEMPLATE";
	public static final String RESTR_C_REGEXMATCH = "RESTR_C_REGEXMATCH";
	public static final String RESTR_CN_CARDINALITY = "RESTR_CN_CARDINALITY";
	public static final String RESTR_CN_TEMPLATE = "RESTR_CN_TEMPLATE";
	public static final String RESTR_CN_REGEXMATCH = "RESTR_CN_REGEXMATCH";

	public static final String RESTR_O_CARDINALITY = "RESTR_O_CARDINALITY";
	public static final String RESTR_O_TEMPLATE = "RESTR_O_TEMPLATE";
	public static final String RESTR_O_REGEXMATCH = "RESTR_O_REGEXMATCH";
	public static final String RESTR_OU_CARDINALITY = "RESTR_OU_CARDINALITY";
	public static final String RESTR_OU_TEMPLATE = "RESTR_OU_TEMPLATE";
	public static final String RESTR_OU_REGEXMATCH = "RESTR_OU_REGEXMATCH";

	public static final String RESTR_L_CARDINALITY = "RESTR_L_CARDINALITY";
	public static final String RESTR_L_TEMPLATE = "RESTR_L_TEMPLATE";
	public static final String RESTR_L_REGEXMATCH = "RESTR_L_REGEXMATCH";
	public static final String RESTR_S_CARDINALITY = "RESTR_S_CARDINALITY";
	public static final String RESTR_S_TEMPLATE = "RESTR_S_TEMPLATE";
	public static final String RESTR_S_REGEXMATCH = "RESTR_S_REGEXMATCH";

	public static final String RESTR_SAN_CARDINALITY = "RESTR_SAN_CARDINALITY";
	public static final String RESTR_SAN_TEMPLATE = "RESTR_SAN_TEMPLATE";
	public static final String RESTR_SAN_REGEXMATCH = "RESTR_SAN_REGEXMATCH";

	public static final String RESTR_ARA_PREFIX = "RESTR_ARA_";
	public static final String RESTR_ARA_PATTERN = RESTR_ARA_PREFIX + "(.*)_(.*)";
	public static final String RESTR_ARA_NAME = "NAME";
//	public static final String RESTR_ARA_CARDINALITY = "CARDINALITY";
	public static final String RESTR_ARA_TEMPLATE = "TEMPLATE";
	public static final String RESTR_ARA_REGEXMATCH = "REGEXMATCH";
	public static final String RESTR_ARA_REQUIRED = "REQUIRED";

	public static final String ALLOW_IP_AS_SUBJECT = "ALLOW_IP_AS_SUBJECT";
	public static final String ALLOW_IP_AS_SAN = "ALLOW_IP_AS_SAN";
	public static final String TO_PENDIND_ON_FAILED_RESTRICTIONS = "TO_PENDIND_ON_FAILED_RESTRICTIONS";

	public static final String ACME_ALLOW_CHALLENGE_HTTP01 = "ACME_ALLOW_CHALLENGE_HTTP01";
	public static final String ACME_ALLOW_CHALLENGE_DNS = "ACME_ALLOW_CHALLENGE_DNS";

	public static final String ACME_ALLOW_CHALLENGE_WILDCARDS = "ACME_ALLOW_WILDCARDS";

	public static final String ACME_CHECK_CAA = "ACME_CHECK_CAA";

	public static final String ACME_NAME_CAA = "ACME_NAME_CAA";

	public static final String ACME_PROCESS_ACCOUNT_VALIDATION = "ACME_PROCESS_ACCOUNT_VALIDATION";
	public static final String ACME_PROCESS_ORDER_VALIDATION = "ACME_PROCESS_ORDER_VALIDATION";
	public static final String ACME_PROCESS_CHALLENGE_VALIDATION = "ACME_PROCESS_CHALLENGE_VALIDATION";

    public static final String SCEP_CAPABILITY_RENEWAL = "SCEP_CAPABILITY_RENEWAL";
    public static final String SCEP_CAPABILITY_POST = "SCEP_CAPABILITY_POST";
    public static final String SCEP_SECRET = "SCEP_SECRET";
    public static final String SCEP_SECRET_VALID_TO = "SCEP_SECRET_VALID_TO";
    public static final String SCEP_SECRET_PC_ID = "SCEP_SECRET_PC_ID";


    Logger LOG = LoggerFactory.getLogger(PipelineUtil.class);

	@Autowired
	private CAConnectorConfigRepository caConnRepository;

	@Autowired
    private PipelineRepository pipelineRepository;

	@Autowired
	private PipelineAttributeRepository pipelineAttRepository;

    @Autowired
    private BPMNProcessInfoRepository bpmnPIRepository;

    @Autowired
    private ProtectedContentRepository protectedContentRepository;

    @Autowired
    private ProtectedContentUtil protectedContentUtil;

    @Autowired
    private AuditService auditService;

    public PipelineView from(Pipeline pipeline) {

    	PipelineView pv = new PipelineView();

    	pv.setId(pipeline.getId());
    	pv.setName(pipeline.getName());
    	pv.setType( pipeline.getType());
    	pv.setDescription( pipeline.getDescription());
    	pv.setApprovalRequired( pipeline.isApprovalRequired());
    	pv.setUrlPart(pipeline.getUrlPart());

    	if( pipeline.getCaConnector()!= null) {
    		pv.setCaConnectorName(pipeline.getCaConnector().getName());
    	}

    	if( pipeline.getProcessInfo() != null) {
    		pv.setProcessInfoName(pipeline.getProcessInfo().getName());
    	}

    	RDNRestriction[] rdnRestrictArr = new RDNRestriction[7];

		RDNRestriction rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("C");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_C(rdnRestrict);
    	rdnRestrictArr[0] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("CN");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_CN(rdnRestrict);
    	rdnRestrictArr[1] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("O");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_O(rdnRestrict);
    	rdnRestrictArr[2] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("OU");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
    	pv.setRestriction_OU(rdnRestrict);
    	rdnRestrictArr[3] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("L");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_L(rdnRestrict);
    	rdnRestrictArr[4] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("ST");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_S(rdnRestrict);
    	rdnRestrictArr[5] = rdnRestrict;

		rdnRestrict = new RDNRestriction();
		rdnRestrict.setRdnName("SAN");
		rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
    	pv.setRestriction_SAN(rdnRestrict);
    	rdnRestrictArr[6] = rdnRestrict;

		pv.setRdnRestrictions(rdnRestrictArr);


		pv.setAraRestrictions(new ARARestriction[0]);

    	ACMEConfigItems acmeConfigItems = new ACMEConfigItems();
        SCEPConfigItems scepConfigItems = new SCEPConfigItems();

//    	acmeConfigItems.setProcessInfoNameAccountValidation(processInfoNameAccountValidation);

    	for( PipelineAttribute plAtt: pipeline.getPipelineAttributes()) {

    		if( ACME_ALLOW_CHALLENGE_HTTP01.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowChallengeHTTP01(Boolean.valueOf(plAtt.getValue()));

    		}else if( ACME_ALLOW_CHALLENGE_DNS.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowChallengeDNS(Boolean.valueOf(plAtt.getValue()));

    		}else if( ACME_ALLOW_CHALLENGE_WILDCARDS.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowWildcards(Boolean.valueOf(plAtt.getValue()));

    		}else if( ACME_CHECK_CAA.equals(plAtt.getName())) {
    			acmeConfigItems.setCheckCAA(Boolean.valueOf(plAtt.getValue()));

    		}else if( ACME_NAME_CAA.equals(plAtt.getName())) {
    			acmeConfigItems.setCaNameCAA(plAtt.getValue());

    		}else if( ALLOW_IP_AS_SUBJECT.equals(plAtt.getName())) {
    			pv.setIpAsSubjectAllowed(Boolean.valueOf(plAtt.getValue()));

    		}else if( ALLOW_IP_AS_SAN.equals(plAtt.getName())) {
    			pv.setIpAsSANAllowed(Boolean.valueOf(plAtt.getValue()));

    		}else if( RESTR_C_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_C_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_C().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_C_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_C().setRegExMatch(Boolean.valueOf(plAtt.getValue()));
    		}else if( RESTR_CN_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_CN_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_CN().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_CN_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_CN().setRegExMatch(Boolean.valueOf(plAtt.getValue()));

    		}else if( RESTR_O_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_O_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_O().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_O_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_O().setRegExMatch(Boolean.valueOf(plAtt.getValue()));
    		}else if( RESTR_OU_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_OU_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_OU().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_OU_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_OU().setRegExMatch(Boolean.valueOf(plAtt.getValue()));

    		}else if( RESTR_L_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_L_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_L().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_L_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_L().setRegExMatch(Boolean.valueOf(plAtt.getValue()));
    		}else if( RESTR_S_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_S_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_S().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_S_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_S().setRegExMatch(Boolean.valueOf(plAtt.getValue()));

    		}else if( RESTR_SAN_CARDINALITY.equals(plAtt.getName())) {
				pv.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
    		}else if( RESTR_SAN_TEMPLATE.equals(plAtt.getName())) {
    			pv.getRestriction_SAN().setContentTemplate(plAtt.getValue());
    		}else if( RESTR_SAN_REGEXMATCH.equals(plAtt.getName())) {
    			pv.getRestriction_SAN().setRegExMatch(Boolean.valueOf(plAtt.getValue()));

            }else if( TO_PENDIND_ON_FAILED_RESTRICTIONS.equals(plAtt.getName())) {
                pv.setToPendingOnFailedRestrictions(Boolean.valueOf(plAtt.getValue()));
            }else if( SCEP_SECRET_PC_ID.equals(plAtt.getName())) {

                Optional<ProtectedContent> optPC = protectedContentRepository.findById( Long.parseLong(plAtt.getValue()));
                if(optPC.isPresent()){
                    ProtectedContent pc = optPC.get();
                    String clearContent = protectedContentUtil.unprotectString(pc.getContentBase64());
                    scepConfigItems.setScepSecret(clearContent);
                    scepConfigItems.setScepSecretPCId(pc.getId().toString());
                    LOG.debug("pc id : " +  pc.getId() + ", clearContent: " + clearContent);
                    if( pc.getValidTo() == null){
                        // Initialize to midnight
                        scepConfigItems.setScepSecretValidTo(Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1,ChronoUnit.DAYS));
                    }else {
                        scepConfigItems.setScepSecretValidTo(pc.getValidTo());
                    }
                }else{
                    LOG.debug("no protected content for pc id : " + plAtt.getValue());
                }
    		}

        }

    	pv.setAcmeConfigItems(acmeConfigItems);
    	pv.setScepConfigItems(scepConfigItems);

    	/*
    	 * determine the number of  ARA restrictions
    	 */
		Pattern araPattern = Pattern.compile(RESTR_ARA_PATTERN);
		int nARA = 0;
    	for( PipelineAttribute plAtt: pipeline.getPipelineAttributes()) {
    		if( plAtt.getName().startsWith(RESTR_ARA_PREFIX)) {
    			Matcher m = araPattern.matcher(plAtt.getName());
    		    if (m.find( )) {
    		    	int araIdx = Integer.parseInt(m.group(1));
    		    	if( araIdx +1 > nARA) {
    		    		nARA = araIdx +1;
    		    	}
    		    }
    		}
    	}
    	LOG.debug("#{} ARA itmes found", nARA);
		ARARestriction[] araRestrictions = new ARARestriction[nARA];


    	/*
    	 * find all ARA restrictions
    	 */
    	for( PipelineAttribute plAtt: pipeline.getPipelineAttributes()) {
    		if( plAtt.getName().startsWith(RESTR_ARA_PREFIX)) {
    	    	LOG.debug("ARA itmes : {}", plAtt.getName());
    			Matcher m = araPattern.matcher(plAtt.getName());
    		    if (m.find( )) {
    		    	int araIdx = Integer.parseInt(m.group(1));
        	    	LOG.debug("araIdx: {}", araIdx);
    		    	if( araRestrictions[araIdx] == null) {
    		    		araRestrictions[araIdx] = new ARARestriction();
    		    	}
    		    	ARARestriction araRestriction = araRestrictions[araIdx];
    		    	String namePart = m.group(2);
        	    	LOG.debug("ARA namePart : {}", namePart);
    		    	if( RESTR_ARA_NAME.equals(namePart)) {
    		    		araRestriction.setName(plAtt.getValue());
    		    	}else if( RESTR_ARA_REQUIRED.equals(namePart)) {
    		    		araRestriction.setRequired(Boolean.valueOf(plAtt.getValue()));
    	    		}else if( RESTR_ARA_TEMPLATE.equals(namePart)) {
    	    			araRestriction.setContentTemplate(plAtt.getValue());
    	    		}else if( RESTR_ARA_REGEXMATCH.equals(namePart)) {
    	    			araRestriction.setRegExMatch(Boolean.valueOf(plAtt.getValue()));
    	    		}
    		    }
		    }
		}

		pv.setAraRestrictions(araRestrictions);

    	return pv;
    }


	/**
	 *
	 * @param pv
	 * @return
	 */
	public Pipeline toPipeline(PipelineView pv) {

        Pipeline p;
        if( pv.getId() != null) {
	       	Optional<Pipeline> optP = pipelineRepository.findById(pv.getId());
	        if(optP.isPresent()) {
	        	p = optP.get();
	        	pipelineAttRepository.deleteAll(p.getPipelineAttributes());
	        }else {
	        	p = new Pipeline();
                auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_CREATED, p);
	        }
        }else {
        	p = new Pipeline();
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_CREATED, p);
        }

		p.setId(pv.getId());
        if(!Objects.equals(pv.getName(), p.getName())) {
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_NAME_CHANGED, p.getName(), pv.getName(), p);
            p.setName(pv.getName());
        }
        if(!Objects.equals(pv.getDescription(), p.getDescription())) {
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_DESCRIPTION_CHANGED, p.getDescription(), pv.getDescription(), p);
            p.setDescription(pv.getDescription());
        }
        if(!pv.getType().equals(p.getType())) {
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_TYPE_CHANGED, p.getType().toString(), pv.getType().toString(), p);
            p.setType(pv.getType());
        }
        if(!Objects.equals(pv.getUrlPart(), p.getUrlPart())) {
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_URLPART_CHANGED, p.getUrlPart(), pv.getUrlPart(), p);
            p.setUrlPart(pv.getUrlPart());
        }
        if(pv.getApprovalRequired() != p.isApprovalRequired()){
            auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_APPROVAL_REQUIRED_CHANGED, p.isApprovalRequired().toString(), pv.getApprovalRequired().toString(), p);
            p.setApprovalRequired(pv.getApprovalRequired());
        }

        pipelineRepository.save(p);

        String oldCaConnectorName = "";
        if( p.getCaConnector() != null){
            oldCaConnectorName = p.getCaConnector().getName();
        }

        List<CAConnectorConfig> ccc = caConnRepository.findByName(pv.getCaConnectorName());
		if( ccc.isEmpty()) {
			p.setCaConnector(null);
            auditService.createAuditTracePipelineAttribute( "CA_CONNECTOR", oldCaConnectorName, "", p);

        }else {
			p.setCaConnector(ccc.get(0));
            auditService.createAuditTracePipelineAttribute( "CA_CONNECTOR", oldCaConnectorName, ccc.get(0).getName(), p);
		}


        String oldProcessName = "";
        if( p.getProcessInfo() != null){
            oldProcessName = p.getProcessInfo().getName();
        }

		Optional<BPMNProcessInfo> bpiOpt = bpmnPIRepository.findByName(pv.getProcessInfoName());
		if( bpiOpt.isPresent()) {
			p.setProcessInfo(bpiOpt.get());
            auditService.createAuditTracePipelineAttribute( "ISSUANCE_PROCESS", oldProcessName, bpiOpt.get().getName(), p);
		}else {
			p.setProcessInfo(null);
            auditService.createAuditTracePipelineAttribute( "ISSUANCE_PROCESS", oldProcessName, "", p);
		}

        Set<PipelineAttribute> pipelineOldAttributes = new HashSet<PipelineAttribute>(p.getPipelineAttributes());

        Set<PipelineAttribute> pipelineAttributes = new HashSet<PipelineAttribute>();

		addPipelineAttribute(pipelineAttributes, p, RESTR_C_CARDINALITY, pv.getRestriction_C().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_C_TEMPLATE, pv.getRestriction_C().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_C_REGEXMATCH, pv.getRestriction_C().isRegExMatch());
		addPipelineAttribute(pipelineAttributes, p, RESTR_CN_CARDINALITY, pv.getRestriction_CN().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_CN_TEMPLATE, pv.getRestriction_CN().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_CN_REGEXMATCH, pv.getRestriction_CN().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, RESTR_O_CARDINALITY, pv.getRestriction_O().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_O_TEMPLATE, pv.getRestriction_O().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_O_REGEXMATCH, pv.getRestriction_O().isRegExMatch());
		addPipelineAttribute(pipelineAttributes, p, RESTR_OU_CARDINALITY, pv.getRestriction_OU().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_OU_TEMPLATE, pv.getRestriction_OU().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_OU_REGEXMATCH, pv.getRestriction_OU().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, RESTR_L_CARDINALITY, pv.getRestriction_L().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_L_TEMPLATE, pv.getRestriction_L().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_L_REGEXMATCH, pv.getRestriction_L().isRegExMatch());
		addPipelineAttribute(pipelineAttributes, p, RESTR_S_CARDINALITY, pv.getRestriction_S().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_S_TEMPLATE, pv.getRestriction_S().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_S_REGEXMATCH, pv.getRestriction_S().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, RESTR_SAN_CARDINALITY, pv.getRestriction_SAN().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, RESTR_SAN_TEMPLATE, pv.getRestriction_SAN().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, RESTR_SAN_REGEXMATCH, pv.getRestriction_SAN().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, ALLOW_IP_AS_SUBJECT,pv.isIpAsSubjectAllowed());
		addPipelineAttribute(pipelineAttributes, p, ALLOW_IP_AS_SAN,pv.isIpAsSANAllowed());
		addPipelineAttribute(pipelineAttributes, p, TO_PENDIND_ON_FAILED_RESTRICTIONS,pv.isToPendingOnFailedRestrictions());


		if( pv.getAcmeConfigItems() == null) {
			ACMEConfigItems acmeConfigItems = new ACMEConfigItems();
			pv.setAcmeConfigItems(acmeConfigItems );
		}
		addPipelineAttribute(pipelineAttributes, p, ACME_ALLOW_CHALLENGE_HTTP01,pv.getAcmeConfigItems().isAllowChallengeHTTP01());
		addPipelineAttribute(pipelineAttributes, p, ACME_ALLOW_CHALLENGE_DNS,pv.getAcmeConfigItems().isAllowChallengeDNS());
		addPipelineAttribute(pipelineAttributes, p, ACME_ALLOW_CHALLENGE_WILDCARDS,pv.getAcmeConfigItems().isAllowWildcards());
		addPipelineAttribute(pipelineAttributes, p, ACME_CHECK_CAA,pv.getAcmeConfigItems().isCheckCAA());
		addPipelineAttribute(pipelineAttributes, p, ACME_NAME_CAA,pv.getAcmeConfigItems().getCaNameCAA());

        if( pv.getScepConfigItems() == null) {
            SCEPConfigItems scepConfigItems = new SCEPConfigItems();
            pv.setScepConfigItems(scepConfigItems );
        }
        addPipelineAttribute(pipelineAttributes, p, SCEP_CAPABILITY_RENEWAL,pv.getScepConfigItems().isCapabilityRenewal());
        addPipelineAttribute(pipelineAttributes, p, SCEP_CAPABILITY_POST,pv.getScepConfigItems().isCapabilityPostPKIOperation());

        ProtectedContent pc;
        List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW,p.getId());
        if(listPC.isEmpty()) {
            pc = protectedContentUtil.createProtectedContent("", ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW, p.getId());
            LOG.debug("Protected Content created for SCEP password");
        }else{
            pc = listPC.get(0);
            LOG.debug("Protected Content found for SCEP password");
        }

        String oldContent = protectedContentUtil.unprotectString(pc.getContentBase64());
        Instant validTo = pv.getScepConfigItems().getScepSecretValidTo();

        if(!oldContent.equals(pv.getScepConfigItems().getScepSecret()) || !pc.getValidTo().equals(validTo)){
            pc.setContentBase64( protectedContentUtil.protectString(pv.getScepConfigItems().getScepSecret()));
            pc.setValidTo(validTo);
            pc.setDeleteAfter(validTo.plus(1, ChronoUnit.DAYS));
            protectedContentRepository.save(pc);
            LOG.debug("SCEP password updated");
            auditService.createAuditTracePipelineAttribute( "SCEP_SECRET", "#######", "******", p);
        }

        addPipelineAttribute(pipelineAttributes, p, SCEP_SECRET_PC_ID,pc.getId().toString());

        p.setPipelineAttributes(pipelineAttributes);

/*
		for(PipelineAttribute pa: p.getPipelineAttributes()) {
			LOG.debug("PipelineAttribute : " +  pa);
		}

*/
		ARARestriction[] araRestrictions = pv.getAraRestrictions();
		if( araRestrictions != null) {
			int j = 0;
			for( int i = 0; i < araRestrictions.length; i++) {
				ARARestriction araRestriction = araRestrictions[i];
				String araName = araRestriction.getName();
				if(araName != null && !araName.trim().isEmpty()) {
					addPipelineAttribute(pipelineAttributes, p, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_NAME,araName.trim());
					addPipelineAttribute(pipelineAttributes, p, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REQUIRED,araRestriction.isRequired());
					addPipelineAttribute(pipelineAttributes, p, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_TEMPLATE,araRestriction.getContentTemplate());
					addPipelineAttribute(pipelineAttributes, p, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REGEXMATCH,araRestriction.isRegExMatch());
					j++;
				}
			}
		}


        auditTraceForAttributes(p, pipelineOldAttributes);

        pipelineAttRepository.saveAll(p.getPipelineAttributes());
		pipelineRepository.save(p);

		return p;
	}

    private void auditTraceForAttributes(Pipeline p, Set<PipelineAttribute> pipelineOldAttributes) {
        for( PipelineAttribute pOld: pipelineOldAttributes) {

            boolean bFound = false;
            for (PipelineAttribute pNew : p.getPipelineAttributes()) {
                if( pNew.getName().equals(pOld.getName())){
                    if(!Objects.equals(pNew.getValue(), pOld.getValue())){
                        auditService.createAuditTracePipelineAttribute( pOld.getName(), pOld.getValue(), pNew.getValue(), p);
                    }
                    bFound = true;
                    break;
                }
            }
            if(!bFound){
                auditService.createAuditTracePipelineAttribute( pOld.getName(), pOld.getValue(), "", p);
            }
        }

        for (PipelineAttribute pNew : p.getPipelineAttributes()) {
            boolean bFound = false;
            for( PipelineAttribute pOld: pipelineOldAttributes) {
                if( pNew.getName().equals(pOld.getName())){
                    bFound = true;
                    break;
                }
            }
            if(!bFound){
                auditService.createAuditTracePipelineAttribute( pNew.getName(), "", pNew.getValue(), p);
            }
        }
    }


    public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, String name, Boolean value) {
		addPipelineAttribute(pipelineAttributes, p, name, value.toString());

	}

	public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, String name, String value) {


		if( name == null || name.trim().isEmpty()) {
			new Exception("name == null");
			return;
		}

		if( value == null || value.trim().isEmpty()) {
			return;
		}

		PipelineAttribute pAtt = new PipelineAttribute();
		pAtt.setPipeline(p);
		pAtt.setName(name);
		pAtt.setValue(value);
		pipelineAttributes.add(pAtt);

	}

	public boolean isPipelineRestrictionsResolved(Pipeline p, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

		return isPipelineRestrictionsResolved(from(p), p10ReqHolder, messageList);
	}

	public boolean isPipelineRestrictionsResolved(PipelineView pv, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

		boolean outcome = true;

	    RDN[] rdnArr = p10ReqHolder.getSubjectRDNs();

	    if( !checkRestrictions(BCStyle.C, pv.getRestriction_C(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.CN, pv.getRestriction_CN(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.O, pv.getRestriction_O(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.OU, pv.getRestriction_OU(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.L, pv.getRestriction_L(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.ST, pv.getRestriction_S(), rdnArr, messageList)) { outcome = false;}

    	Set<GeneralName> gNameSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());
    	LOG.debug("#" + gNameSet.size() + " SANs present");


	    if( !checkRestrictions(pv.getRestriction_SAN(), gNameSet, messageList)) { outcome = false;}

	    if(!pv.isIpAsSubjectAllowed()) {
	    	if( isSubjectIP(rdnArr, messageList)) {
	    		outcome = false;
	    	}
	    }

	    if(!pv.isIpAsSANAllowed()) {
	    	if( hasIPinSANList(gNameSet, messageList)) {
	    		outcome = false;
	    	}
	    }

		return outcome;
	}


	private boolean hasIPinSANList(Set<GeneralName> gNameSet, List<String> messageList) {

		boolean outcome = false;

    	for( GeneralName gn: gNameSet) {
			if (GeneralName.iPAddress == gn.getTagNo()) {
				String sanValue = gn.getName().toString();
				messageList.add("SAN '"+sanValue+"' is an IP address");
				outcome = true;
			}
    	}
		return outcome;
	}


	private boolean checkRestrictions(RDNRestriction restriction, Set<GeneralName> gNameSet, List<String> messageList) {

		if( restriction == null) {
			return true; // no restrictions present!!
		}

		boolean outcome = true;

		String template = "";
		LOG.debug("checking SANs");

		boolean hasTemplate = false;
		if( restriction.getContentTemplate() != null) {
			template = restriction.getContentTemplate().trim();
			hasTemplate = !template.isEmpty();
		}
		int n = 0;

    	for( GeneralName gn: gNameSet) {
			n++;
			if( hasTemplate) {
				String value = gn.getName().toString().trim();
				if( restriction.isRegExMatch()) {
					boolean evalResult = false;
					try {
						evalResult = value.matches(template);
					}catch(PatternSyntaxException pse) {
						LOG.warn("pattern '"+template+"' is not valid");
					}
					if( !evalResult ) {
						String msg = "restriction mismatch: SAN '"+value +"' does not match regular expression '"+template+"' !";
						messageList.add(msg);
						LOG.debug(msg);
						outcome = false;
					}
				}else{
					if( !template.equalsIgnoreCase(value) ) {
						String msg = "restriction mismatch: SAN '"+value +"' does not match expected value '"+template+"' !";
						messageList.add(msg);
						LOG.debug(msg);
						outcome = false;
					}
				}
			}

		}

		RDNCardinalityRestriction cardinality = restriction.getCardinalityRestriction();
		if( RDNCardinalityRestriction.NOT_ALLOWED.equals(cardinality)) {
			if( n > 0) {
				String msg = "restriction mismatch: A SAN MUST NOT occur!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE.equals(cardinality)) {
			if( n ==  0) {
				String msg = "restriction mismatch: SAN MUST occur once, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
			if( n != 1) {
				String msg = "restriction mismatch: SAN MUST occur exactly once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
			if( n == 0) {
				String msg = "restriction mismatch: SAns MUST occur once or more, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
			if( n > 1) {
				String msg = "restriction mismatch: SANs MUST occur zero or once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		}
		return outcome;
	}


	private boolean checkRestrictions(ASN1ObjectIdentifier restricted, RDNRestriction restriction, RDN[] rdnArr, List<String> messageList) {

		if( restriction == null) {
			return true; // no restrictions present!!
		}

		boolean outcome = true;

		String template = "";
		String restrictedName = OidNameMapper.lookupOid(restricted.toString());
		LOG.debug("checking element '{}'", restrictedName);

		boolean hasTemplate = false;
		if( restriction.getContentTemplate() != null) {
			template = restriction.getContentTemplate().trim();
			hasTemplate = !template.isEmpty();
		}
		int n = 0;

		for( RDN rdn: rdnArr) {
			AttributeTypeAndValue atv = rdn.getFirst();
			if( restricted.equals(atv.getType())){
				n++;
				if( hasTemplate) {
					String value = atv.getValue().toString().trim();
					if( restriction.isRegExMatch()) {
						boolean evalResult = false;
						try {
							evalResult = value.matches(template);
						}catch(PatternSyntaxException pse) {
							LOG.warn("pattern '"+template+"' is not valid");
						}
						if( !evalResult ) {
							String msg = "restriction mismatch: '"+value +"' does not match regular expression '"+template+"' !";
							messageList.add(msg);
							LOG.debug(msg);
							outcome = false;
						}
					}else{
						if( !template.equalsIgnoreCase(value) ) {
							String msg = "restriction mismatch: '"+value +"' does not match expected value '"+template+"' !";
							messageList.add(msg);
							LOG.debug(msg);
							outcome = false;
						}
					}
				}

			}

		}

		RDNCardinalityRestriction cardinality = restriction.getCardinalityRestriction();
		if( RDNCardinalityRestriction.NOT_ALLOWED.equals(cardinality)) {
			if( n > 0) {
				String msg = "restrcition mismatch: '"+restrictedName+"' MUST NOT occur!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE.equals(cardinality)) {
			if( n ==  0) {
				String msg = "restrcition mismatch: '"+restrictedName+"' MUST occur once, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
			if( n != 1) {
				String msg = "restrcition mismatch: '"+restrictedName+"' MUST occur exactly once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
			if( n == 0) {
				String msg = "restrcition mismatch: '"+restrictedName+"' MUST occur once or more, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
			if( n > 1) {
				String msg = "restrcition mismatch: '"+restrictedName+"' MUST occur zero or once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		}
		return outcome;
	}

	private boolean isSubjectIP(RDN[] rdnArr, List<String> messageList) {


		for( RDN rdn: rdnArr) {
			AttributeTypeAndValue atv = rdn.getFirst();
			if( BCStyle.CN.equals(atv.getType())){

				String value = atv.getValue().toString().trim();
				InetAddressValidator inv = InetAddressValidator.getInstance();
				if( inv.isValidInet4Address(value)) {
					messageList.add("CommonName '"+value+"' is valid IP4 address");
					return true;
				}
				if( inv.isValidInet6Address(value)) {
					messageList.add("CommonName '"+value+"' is valid IP6 address");
					return true;
				}
			}

		}
		return false;
	}

}
