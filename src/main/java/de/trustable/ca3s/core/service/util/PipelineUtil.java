package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.config.Constants;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.*;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.*;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


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
    public static final String RESTR_E_CARDINALITY = "RESTR_E_CARDINALITY";
    public static final String RESTR_E_TEMPLATE = "RESTR_E_TEMPLATE";
    public static final String RESTR_E_REGEXMATCH = "RESTR_E_REGEXMATCH";

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

    public static final String CSR_USAGE = "CSR_USAGE";

    public static final String LIST_ORDER = "LIST_ORDER";

    public static final String ACME_PROCESS_ACCOUNT_VALIDATION = "ACME_PROCESS_ACCOUNT_VALIDATION";
	public static final String ACME_PROCESS_ORDER_VALIDATION = "ACME_PROCESS_ORDER_VALIDATION";
	public static final String ACME_PROCESS_CHALLENGE_VALIDATION = "ACME_PROCESS_CHALLENGE_VALIDATION";

    public static final String SCEP_CAPABILITY_RENEWAL = "SCEP_CAPABILITY_RENEWAL";
    public static final String SCEP_CAPABILITY_POST = "SCEP_CAPABILITY_POST";
    public static final String SCEP_SECRET = "SCEP_SECRET";
    public static final String SCEP_SECRET_VALID_TO = "SCEP_SECRET_VALID_TO";
    public static final String SCEP_SECRET_PC_ID = "SCEP_SECRET_PC_ID";

    public static final String SCEP_RECIPIENT_DN = "SCEP_RECIPIENT_DN";
    public static final String SCEP_RECIPIENT_KEY_TYPE_LEN = "SCEP_RECIPIENT_KEY_TYPE_LEN";
    public static final String SCEP_CA_CONNECTOR_RECIPIENT_NAME = "SCEP_CA_CONNECTOR_RECIPIENT_NAME";


    Logger LOG = LoggerFactory.getLogger(PipelineUtil.class);

    @Autowired
    private CertificateRepository certRepository;

    @Autowired
    private CSRRepository csrRepository;

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
    private CertificateUtil certUtil;

    @Autowired
    private CertificateProcessingUtil cpUtil;

    @Autowired
    private AuditService auditService;

    @Autowired
    private AuditTraceRepository auditTraceRepository;

    public PipelineView from(Pipeline pipeline) {

    	PipelineView pv = new PipelineView();

    	pv.setId(pipeline.getId());
    	pv.setName(pipeline.getName());
    	pv.setType(pipeline.getType());
    	pv.setActive(pipeline.isActive());
    	pv.setDescription(pipeline.getDescription());
    	pv.setApprovalRequired(pipeline.isApprovalRequired());
    	pv.setUrlPart(pipeline.getUrlPart());

    	if( pipeline.getCaConnector()!= null) {
    		pv.setCaConnectorName(pipeline.getCaConnector().getName());
    	}

    	if( pipeline.getProcessInfo() != null) {
    		pv.setProcessInfoName(pipeline.getProcessInfo().getName());
    	}

        RDNRestriction[] rdnRestrictArr = initRdnRestrictions(pv, pipeline);
        pv.setRdnRestrictions(rdnRestrictArr);

		pv.setAraRestrictions(new ARARestriction[0]);

    	ACMEConfigItems acmeConfigItems = new ACMEConfigItems();
        SCEPConfigItems scepConfigItems = new SCEPConfigItems();

        pv.setCsrUsage(CsrUsage.TLS_SERVER);

//    	acmeConfigItems.setProcessInfoNameAccountValidation(processInfoNameAccountValidation);

    	for( PipelineAttribute plAtt: pipeline.getPipelineAttributes()) {

    		if( ACME_ALLOW_CHALLENGE_HTTP01.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowChallengeHTTP01(Boolean.parseBoolean(plAtt.getValue()));
    		}else if( ACME_ALLOW_CHALLENGE_DNS.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowChallengeDNS(Boolean.parseBoolean(plAtt.getValue()));
    		}else if( ACME_ALLOW_CHALLENGE_WILDCARDS.equals(plAtt.getName())) {
    			acmeConfigItems.setAllowWildcards(Boolean.parseBoolean(plAtt.getValue()));
    		}else if( ACME_CHECK_CAA.equals(plAtt.getName())) {
    			acmeConfigItems.setCheckCAA(Boolean.parseBoolean(plAtt.getValue()));
    		}else if( ACME_NAME_CAA.equals(plAtt.getName())) {
    			acmeConfigItems.setCaNameCAA(plAtt.getValue());

            }else if( SCEP_RECIPIENT_DN.equals(plAtt.getName())) {
                scepConfigItems.setScepRecipientDN(plAtt.getValue());
            }else if( SCEP_RECIPIENT_KEY_TYPE_LEN.equals(plAtt.getName())) {
                KeyAlgoLength keyAlgoLength = KeyAlgoLength.valueOf(plAtt.getValue());
                scepConfigItems.setKeyAlgoLength(keyAlgoLength);
            }else if( SCEP_CA_CONNECTOR_RECIPIENT_NAME.equals(plAtt.getName())) {
                scepConfigItems.setCaConnectorRecipientName(plAtt.getValue());
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
                    scepConfigItems.setScepSecret("-- expired --");
                    LOG.debug("no protected content for pc id : " + plAtt.getValue());
                }
    		}

        }

        if( PipelineType.SCEP.equals(pipeline.getType())){
            try {
                Certificate currentRecipientCert = getSCEPRecipientCertificate(pipeline);
                if( currentRecipientCert != null) {
                    LOG.debug("pipeline id {} identifies recipient certificate with id {} ",
                        pipeline.getId(), currentRecipientCert.getId());
                    scepConfigItems.setRecepientCertId(currentRecipientCert.getId());
                    scepConfigItems.setRecepientCertSerial(currentRecipientCert.getSerial());
                    scepConfigItems.setRecepientCertSubject(currentRecipientCert.getSubject());
                }
            } catch (IOException | GeneralSecurityException e) {
                LOG.warn("problem retrieving recipient certificate", e);
            }
        }

        pv.setAcmeConfigItems(acmeConfigItems);
    	pv.setScepConfigItems(scepConfigItems);

        ARARestriction[] araRestrictions = initAraRestrictions(pipeline);

        pv.setAraRestrictions(araRestrictions);

    	return pv;
    }

    @NotNull
    private RDNRestriction[] initRdnRestrictions(PipelineView pv, Pipeline pipeline) {

        RDNRestriction[] rdnRestrictArr = new RDNRestriction[8];

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
        rdnRestrict.setRdnName("E");
        rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
        pv.setRestriction_E(rdnRestrict);
        rdnRestrictArr[6] = rdnRestrict;

        rdnRestrict = new RDNRestriction();
        rdnRestrict.setRdnName("SAN");
        rdnRestrict.setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
        pv.setRestriction_SAN(rdnRestrict);
        rdnRestrictArr[7] = rdnRestrict;

        for( PipelineAttribute plAtt: pipeline.getPipelineAttributes()) {

            if( ALLOW_IP_AS_SUBJECT.equals(plAtt.getName())) {
                pv.setIpAsSubjectAllowed(Boolean.parseBoolean(plAtt.getValue()));

            }else if( ALLOW_IP_AS_SAN.equals(plAtt.getName())) {
                pv.setIpAsSANAllowed(Boolean.parseBoolean(plAtt.getValue()));

            }else if( RESTR_C_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_C_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_C().setContentTemplate(plAtt.getValue());
            }else if( RESTR_C_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_C().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            }else if( RESTR_CN_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_CN_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_CN().setContentTemplate(plAtt.getValue());
            }else if( RESTR_CN_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_CN().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));

            }else if( RESTR_O_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_O_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_O().setContentTemplate(plAtt.getValue());
            }else if( RESTR_O_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_O().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            }else if( RESTR_OU_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_OU_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_OU().setContentTemplate(plAtt.getValue());
            }else if( RESTR_OU_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_OU().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));

            }else if( RESTR_L_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_L_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_L().setContentTemplate(plAtt.getValue());
            }else if( RESTR_L_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_L().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            }else if( RESTR_S_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_S_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_S().setContentTemplate(plAtt.getValue());
            }else if( RESTR_S_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_S().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            }else if( RESTR_E_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_E_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_E().setContentTemplate(plAtt.getValue());
            }else if( RESTR_E_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_E().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));

            }else if( RESTR_SAN_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            }else if( RESTR_SAN_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setContentTemplate(plAtt.getValue());
            }else if( RESTR_SAN_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));

            }else if( CSR_USAGE.equals(plAtt.getName())) {
                pv.setCsrUsage(CsrUsage.valueOf(plAtt.getValue()));

            }else if( LIST_ORDER.equals(plAtt.getName())) {
                pv.setListOrder(Integer.parseInt(plAtt.getValue()));

            }else if( TO_PENDIND_ON_FAILED_RESTRICTIONS.equals(plAtt.getName())) {
                pv.setToPendingOnFailedRestrictions(Boolean.parseBoolean(plAtt.getValue()));

            }
        }

        return rdnRestrictArr;
    }

    @NotNull
    public ARARestriction[] initAraRestrictions(Pipeline pipeline) {
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
                        araRestriction.setRequired(Boolean.parseBoolean(plAtt.getValue()));
                    }else if( RESTR_ARA_TEMPLATE.equals(namePart)) {
                        araRestriction.setContentTemplate(plAtt.getValue());
                    }else if( RESTR_ARA_REGEXMATCH.equals(namePart)) {
                        araRestriction.setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
                    }
                }
            }
        }
        return araRestrictions;
    }


    /**
	 *
	 * @param pv
	 * @return
	 */
	public Pipeline toPipeline(PipelineView pv) {

	    List<AuditTrace> auditList = new ArrayList<>();
        Pipeline p;
        if( pv.getId() != null) {
	       	Optional<Pipeline> optP = pipelineRepository.findById(pv.getId());
	        if(optP.isPresent()) {
	        	p = optP.get();
	        	pipelineAttRepository.deleteAll(p.getPipelineAttributes());
	        }else {
	        	p = new Pipeline();
                pipelineRepository.save(p);
                auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_CREATED, p));
	        }
        }else {
        	p = new Pipeline();
            p.setName(pv.getName());
            p.setType(pv.getType());
            pipelineRepository.save(p);
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_COPIED, p));
        }

