package de.trustable.ca3s.core.service.util;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

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

import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.domain.enumeration.RDNCardinalityRestriction;
import de.trustable.ca3s.core.repository.BPNMProcessInfoRepository;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.PipelineAttributeRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.dto.ACMEConfigItems;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.dto.RDNRestriction;
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

	
	Logger LOG = LoggerFactory.getLogger(PipelineUtil.class);

	@Autowired
	private CAConnectorConfigRepository caConnRepository;

	@Autowired
    private PipelineRepository pipelineRepository;

	@Autowired
	private PipelineAttributeRepository pipelineAttRepository;
	
	@Autowired
    private BPNMProcessInfoRepository bpmnPIRepository;


	
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
    	
    	pv.setRestriction_C(new RDNRestriction());
		pv.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv.setRestriction_CN(new RDNRestriction());
		pv.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_ONE);
    	pv.setRestriction_L(new RDNRestriction());
		pv.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv.setRestriction_O(new RDNRestriction());
		pv.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv.setRestriction_OU(new RDNRestriction());
		pv.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	pv.setRestriction_S(new RDNRestriction());
		pv.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.NOT_ALLOWED);
    	
    	pv.setRestriction_SAN(new RDNRestriction());
		pv.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.ZERO_OR_MANY);
    	
    	ACMEConfigItems acmeConfigItems = new ACMEConfigItems ();
    	
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
    		}
    	}
    	
    	pv.setAcmeConfigItems(acmeConfigItems);
    	
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
	        }
        }else {
        	p = new Pipeline();
        }

		p.setId(pv.getId());
		p.setName(pv.getName());
		p.setDescription(pv.getDescription());
		p.setType(pv.getType());
		p.setUrlPart(pv.getUrlPart());
		p.setApprovalRequired(pv.getApprovalRequired());

		List<CAConnectorConfig> ccc = caConnRepository.findByName(pv.getCaConnectorName());
		if( ccc.isEmpty()) {
			p.setCaConnector(null);
		}else {
			p.setCaConnector(ccc.get(0));
		}
		
		Optional<BPNMProcessInfo> bpiOpt = bpmnPIRepository.findByName(pv.getProcessInfoName());
		if( bpiOpt.isPresent()) {
			p.setProcessInfo(bpiOpt.get());
		}else {
			p.setProcessInfo(null);
		}
		
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
		
		p.setPipelineAttributes(pipelineAttributes);
		
/*		
		for(PipelineAttribute pa: p.getPipelineAttributes()) {
			LOG.debug("PipelineAttribute : " +  pa);
		}
		
*/
    	pipelineAttRepository.saveAll(p.getPipelineAttributes());
		pipelineRepository.save(p);
    	
		return p;
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
				String msg = "restrcition mismatch: A SAN MUST NOT occur!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE.equals(cardinality)) {
			if( n ==  0) {
				String msg = "restrcition mismatch: SAN MUST occur once, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
			if( n != 1) {
				String msg = "restrcition mismatch: SAN MUST occur exactly once, found "+n+" times!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
			if( n == 0) {
				String msg = "restrcition mismatch: SAns MUST occur once or more, missing here!";
				messageList.add(msg);
				LOG.debug(msg);
				outcome = false;
			}
		} else if( RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
			if( n > 1) {
				String msg = "restrcition mismatch: SANs MUST occur zero or once, found "+n+" times!";
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