//        p.setId(pv.getId());
//        pipelineRepository.save(p);

        if(!Objects.equals(pv.getName(), p.getName())) {
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_NAME_CHANGED, p.getName(), pv.getName(), p));
            p.setName(pv.getName());
        }
        if(!Objects.equals(pv.getDescription(), p.getDescription())) {
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_DESCRIPTION_CHANGED, p.getDescription(), pv.getDescription(), p));
            p.setDescription(pv.getDescription());
        }
        if(!pv.getType().equals(p.getType())) {
            String oldType = "";
            if( p.getType() != null){
                oldType = p.getType().toString();
            }
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_TYPE_CHANGED, oldType, pv.getType().toString(), p));
            p.setType(pv.getType());
        }
        if(!Objects.equals(pv.getUrlPart(), p.getUrlPart())) {
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_URLPART_CHANGED, p.getUrlPart(), pv.getUrlPart(), p));
            p.setUrlPart(pv.getUrlPart());
        }

        if(pv.getApprovalRequired() != p.isApprovalRequired()){
            String isApprovalRequired = "";
            if( p.isApprovalRequired() != null){
                isApprovalRequired = p.isApprovalRequired().toString();
            }
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_APPROVAL_REQUIRED_CHANGED, isApprovalRequired, pv.getApprovalRequired().toString(), p));
            p.setApprovalRequired(pv.getApprovalRequired());
        }

        if(pv.getActive()!= p.isActive()){
            String isActive = "";
            if( p.isActive() != null){
                isActive = p.isActive().toString();
            }
            auditList.add(auditService.createAuditTracePipeline( AuditService.AUDIT_PIPELINE_ACTIVE_CHANGED, isActive, pv.getActive().toString(), p));
            p.setActive(pv.getActive());
        }

        String oldCaConnectorName = "";
        if( p.getCaConnector() != null){
            oldCaConnectorName = p.getCaConnector().getName();
        }

        List<CAConnectorConfig> ccc = caConnRepository.findByName(pv.getCaConnectorName());
		if( ccc.isEmpty()) {
			p.setCaConnector(null);
            auditList.add(auditService.createAuditTracePipelineAttribute( "CA_CONNECTOR", oldCaConnectorName, "", p));

        }else {
			p.setCaConnector(ccc.get(0));
			if( !ccc.get(0).getName().equals(oldCaConnectorName) ) {
                auditList.add(auditService.createAuditTracePipelineAttribute("CA_CONNECTOR", oldCaConnectorName, ccc.get(0).getName(), p));
            }
		}

        String oldProcessName = "";
        if( p.getProcessInfo() != null){
            oldProcessName = p.getProcessInfo().getName();
        }

		Optional<BPMNProcessInfo> bpiOpt = bpmnPIRepository.findByName(pv.getProcessInfoName());
		if( bpiOpt.isPresent()) {
            BPMNProcessInfo bpi = bpiOpt.get();
			p.setProcessInfo(bpi);
            if( !bpi.getName().equals(oldProcessName) ) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ISSUANCE_PROCESS", oldProcessName, bpi.getName(), p));
            }
		}else {
			p.setProcessInfo(null);
			if( !oldProcessName.equals("")) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ISSUANCE_PROCESS", oldProcessName, "", p));
            }
		}

        Set<PipelineAttribute> pipelineOldAttributes = new HashSet<>(p.getPipelineAttributes());
        LOG.debug("PipelineAttributes : cloned old #{}, new {}", pipelineOldAttributes.size(), p.getPipelineAttributes().size());

        Set<PipelineAttribute> pipelineAttributes = new HashSet<>();

		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_CARDINALITY, pv.getRestriction_C().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_TEMPLATE, pv.getRestriction_C().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_REGEXMATCH, pv.getRestriction_C().isRegExMatch());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_CARDINALITY, pv.getRestriction_CN().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_TEMPLATE, pv.getRestriction_CN().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_REGEXMATCH, pv.getRestriction_CN().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_CARDINALITY, pv.getRestriction_O().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_TEMPLATE, pv.getRestriction_O().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_REGEXMATCH, pv.getRestriction_O().isRegExMatch());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_CARDINALITY, pv.getRestriction_OU().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_TEMPLATE, pv.getRestriction_OU().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_REGEXMATCH, pv.getRestriction_OU().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_CARDINALITY, pv.getRestriction_L().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_TEMPLATE, pv.getRestriction_L().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_REGEXMATCH, pv.getRestriction_L().isRegExMatch());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_CARDINALITY, pv.getRestriction_S().getCardinalityRestriction().name());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_TEMPLATE, pv.getRestriction_S().getContentTemplate());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_REGEXMATCH, pv.getRestriction_S().isRegExMatch());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_CARDINALITY, pv.getRestriction_E().getCardinalityRestriction().name());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_TEMPLATE, pv.getRestriction_E().getContentTemplate());
        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_REGEXMATCH, pv.getRestriction_E().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_CARDINALITY, pv.getRestriction_SAN().getCardinalityRestriction().name());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_TEMPLATE, pv.getRestriction_SAN().getContentTemplate());
		addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_REGEXMATCH, pv.getRestriction_SAN().isRegExMatch());

		addPipelineAttribute(pipelineAttributes, p, auditList, ALLOW_IP_AS_SUBJECT,pv.isIpAsSubjectAllowed());
		addPipelineAttribute(pipelineAttributes, p, auditList, ALLOW_IP_AS_SAN,pv.isIpAsSANAllowed());
		addPipelineAttribute(pipelineAttributes, p, auditList, TO_PENDIND_ON_FAILED_RESTRICTIONS,pv.isToPendingOnFailedRestrictions());


		if( pv.getAcmeConfigItems() == null) {
			ACMEConfigItems acmeConfigItems = new ACMEConfigItems();
			pv.setAcmeConfigItems(acmeConfigItems );
		}

        if( PipelineType.ACME.equals(pv.getType())) {
            // ensure that at least HTTP-01 challenge is available
            if (!pv.getAcmeConfigItems().isAllowChallengeDNS()) {
                pv.getAcmeConfigItems().setAllowChallengeHTTP01(true);
                pv.getAcmeConfigItems().setAllowWildcards(false);
            }
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_HTTP01, pv.getAcmeConfigItems().isAllowChallengeHTTP01());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_DNS, pv.getAcmeConfigItems().isAllowChallengeDNS());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_WILDCARDS, pv.getAcmeConfigItems().isAllowWildcards());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_CHECK_CAA, pv.getAcmeConfigItems().isCheckCAA());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_NAME_CAA, pv.getAcmeConfigItems().getCaNameCAA());
        }

        addPipelineAttribute(pipelineAttributes, p, auditList, CSR_USAGE,pv.getCsrUsage().toString());
        if( PipelineType.WEB.equals(pv.getType())) {
            addPipelineAttribute(pipelineAttributes, p, auditList, LIST_ORDER, "" + pv.getListOrder());
        }

        if( pv.getScepConfigItems() == null) {
            SCEPConfigItems scepConfigItems = new SCEPConfigItems();
            pv.setScepConfigItems(scepConfigItems );
        }

        if( PipelineType.SCEP.equals(pv.getType())) {
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CAPABILITY_RENEWAL, pv.getScepConfigItems().isCapabilityRenewal());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CAPABILITY_POST, pv.getScepConfigItems().isCapabilityPostPKIOperation());

            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_RECIPIENT_DN, pv.getScepConfigItems().getScepRecipientDN());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_RECIPIENT_KEY_TYPE_LEN, pv.getScepConfigItems().getKeyAlgoLength().toString());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CA_CONNECTOR_RECIPIENT_NAME, pv.getScepConfigItems().getCaConnectorRecipientName());
        }

        ProtectedContent pc;
        List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW,p.getId());
        if(listPC.isEmpty()) {
//            pc = protectedContentUtil.createProtectedContent("", ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW, p.getId());

            pc = new ProtectedContent();
            pc.setType(ProtectedContentType.PASSWORD);
            pc.setRelationType(ContentRelationType.SCEP_PW);
            pc.setRelatedId(p.getId());
            pc.setLeftUsages(-1);
            pc.setValidTo(ProtectedContentUtil.MAX_INSTANT);
            pc.setDeleteAfter(ProtectedContentUtil.MAX_INSTANT);

            LOG.debug("Protected Content created for SCEP password");
        }else{
            pc = listPC.get(0);
            LOG.debug("Protected Content found for SCEP password");
        }

        String oldContent = protectedContentUtil.unprotectString(pc.getContentBase64());
        Instant validTo = pv.getScepConfigItems().getScepSecretValidTo();

        if(oldContent == null ||
            !oldContent.equals(pv.getScepConfigItems().getScepSecret()) ||
            pc.getValidTo() == null ||
            !pc.getValidTo().equals(validTo)){

            pc.setContentBase64( protectedContentUtil.protectString(pv.getScepConfigItems().getScepSecret()));
            pc.setValidTo(validTo);
            pc.setDeleteAfter(validTo.plus(1, ChronoUnit.DAYS));
            protectedContentRepository.save(pc);
            LOG.debug("SCEP password updated {} -> {}, {} -> {}", oldContent, pv.getScepConfigItems().getScepSecret(), validTo, pc.getValidTo());
            auditList.add(auditService.createAuditTracePipelineAttribute( "SCEP_SECRET", "#######", "******", p));
        }

        addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_SECRET_PC_ID, pc.getId().toString());

        p.setPipelineAttributes(pipelineAttributes);

/*
		for(PipelineAttribute pa: p.getPipelineAttributes()) {
			LOG.debug("PipelineAttribute : " +  pa);
		}

*/
		ARARestriction[] araRestrictions = pv.getAraRestrictions();
		if( araRestrictions != null) {
			int j = 0;
            for (ARARestriction araRestriction : araRestrictions) {
                String araName = araRestriction.getName();
                if (araName != null && !araName.trim().isEmpty()) {
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_NAME, araName.trim());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REQUIRED, araRestriction.isRequired());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_TEMPLATE, araRestriction.getContentTemplate());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REGEXMATCH, araRestriction.isRegExMatch());
                    j++;
                }
            }
		}


        auditTraceForAttributes(p, auditList, pipelineOldAttributes);

        p.setPipelineAttributes(pipelineAttributes);
        pipelineAttRepository.saveAll(p.getPipelineAttributes());
		pipelineRepository.save(p);
        auditTraceRepository.saveAll(auditList);

		return p;
	}

    private void auditTraceForAttributes(Pipeline p, List<AuditTrace> auditList, Set<PipelineAttribute> pipelineOldAttributes) {
        LOG.debug("matching PipelineAttributes : old #{}, new {}", pipelineOldAttributes.size(), p.getPipelineAttributes().size());

        for( PipelineAttribute pOld: pipelineOldAttributes) {

            boolean bFound = false;
            for (PipelineAttribute pNew : p.getPipelineAttributes()) {
                if( pNew.getName().equals(pOld.getName())){
                    if(!Objects.equals(pNew.getValue(), pOld.getValue())){
                        auditList.add(auditService.createAuditTracePipelineAttribute( pOld.getName(), pOld.getValue(), pNew.getValue(), p));
                    }
                    bFound = true;
                    break;
                }
            }
            if(!bFound){
                auditList.add(auditService.createAuditTracePipelineAttribute( pOld.getName(), pOld.getValue(), "", p));
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
                auditList.add(auditService.createAuditTracePipelineAttribute( pNew.getName(), "", pNew.getValue(), p));
                LOG.debug("matching PipelineAttributes : new name {} not found in old list", pNew.getName());
            }
        }
    }

    public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, List<AuditTrace> auditList, String name, Boolean value) {
		addPipelineAttribute(pipelineAttributes, p, auditList, name, value.toString());
	}

	public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, List<AuditTrace> auditList, String name, String value) {

		if( name == null || name.trim().isEmpty()) {
			new Exception("name == null").printStackTrace();
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

        auditList.add(auditService.createAuditTracePipelineAttribute( name, "", value, p));
        LOG.debug("matching PipelineAttributes : new attribute with name '{}' and value  '{}' added", name, value);

    }

    public boolean isPipelineRestrictionsResolved(Pipeline p, Pkcs10RequestHolder p10ReqHolder, NamedValues[] nvARArr, List<String> messageList) {

	    // null pipeline means internal requests without an associated pipeline and no restrictions
        if(p == null){
            return true;
        }

        if(!isPipelineAdditionalRestrictionsResolved(initAraRestrictions(p), nvARArr, messageList)){
            return false;
        }
        return isPipelineRestrictionsResolved(p, p10ReqHolder, messageList);
    }

    public boolean isPipelineRestrictionsResolved(Pipeline p, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

	    if(p == null){
	        return true;
        }

        PipelineView pv = new PipelineView();
        initRdnRestrictions(pv, p);

        return isPipelineRestrictionsResolved(pv, p10ReqHolder, messageList);
    }

    public boolean isPipelineAdditionalRestrictionsResolved(ARARestriction[] araRestrictions, NamedValues[] nvARArr, List<String> messageList) {

        boolean outcome = true;

        for(NamedValues nvAR: nvARArr){
            for(ARARestriction araRestriction:araRestrictions){
                if( araRestriction.getName().equals(nvAR.getName())){
                    if(!checkAdditionalRestrictions(araRestriction, nvAR, messageList)){
                        outcome = false;
                    }
                }
            }
        }
        for(ARARestriction araRestriction:araRestrictions){
            if(araRestriction.isRequired()){
                if(Arrays.stream(nvARArr).noneMatch(nv -> (araRestriction.getName().equals(nv.getName())))){
                    String msg = "additional restriction mismatch: An value for '" + araRestriction.getName() + "' MUST be present!";
                    messageList.add(msg);
                    LOG.debug(msg);
                    outcome = false;
                }
            }
        }
        return outcome;
    }

    public boolean isPipelineRestrictionsResolved(PipelineView pv, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

		boolean outcome = true;

        Set<GeneralName> gNameSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());
        LOG.debug("#" + gNameSet.size() + " SANs present");


        RDN[] rdnArr = p10ReqHolder.getSubjectRDNs();

	    if( !checkRestrictions(BCStyle.C, pv.getRestriction_C(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.CN, pv.getRestriction_CN(), rdnArr, gNameSet, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.O, pv.getRestriction_O(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.OU, pv.getRestriction_OU(), rdnArr, messageList)) { outcome = false;}
	    if( !checkRestrictions(BCStyle.L, pv.getRestriction_L(), rdnArr, messageList)) { outcome = false;}
        if( !checkRestrictions(BCStyle.ST, pv.getRestriction_S(), rdnArr, messageList)) { outcome = false;}
        if( !checkRestrictions(BCStyle.E, pv.getRestriction_E(), rdnArr, messageList)) { outcome = false;}

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
                String sanValue = CertificateUtil.getTypedSAN(gn);
                messageList.add("SAN '"+sanValue+"' is an IP address, not allowed.");
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
                String value = CertificateUtil.getTypedSAN(gn);

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

    private boolean checkAdditionalRestrictions(ARARestriction araRestriction, NamedValues nvAR, List<String> messageList) {

        if( araRestriction == null) {
            return true; // no restrictions present!!
        }

        boolean outcome = true;

        String template;
        LOG.debug("checking AdditionalRestrictions");

        boolean hasTemplate = false;
        if( araRestriction.getContentTemplate() != null) {
            template = araRestriction.getContentTemplate().trim();
            hasTemplate = !template.isEmpty();
        }

        if( araRestriction.isRequired() ){
            if( nvAR.getValues().length == 0 ){
                String msg = "additional restriction mismatch: An value for '"+nvAR.getName()+"' MUST be present!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }else{
                for( String value: nvAR.getValues()){
                    if( value.isEmpty()) {
                        String msg = "additional restriction mismatch: An value for '" + nvAR.getName() + "' MUST be present!";
                        messageList.add(msg);
                        LOG.debug(msg);
                        outcome = false;
                    }
                    if( hasTemplate ) {
                        if (!checkTemplate(araRestriction, value, messageList)) {
                            outcome = false;
                        }
                    }
                }
            }

        }
        return outcome;
    }


    private boolean checkRestrictions(ASN1ObjectIdentifier restricted, RDNRestriction restriction, RDN[] rdnArr, List<String> messageList) {
        return checkRestrictions(restricted, restriction, rdnArr, new HashSet<>(), messageList);
    }

    private boolean checkRestrictions(ASN1ObjectIdentifier restricted, RDNRestriction restriction, RDN[] rdnArr, Set<GeneralName> gNameSet, List<String> messageList) {


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
				String msg = "restricition mismatch: '"+restrictedName+"' MUST NOT occur!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE.equals(cardinality)) {
			if( n ==  0) {
				String msg = "restricition mismatch: '"+restrictedName+"' MUST occur once, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
			if( n != 1) {
				String msg = "restricition mismatch: '"+restrictedName+"' MUST occur exactly once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
        } else if( RDNCardinalityRestriction.ONE_OR_SAN.equals(cardinality)) {
            if( n == 0 && gNameSet.isEmpty()) {
                String msg = "restricition mismatch: '"+restrictedName+"' MUST occur once or a SAN entry MUST be present!!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
            if( n != 1) {
                String msg = "restricition mismatch: '"+restrictedName+"' MUST occur exactly once, found "+n+" times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
		} else if( RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
			if( n == 0) {
				String msg = "restricition mismatch: '"+restrictedName+"' MUST occur once or more, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
			if( n > 1) {
				String msg = "restricition mismatch: '"+restrictedName+"' MUST occur zero or once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		}
		return outcome;
	}

	private boolean checkTemplate(ARARestriction araRestriction, String value, List<String> messageList){

        boolean outcome = true;
        String template = araRestriction.getContentTemplate().trim();

        if( araRestriction.isRegExMatch()) {
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

    public void setPipelineAttribute(Pipeline pipeline, String name, String value) {

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (name.equals(plAtt.getName())) {
                if( !plAtt.getValue().equals(value) ){
                    plAtt.setValue(value);
                    pipelineAttRepository.save(plAtt);
                }
                return;
            }
        }
        PipelineAttribute pAtt = new PipelineAttribute();
        pAtt.setPipeline(pipeline);
        pAtt.setName(name);
        pAtt.setValue(value);
        pipeline.getPipelineAttributes().add(pAtt);

        pipelineAttRepository.save(pAtt);
        pipelineRepository.save(pipeline);
    }

    public String getPipelineAttribute(Pipeline pipeline, String name, String defaultValue) {

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (name.equals(plAtt.getName())) {
                return plAtt.getValue();
            }
        }
        return defaultValue;
    }

    public Certificate getSCEPRecipientCertificate( Pipeline pipeline) throws IOException, GeneralSecurityException {

        Certificate currentRecepientCert = certUtil.getCurrentSCEPRecipient(pipeline);
        if(currentRecepientCert != null ) {
            LOG.debug("found active certificate as scep recipient with id {}", currentRecepientCert.getId());
            return currentRecepientCert;
        }
        if( pipeline.isActive()) {

            Certificate recipientCert = createSCEPRecipientCertificate(pipeline);
            if (recipientCert == null) {
                LOG.info("creation of scep recipient certificate for pipeline {} failed", pipeline.getId());
            } else {
                LOG.debug("new scep recipient certificate {} created for pipeline {}", recipientCert.getId(), pipeline.getId());
            }
            return recipientCert;
        }else{
            LOG.debug("pipeline {} NOT active, no recipient certificate created",pipeline.getId());
            return currentRecepientCert;
        }
    }

    private Certificate createSCEPRecipientCertificate( Pipeline pipeline) throws IOException, GeneralSecurityException {

        String scepRecipientDN = getPipelineAttribute( pipeline, SCEP_RECIPIENT_DN, "CN=SCEPRecepient_"+ pipeline.getId());
        X500Principal subject = new X500Principal(scepRecipientDN);

        String caConnectorName = getPipelineAttribute( pipeline, SCEP_CA_CONNECTOR_RECIPIENT_NAME, "");
        List<CAConnectorConfig> caConfigList = caConnRepository.findByName(caConnectorName);
        if( caConfigList.isEmpty() ){
            LOG.warn("creation of SCEP recipient certificate failed, connector {} missing !", caConnectorName);
            return null;
        }

        String scepRecipientKeyLength = getPipelineAttribute( pipeline, SCEP_RECIPIENT_KEY_TYPE_LEN, "RSA_2048");
        KeyAlgoLength kal = KeyAlgoLength.valueOf(scepRecipientKeyLength);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(kal.algoName());
        keyPairGenerator.initialize(kal.keyLength());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String p10ReqPem = CryptoUtil.getCsrAsPEM(subject,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            null
        );

        String requestorName = Constants.SYSTEM_ACCOUNT;
        CSR csr = cpUtil.buildCSR(p10ReqPem, requestorName, AuditService.AUDIT_SCEP_CERTIFICATE_REQUESTED, "", null );
        csrRepository.save(csr);


        Certificate cert = cpUtil.processCertificateRequest(csr, requestorName,  AuditService.AUDIT_SCEP_CERTIFICATE_CREATED, caConfigList.get(0) );
        if( cert == null) {
            LOG.warn("creation of SCEP recipient certificate with DN '{}' failed ", scepRecipientDN);
        }else {
            LOG.debug("new certificate id '{}' for SCEP recipient", cert.getId());

            certUtil.storePrivateKey(cert, keyPair);
            certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT, ""+ pipeline.getId());

            certRepository.save(cert);
        }

        return cert;
    }


}

