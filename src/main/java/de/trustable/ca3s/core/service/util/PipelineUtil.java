package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.config.Constants;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.*;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.*;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.util.CryptoUtil;
import de.trustable.util.OidNameMapper;
import de.trustable.util.Pkcs10RequestHolder;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import java.util.stream.Collectors;

import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_TYPED_SAN;
import static de.trustable.ca3s.core.domain.enumeration.CnAsSanRestriction.CN_AS_SAN_IGNORE;
import static de.trustable.ca3s.core.domain.enumeration.CnAsSanRestriction.CN_AS_SAN_REQUIRED;


@Service
public class PipelineUtil {


    public static final String RESTR_C_CARDINALITY = "RESTR_C_CARDINALITY";
    public static final String RESTR_C_TEMPLATE = "RESTR_C_TEMPLATE";
    public static final String RESTR_C_REGEXMATCH = "RESTR_C_REGEXMATCH";
    public static final String RESTR_C_REGEX = "RESTR_C_REGEX";

    public static final String RESTR_CN_CARDINALITY = "RESTR_CN_CARDINALITY";
    public static final String RESTR_CN_TEMPLATE = "RESTR_CN_TEMPLATE";
    public static final String RESTR_CN_REGEXMATCH = "RESTR_CN_REGEXMATCH";
    public static final String RESTR_CN_REGEX = "RESTR_CN_REGEX";

    public static final String RESTR_O_CARDINALITY = "RESTR_O_CARDINALITY";
    public static final String RESTR_O_TEMPLATE = "RESTR_O_TEMPLATE";
    public static final String RESTR_O_REGEXMATCH = "RESTR_O_REGEXMATCH";
    public static final String RESTR_O_REGEX = "RESTR_O_REGEX";

    public static final String RESTR_OU_CARDINALITY = "RESTR_OU_CARDINALITY";
    public static final String RESTR_OU_TEMPLATE = "RESTR_OU_TEMPLATE";
    public static final String RESTR_OU_REGEXMATCH = "RESTR_OU_REGEXMATCH";
    public static final String RESTR_OU_REGEX = "RESTR_OU_REGEX";

    public static final String RESTR_L_CARDINALITY = "RESTR_L_CARDINALITY";
    public static final String RESTR_L_TEMPLATE = "RESTR_L_TEMPLATE";
    public static final String RESTR_L_REGEXMATCH = "RESTR_L_REGEXMATCH";
    public static final String RESTR_L_REGEX = "RESTR_L_REGEX";

    public static final String RESTR_S_CARDINALITY = "RESTR_S_CARDINALITY";
    public static final String RESTR_S_TEMPLATE = "RESTR_S_TEMPLATE";
    public static final String RESTR_S_REGEXMATCH = "RESTR_S_REGEXMATCH";
    public static final String RESTR_S_REGEX = "RESTR_S_REGEX";

    public static final String RESTR_E_CARDINALITY = "RESTR_E_CARDINALITY";
    public static final String RESTR_E_TEMPLATE = "RESTR_E_TEMPLATE";
    public static final String RESTR_E_REGEXMATCH = "RESTR_E_REGEXMATCH";
    public static final String RESTR_E_REGEX = "RESTR_E_REGEX";

    public static final String RESTR_SAN_CARDINALITY = "RESTR_SAN_CARDINALITY";
    public static final String RESTR_SAN_TEMPLATE = "RESTR_SAN_TEMPLATE";
    public static final String RESTR_SAN_REGEXMATCH = "RESTR_SAN_REGEXMATCH";
    public static final String RESTR_SAN_REGEX = "RESTR_SAN_REGEX";

    public static final String RESTR_ARA_PREFIX = "RESTR_ARA_";
    public static final String RESTR_ARA_PATTERN = RESTR_ARA_PREFIX + "(.*)_(.*)";
    public static final String RESTR_ARA_NAME = "NAME";
    //	public static final String RESTR_ARA_CARDINALITY = "CARDINALITY";
    public static final String RESTR_ARA_TEMPLATE = "TEMPLATE";
    public static final String RESTR_ARA_REGEX = "REGEX";
    public static final String RESTR_ARA_REGEXMATCH = "REGEXMATCH";
    public static final String RESTR_ARA_REQUIRED = "REQUIRED";
    public static final String RESTR_ARA_COMMENT = "COMMENT";
    public static final String RESTR_ARA_CONTENT_TYPE = "ARAContentType";

    public static final String CN_AS_SAN_RESTRICTION = "CN_AS_SAN_RESTRICTION";
    public static final String KEY_UNIQUENESS = "KEY_UNIQUENESS";
    public static final String TOS_AGREEMENT_REQUIRED = "TOS_AGREEMENT_REQUIRED";
    public static final String TOS_AGREEMENT_LINK = "TOS_AGREEMENT_LINK";
    public static final String WEBSITE_LINK = "WEBSITE_LINK";
    public static final String CAA_IDENTITIES = "CAA_IDENTITIES";
    public static final String ACME_EAB_REQUIRED = "EAB_REQUIRED";

    public static final String ALLOW_IP_AS_SUBJECT = "ALLOW_IP_AS_SUBJECT";
    public static final String ALLOW_IP_AS_SAN = "ALLOW_IP_AS_SAN";
    public static final String TO_PENDIND_ON_FAILED_RESTRICTIONS = "TO_PENDIND_ON_FAILED_RESTRICTIONS";

    public static final String DOMAIN_RA_OFFICER = "DOMAIN_RA_OFFICER";
    public static final String NOTIFY_RA_OFFICER_ON_PENDING = "NOTIFY_RA_OFFICER_ON_PENDING";
    public static final String NOTIFY_DOMAIN_RA_OFFICER_ON_PENDING = "NOTIFY_DOMAIN_RA_OFFICER_ON_PENDING";

    public static final String ADDITIONAL_EMAIL_RECIPIENTS = "ADDITIONAL_EMAIL_RECIPIENTS";
    public static final String CAN_ISSUE_2_FACTOR_CLIENT_CERTS = "CAN_ISSUE_2_FACTOR_CLIENT_CERTS";

    public static final String  NETWORK_ACCEPT = "NETWORK_ACCEPT";
    public static final String  NETWORK_REJECT = "NETWORK_REJECT";

    public static final String ACME_ALLOW_CHALLENGE_HTTP01 = "ACME_ALLOW_CHALLENGE_HTTP01";
    public static final String ACME_ALLOW_CHALLENGE_ALPN = "ACME_ALLOW_CHALLENGE_ALPN";
    public static final String ACME_ALLOW_CHALLENGE_DNS = "ACME_ALLOW_CHALLENGE_DNS";

    public static final String ACME_ALLOW_CHALLENGE_WILDCARDS = "ACME_ALLOW_WILDCARDS";

    public static final String ACME_CHECK_CAA = "ACME_CHECK_CAA";
    public static final String ACME_NAME_CAA = "ACME_NAME_CAA";
    public static final String ACME_CONTACT_EMAIL_REGEX = "ACME_CONTACT_EMAIL_REGEX";
    public static final String ACME_CONTACT_EMAIL_REGEX_REJECT = "ACME_CONTACT_EMAIL_REGEX_REJECT";

    public static final String CSR_USAGE = "CSR_USAGE";

    public static final String LIST_ORDER = "LIST_ORDER";

    public static final String ACME_ORDER_VALIDITY_SECONDS = "ACME_ORDER_VALIDITY_SECONDS";
    public static final String ACME_NOTIFY_ACCOUNT_CONTACT_ON_ERROR = "ACME_NOTIFY_ACCOUNT_CONTACT_ON_ERROR";

    public static final String SCEP_CAPABILITY_RENEWAL = "SCEP_CAPABILITY_RENEWAL";
    public static final String SCEP_CAPABILITY_POST = "SCEP_CAPABILITY_POST";
    public static final String SCEP_SECRET_PC_ID = "SCEP_SECRET_PC_ID";

    public static final String SCEP_RECIPIENT_DN = "SCEP_RECIPIENT_DN";
    public static final String SCEP_RECIPIENT_KEY_TYPE_LEN = "SCEP_RECIPIENT_KEY_TYPE_LEN";
    public static final String SCEP_CA_CONNECTOR_RECIPIENT_NAME = "SCEP_CA_CONNECTOR_RECIPIENT_NAME";
    public static final String SCEP_PERIOD_DAYS_RENEWAL = "SCEP_PERIOD_DAYS_RENEWAL";
    public static final String SCEP_PERCENTAGE_OF_VALIDITY_BEFORE_RENEWAL = "SCEP_PERCENTAGE_OF_VALIDITY_BEFORE_RENEWAL";
    private final CSRUtil cSRUtil;


    Logger LOG = LoggerFactory.getLogger(PipelineUtil.class);

    final private CertificateRepository certRepository;
    final private CSRRepository csrRepository;
    final private CAConnectorConfigRepository caConnRepository;
    final private PipelineRepository pipelineRepository;
    final private PipelineAttributeRepository pipelineAttRepository;
    final private BPMNProcessInfoRepository bpmnPIRepository;
    final private ProtectedContentRepository protectedContentRepository;
    final private ProtectedContentUtil protectedContentUtil;
    final private CertificateUtil certUtil;
    final private AlgorithmRestrictionUtil algorithmRestrictionUtil;
    final private ConfigUtil configUtil;
    final private RequestUtil requestUtil;
    final private AuditService auditService;

    final private AuditTraceRepository auditTraceRepository;
    final private NotificationService notificationService;
    final private TenantRepository tenantRepository;
    final private AuthorityRepository authorityRepository;
    final private RequestProxyConfigRepository requestProxyConfigRepository;
    final private String defaultKeySpec;

    private final RandomUtil randomUtil;

    public PipelineUtil(CertificateRepository certRepository,
                        CSRRepository csrRepository,
                        CAConnectorConfigRepository caConnRepository,
                        PipelineRepository pipelineRepository,
                        PipelineAttributeRepository pipelineAttRepository,
                        BPMNProcessInfoRepository bpmnPIRepository,
                        ProtectedContentRepository protectedContentRepository,
                        ProtectedContentUtil protectedContentUtil,
                        CertificateUtil certUtil,
                        AlgorithmRestrictionUtil algorithmRestrictionUtil,
                        ConfigUtil configUtil,
                        RequestUtil requestUtil,
                        AuditService auditService,
                        AuditTraceRepository auditTraceRepository,
                        @Lazy NotificationService notificationService,
                        TenantRepository tenantRepository,
                        AuthorityRepository authorityRepository,
                        RequestProxyConfigRepository requestProxyConfigRepository,
                        @Value("${ca3s.keyspec.default:RSA_4096}") String defaultKeySpec,
                        RandomUtil randomUtil,
                        @Lazy CSRUtil cSRUtil) {

        this.certRepository = certRepository;
        this.csrRepository = csrRepository;
        this.caConnRepository = caConnRepository;
        this.pipelineRepository = pipelineRepository;
        this.pipelineAttRepository = pipelineAttRepository;
        this.bpmnPIRepository = bpmnPIRepository;
        this.protectedContentRepository = protectedContentRepository;
        this.protectedContentUtil = protectedContentUtil;
        this.certUtil = certUtil;
        this.algorithmRestrictionUtil = algorithmRestrictionUtil;
        this.configUtil = configUtil;
        this.requestUtil = requestUtil;
        this.auditService = auditService;
        this.auditTraceRepository = auditTraceRepository;
        this.notificationService = notificationService;
        this.tenantRepository = tenantRepository;
        this.authorityRepository = authorityRepository;
        this.requestProxyConfigRepository = requestProxyConfigRepository;
        // @ToDo check back with the list of valid algos
        this.defaultKeySpec = defaultKeySpec;
        this.randomUtil = randomUtil;
        this.cSRUtil = cSRUtil;
    }


    public PipelineView from(Pipeline pipeline) {

        PipelineView pv = new PipelineView();

        pv.setId(pipeline.getId());
        pv.setName(pipeline.getName());
        pv.setType(pipeline.getType());
        pv.setActive(pipeline.isActive());
        pv.setExpiryDate(pipeline.getExpiryDate(certUtil));

        pv.setDescription(pipeline.getDescription());
        pv.setApprovalRequired(pipeline.isApprovalRequired());
        pv.setUrlPart(pipeline.getUrlPart());

        if (pipeline.getCaConnector() != null) {
            pv.setCaConnectorId(pipeline.getCaConnector().getId());
            pv.setCaConnectorName(pipeline.getCaConnector().getName());
        }

        if (pipeline.getProcessInfoCreate() != null) {
            pv.setProcessInfoNameCreate(pipeline.getProcessInfoCreate().getName());
        }

        if (pipeline.getProcessInfoRevoke() != null) {
            pv.setProcessInfoNameRevoke(pipeline.getProcessInfoRevoke().getName());
        }

        if (pipeline.getProcessInfoNotify() != null) {
            pv.setProcessInfoNameNotify(pipeline.getProcessInfoNotify().getName());
        }

        RDNRestriction[] rdnRestrictArr = initRdnRestrictions(pv, pipeline);
        pv.setRdnRestrictions(rdnRestrictArr);

        pv.setAraRestrictions(new ARARestriction[0]);

        AcmeConfigItems acmeConfigItems = new AcmeConfigItems();
        SCEPConfigItems scepConfigItems = new SCEPConfigItems();
        WebConfigItems webConfigItems = new WebConfigItems();

        pv.setCsrUsage(CsrUsage.TLS_SERVER);

        List<String> domainRaOfficerList = new ArrayList<>();

        if( pipeline.getRequestProxies() != null) {
            pv.setRequestProxyConfigIds(pipeline.getRequestProxies().stream().mapToLong(r -> r.getId()).toArray());
        }

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {

            if (ACME_ALLOW_CHALLENGE_HTTP01.equals(plAtt.getName())) {
                acmeConfigItems.setAllowChallengeHTTP01(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_ALLOW_CHALLENGE_ALPN.equals(plAtt.getName())) {
                acmeConfigItems.setAllowChallengeAlpn(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_ALLOW_CHALLENGE_DNS.equals(plAtt.getName())) {
                acmeConfigItems.setAllowChallengeDNS(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_ALLOW_CHALLENGE_WILDCARDS.equals(plAtt.getName())) {
                acmeConfigItems.setAllowWildcards(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_CHECK_CAA.equals(plAtt.getName())) {
                acmeConfigItems.setCheckCAA(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_NAME_CAA.equals(plAtt.getName())) {
                acmeConfigItems.setCaNameCAA(plAtt.getValue());
            } else if (ACME_CONTACT_EMAIL_REGEX.equals(plAtt.getName())) {
                acmeConfigItems.setContactEMailRegEx(plAtt.getValue());
            } else if (ACME_CONTACT_EMAIL_REGEX_REJECT.equals(plAtt.getName())) {
                acmeConfigItems.setContactEMailRejectRegEx(plAtt.getValue());
            } else if (ACME_EAB_REQUIRED.equals(plAtt.getName())) {
                acmeConfigItems.setExternalAccountRequired(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ACME_NOTIFY_ACCOUNT_CONTACT_ON_ERROR.equals(plAtt.getName())) {
                acmeConfigItems.setNotifyContactsOnError(Boolean.parseBoolean(plAtt.getValue()));
            } else if (DOMAIN_RA_OFFICER.equals(plAtt.getName())) {
                domainRaOfficerList.add(plAtt.getValue());
            } else if (NOTIFY_RA_OFFICER_ON_PENDING.equals(plAtt.getName())) {
                webConfigItems.setNotifyRAOfficerOnPendingRequest(Boolean.parseBoolean(plAtt.getValue()));
            } else if (NOTIFY_DOMAIN_RA_OFFICER_ON_PENDING.equals(plAtt.getName())) {
                webConfigItems.setNotifyDomainRAOfficerOnPendingRequest(Boolean.parseBoolean(plAtt.getValue()));
            } else if (ADDITIONAL_EMAIL_RECIPIENTS.equals(plAtt.getName())) {
                webConfigItems.setAdditionalEMailRecipients(plAtt.getValue());
            } else if (NETWORK_ACCEPT.equals(plAtt.getName())) {
                pv.setNetworkAcceptArr(splitNetworks(plAtt.getValue()));
            } else if (NETWORK_REJECT.equals(plAtt.getName())) {
                pv.setNetworkRejectArr(splitNetworks(plAtt.getValue()));

            } else if (CN_AS_SAN_RESTRICTION.equals(plAtt.getName()))  {
                if(plAtt.getValue() == null || plAtt.getValue().isEmpty()){
                    pv.setCnAsSanRestriction(CN_AS_SAN_IGNORE);
                }else {
                    pv.setCnAsSanRestriction(CnAsSanRestriction.valueOf(plAtt.getValue()));
                }
            } else if (KEY_UNIQUENESS.equals(plAtt.getName())) {
                if(plAtt.getValue() == null || plAtt.getValue().isEmpty()){
                    pv.setKeyUniqueness(KeyUniqueness.KEY_UNIQUE);
                }else {
                    pv.setKeyUniqueness(KeyUniqueness.valueOf(plAtt.getValue()));
                }

            } else if (TOS_AGREEMENT_REQUIRED.equals(plAtt.getName())) {
                pv.setTosAgreementRequired(Boolean.parseBoolean(plAtt.getValue()));
            } else if (TOS_AGREEMENT_LINK.equals(plAtt.getName())) {
                pv.setTosAgreementLink(plAtt.getValue());

            } else if (WEBSITE_LINK.equals(plAtt.getName())) {
                pv.setWebsite(plAtt.getValue());

            } else if (CSR_USAGE.equals(plAtt.getName())) {
                pv.setCsrUsage(CsrUsage.valueOf(plAtt.getValue()));

            } else if (LIST_ORDER.equals(plAtt.getName())) {
                pv.setListOrder(Integer.parseInt(plAtt.getValue()));

            } else if (CAN_ISSUE_2_FACTOR_CLIENT_CERTS.equals(plAtt.getName())) {
                webConfigItems.setIssuesSecondFactorClientCert(Boolean.parseBoolean(plAtt.getValue()));
            } else if (SCEP_RECIPIENT_DN.equals(plAtt.getName())) {
                scepConfigItems.setScepRecipientDN(plAtt.getValue());
            } else if (SCEP_RECIPIENT_KEY_TYPE_LEN.equals(plAtt.getName())) {
                KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.from(plAtt.getValue());
                scepConfigItems.setKeyAlgoLength(keyAlgoLength);
            } else if (SCEP_CA_CONNECTOR_RECIPIENT_NAME.equals(plAtt.getName())) {
                scepConfigItems.setCaConnectorRecipientName(plAtt.getValue());
            } else if (SCEP_CAPABILITY_RENEWAL.equals(plAtt.getName())) {
                scepConfigItems.setCapabilityRenewal(Boolean.parseBoolean(plAtt.getValue()));
            } else if (SCEP_PERIOD_DAYS_RENEWAL.equals(plAtt.getName())) {
                scepConfigItems.setPeriodDaysRenewal(Integer.parseInt(plAtt.getValue()));
            } else if (SCEP_PERCENTAGE_OF_VALIDITY_BEFORE_RENEWAL.equals(plAtt.getName())) {
                scepConfigItems.setPercentageOfValidtyBeforeRenewal(Integer.parseInt(plAtt.getValue()));
            } else if (SCEP_SECRET_PC_ID.equals(plAtt.getName())) {

                Optional<ProtectedContent> optPC = protectedContentRepository.findById(Long.parseLong(plAtt.getValue()));
                if (optPC.isPresent()) {
                    ProtectedContent pc = optPC.get();
                    try {
                        String clearContent = protectedContentUtil.unprotectString(pc.getContentBase64());
                        scepConfigItems.setScepSecret(clearContent);
                        scepConfigItems.setScepSecretPCId(pc.getId().toString());
                        LOG.debug("pc id : " + pc.getId() + ", clearContent retrieved");
                        if (pc.getValidTo() == null) {
                            // Initialize to midnight
                            scepConfigItems.setScepSecretValidTo(Instant.now().truncatedTo(ChronoUnit.DAYS).plus(1, ChronoUnit.DAYS));
                        } else {
                            scepConfigItems.setScepSecretValidTo(pc.getValidTo());
                        }
                    } catch (EncryptionOperationNotPossibleException eonpe) {
                        LOG.error("pc id : " + pc.getId() + ", clearContent retrieval failed: " + eonpe.getMessage());

                    }
                } else {
                    scepConfigItems.setScepSecret("-- expired --");
                    LOG.debug("no protected content for pc id : " + plAtt.getValue());
                }
            }

        }

        if (pipeline.getProcessInfoRequestAuthorization() != null) {
            webConfigItems.setProcessInfoNameRequestAuthorization(pipeline.getProcessInfoRequestAuthorization().getName());
        }

        if (pipeline.getProcessInfoAccountAuthorization() != null) {
            acmeConfigItems.setProcessInfoNameAccountAuthorization(pipeline.getProcessInfoAccountAuthorization().getName());
        }

        pv.setDomainRaOfficerList(domainRaOfficerList.toArray(new String[0]));

        pv.setAcmeConfigItems(acmeConfigItems);
        pv.setScepConfigItems(scepConfigItems);
        pv.setWebConfigItems(webConfigItems);

        ARARestriction[] araRestrictions = initAraRestrictions(pipeline);
        pv.setAraRestrictions(araRestrictions);

        Tenant[] allTenants = tenantRepository.findAll().toArray(new Tenant[0]);
        pv.setAllTenantList(allTenants);

        Tenant[] selectedTenants = pipeline.getTenants().toArray(new Tenant[0]);
        pv.setSelectedTenantList(selectedTenants);

        Authority[] allAuthorities = authorityRepository.findAll().toArray(new Authority[0]);
        pv.setAllRolesList(allAuthorities);

        Authority[] selectedAuthorities = pipeline.getAuthorities().toArray(new Authority[0]);
        pv.setSelectedRolesList(selectedAuthorities);

        return pv;
    }

    @NotNull
    private RDNRestriction[] initRdnRestrictions(PipelineView pv, Pipeline pipeline) {

        // set default value for ke uniqueness
        pv.setKeyUniqueness(KeyUniqueness.KEY_UNIQUE);

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

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {

            if (ALLOW_IP_AS_SUBJECT.equals(plAtt.getName())) {
                pv.setIpAsSubjectAllowed(Boolean.parseBoolean(plAtt.getValue()));

            } else if (ALLOW_IP_AS_SAN.equals(plAtt.getName())) {
                pv.setIpAsSANAllowed(Boolean.parseBoolean(plAtt.getValue()));

            } else if (RESTR_C_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_C().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_C_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_C().setContentTemplate(plAtt.getValue());
            } else if (RESTR_C_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_C().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_C_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_C().setRegEx(plAtt.getValue());

            } else if (RESTR_CN_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_CN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_CN_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_CN().setContentTemplate(plAtt.getValue());
            } else if (RESTR_CN_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_CN().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_CN_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_CN().setRegEx(plAtt.getValue());

            } else if (RESTR_O_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_O().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_O_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_O().setContentTemplate(plAtt.getValue());
            } else if (RESTR_O_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_O().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_O_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_O().setRegEx(plAtt.getValue());

            } else if (RESTR_OU_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_OU().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_OU_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_OU().setContentTemplate(plAtt.getValue());
            } else if (RESTR_OU_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_OU().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_OU_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_OU().setRegEx(plAtt.getValue());

            } else if (RESTR_L_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_L().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_L_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_L().setContentTemplate(plAtt.getValue());
            } else if (RESTR_L_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_L().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_L_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_L().setRegEx(plAtt.getValue());

            } else if (RESTR_S_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_S().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_S_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_S().setContentTemplate(plAtt.getValue());
            } else if (RESTR_S_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_S().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_S_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_S().setRegEx(plAtt.getValue());

            } else if (RESTR_E_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_E().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_E_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_E().setContentTemplate(plAtt.getValue());
            } else if (RESTR_E_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_E().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_E_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_E().setRegEx(plAtt.getValue());

            } else if (RESTR_SAN_CARDINALITY.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setCardinalityRestriction(RDNCardinalityRestriction.valueOf(plAtt.getValue()));
            } else if (RESTR_SAN_TEMPLATE.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setContentTemplate(plAtt.getValue());
            } else if (RESTR_SAN_REGEXMATCH.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
            } else if (RESTR_SAN_REGEX.equals(plAtt.getName())) {
                pv.getRestriction_SAN().setRegEx(plAtt.getValue());

            } else if (TO_PENDIND_ON_FAILED_RESTRICTIONS.equals(plAtt.getName())) {
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
        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (plAtt.getName().startsWith(RESTR_ARA_PREFIX)) {
                Matcher m = araPattern.matcher(plAtt.getName());
                if (m.find()) {
                    int araIdx = Integer.parseInt(m.group(1));
                    if (araIdx + 1 > nARA) {
                        nARA = araIdx + 1;
                    }
                }
            }
        }
        LOG.debug("#{} ARA itmes found", nARA);
        ARARestriction[] araRestrictions = new ARARestriction[nARA];

        /*
         * find all ARA restrictions
         */
        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (plAtt.getName().startsWith(RESTR_ARA_PREFIX)) {
                LOG.debug("ARA itmes : {}", plAtt.getName());
                Matcher m = araPattern.matcher(plAtt.getName());
                if (m.find()) {
                    int araIdx = Integer.parseInt(m.group(1));
                    LOG.debug("araIdx: {}", araIdx);
                    if (araRestrictions[araIdx] == null) {
                        araRestrictions[araIdx] = new ARARestriction();
                        araRestrictions[araIdx].setContentType( ARAContentType.NO_TYPE);
                    }
                    ARARestriction araRestriction = araRestrictions[araIdx];
                    String namePart = m.group(2);
                    LOG.debug("ARA namePart : {}", namePart);
                    if (RESTR_ARA_NAME.equals(namePart)) {
                        araRestriction.setName(plAtt.getValue());
                    } else if (RESTR_ARA_REQUIRED.equals(namePart)) {
                        araRestriction.setRequired(Boolean.parseBoolean(plAtt.getValue()));
                    } else if (RESTR_ARA_TEMPLATE.equals(namePart)) {
                        araRestriction.setContentTemplate(plAtt.getValue());
                    } else if (RESTR_ARA_COMMENT.equals(namePart)) {
                        araRestriction.setComment(plAtt.getValue());
                    } else if (RESTR_ARA_REGEXMATCH.equals(namePart)) {
                        araRestriction.setRegExMatch(Boolean.parseBoolean(plAtt.getValue()));
                    } else if (RESTR_ARA_REGEX.equals(namePart)) {
                        araRestriction.setRegEx(plAtt.getValue());
                    } else if (RESTR_ARA_CONTENT_TYPE.equals(namePart)) {
                        araRestriction.setContentType( ARAContentType.valueOf(plAtt.getValue()));
                    }

                }
            }
        }
        return araRestrictions;
    }


    /**
     * @param pv
     * @return
     */
    public Pipeline toPipeline(PipelineView pv) {

        List<AuditTrace> auditList = new ArrayList<>();
        Pipeline p;
        List<Pipeline> pipelineList = pipelineRepository.findByName(pv.getName());
        if (pv.getId() != null) {
            Optional<Pipeline> optP = pipelineRepository.findById(pv.getId());
            if (optP.isPresent()) {
                p = optP.get();
                if (!pipelineList.isEmpty() && !pipelineList.get(0).getId().equals(p.getId())) {
                    throw new BadRequestAlertException("Name '" + pv.getName() + "' already assigned", "pipeline", "name already used");
                }
                pipelineAttRepository.deleteAll(p.getPipelineAttributes());
            } else {
                if (!pipelineList.isEmpty()) {
                    throw new BadRequestAlertException("Name '" + pv.getName() + "' already assigned", "pipeline", "name already used");
                }
                p = new Pipeline();
                pipelineRepository.save(p);
                auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_CREATED, p));
            }
        } else {
            if (!pipelineList.isEmpty()) {
                throw new BadRequestAlertException("Name '" + pv.getName() + "' already assigned", "pipeline", "name already used");
            }

            // check uniqueness of realm for ACME and SCEP, only
            if( PipelineType.ACME.equals(pv.getType()) || PipelineType.SCEP.equals(pv.getType())) {
                Pipeline pipelineByName = getPipelineByRealm(pv.getType(), pv.getUrlPart());
                if (pipelineByName != null) {
                    throw new BadRequestAlertException("Realm '" + pv.getUrlPart() + "' already exists with pipeline " + pipelineByName.getName() + " / " + pipelineByName.getUrlPart(), "pipeline", "realmexists");
                }
            }

            p = new Pipeline();
            p.setName(pv.getName());
            p.setType(pv.getType());
            pipelineRepository.save(p);
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_COPIED, p));
        }

        if (!Objects.equals(pv.getName(), p.getName())) {
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_NAME_CHANGED, p.getName(), pv.getName(), p));
            p.setName(pv.getName());
        }
        if (!Objects.equals(pv.getDescription(), p.getDescription())) {
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_DESCRIPTION_CHANGED, p.getDescription(), pv.getDescription(), p));
            p.setDescription(pv.getDescription());
        }
        if (!pv.getType().equals(p.getType())) {
            String oldType = "";
            if (p.getType() != null) {
                oldType = p.getType().toString();
            }
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_TYPE_CHANGED, oldType, pv.getType().toString(), p));
            p.setType(pv.getType());
        }

        if(pv.getType() == PipelineType.WEB || pv.getType() == PipelineType.MANUAL_UPLOAD){
            pv.setUrlPart(""); // no url part for web pipelines
        }
        if (!Objects.equals(pv.getUrlPart(), p.getUrlPart())) {
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_URLPART_CHANGED, p.getUrlPart(), pv.getUrlPart(), p));
            p.setUrlPart(pv.getUrlPart());
        }

        if (pv.getApprovalRequired() != p.isApprovalRequired()) {
            String isApprovalRequired = "";
            if (p.isApprovalRequired() != null) {
                isApprovalRequired = p.isApprovalRequired().toString();
            }
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_APPROVAL_REQUIRED_CHANGED, isApprovalRequired, pv.getApprovalRequired().toString(), p));
            p.setApprovalRequired(pv.getApprovalRequired());
        }

        if (pv.getActive() != p.isActive()) {
            String isActive = "";
            if (p.isActive() != null) {
                isActive = p.isActive().toString();
            }
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_ACTIVE_CHANGED, isActive, pv.getActive().toString(), p));
            p.setActive(pv.getActive());
        }
        if (p.isActive() == null) {
            auditList.add(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_ACTIVE_CHANGED, null, Boolean.FALSE.toString(), p));
            p.setActive(Boolean.FALSE);
        }

        String oldCaConnectorName = "";
        if (p.getCaConnector() != null) {
            oldCaConnectorName = p.getCaConnector().getName();
        }

        List<CAConnectorConfig> ccc = caConnRepository.findByName(pv.getCaConnectorName());
        if (ccc.isEmpty()) {
            p.setCaConnector(null);
            auditList.add(auditService.createAuditTracePipelineAttribute("CA_CONNECTOR", oldCaConnectorName, "", p));

        } else {
            p.setCaConnector(ccc.get(0));
            if (!ccc.get(0).getName().equals(oldCaConnectorName)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("CA_CONNECTOR", oldCaConnectorName, ccc.get(0).getName(), p));
            }
        }

        // Process Create
        String oldProcessNameCreate = "";
        if (p.getProcessInfoCreate() != null) {
            oldProcessNameCreate = p.getProcessInfoCreate().getName();
        }

        List<BPMNProcessInfo> bpmnProcessInfoList = bpmnPIRepository.findByNameOrderedBylastChange(pv.getProcessInfoNameCreate());
        if(!bpmnProcessInfoList.isEmpty()) {
            BPMNProcessInfo bpi = bpmnProcessInfoList.get(0);
            p.setProcessInfoCreate(bpi);
            if (!bpi.getName().equals(oldProcessNameCreate)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ISSUANCE_PROCESS", oldProcessNameCreate, bpi.getName(), p));
            }
        } else {
            p.setProcessInfoCreate(null);
            if (!oldProcessNameCreate.isEmpty()) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ISSUANCE_PROCESS", oldProcessNameCreate, "", p));
            }
        }

        // Process Revoke
        String oldProcessNameRevoke = "";
        if (p.getProcessInfoRevoke() != null) {
            oldProcessNameRevoke = p.getProcessInfoRevoke().getName();
        }

        List<BPMNProcessInfo> bpmnProcessInfoRevokeList = bpmnPIRepository.findByNameOrderedBylastChange(pv.getProcessInfoNameRevoke());
        if(!bpmnProcessInfoRevokeList.isEmpty()) {
            BPMNProcessInfo bpi = bpmnProcessInfoRevokeList.get(0);
            p.setProcessInfoRevoke(bpi);
            if (!bpi.getName().equals(oldProcessNameRevoke)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("REVOCATION_PROCESS", oldProcessNameRevoke, bpi.getName(), p));
            }
        } else {
            p.setProcessInfoRevoke(null);
            if (!oldProcessNameRevoke.isEmpty()) {
                auditList.add(auditService.createAuditTracePipelineAttribute("REVOCATION_PROCESS", oldProcessNameRevoke, "", p));
            }
        }

        // Process Notify
        String oldProcessNameNotify = "";
        if (p.getProcessInfoNotify() != null) {
            oldProcessNameNotify = p.getProcessInfoNotify().getName();
        }

        List<BPMNProcessInfo> bpmnProcessInfoNotifyList = bpmnPIRepository.findByNameOrderedBylastChange(pv.getProcessInfoNameNotify());
        if(!bpmnProcessInfoNotifyList.isEmpty()) {
            BPMNProcessInfo bpi = bpmnProcessInfoNotifyList.get(0);
            p.setProcessInfoNotify(bpi);
            if (!bpi.getName().equals(oldProcessNameNotify)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("NOTIFICATION_PROCESS", oldProcessNameNotify, bpi.getName(), p));
            }
        } else {
            p.setProcessInfoNotify(null);
            if (!oldProcessNameNotify.isEmpty()) {
                auditList.add(auditService.createAuditTracePipelineAttribute("NOTIFICATION_PROCESS", oldProcessNameNotify, "", p));
            }
        }

        // Process Request Authorization
        String oldProcessNameRequestAuthorization = "";
        if (p.getProcessInfoRequestAuthorization() != null) {
            oldProcessNameRequestAuthorization = p.getProcessInfoRequestAuthorization().getName();
        }

        List<BPMNProcessInfo> bpmnProcessInfoRequestAuthorizationList = new ArrayList<>();
        if(pv.getWebConfigItems() != null && pv.getWebConfigItems().getProcessInfoNameRequestAuthorization() != null) {
            bpmnProcessInfoRequestAuthorizationList = bpmnPIRepository.findByNameOrderedBylastChange(pv.getWebConfigItems().getProcessInfoNameRequestAuthorization());
        }
        if(!bpmnProcessInfoRequestAuthorizationList.isEmpty()) {
            BPMNProcessInfo bpi = bpmnProcessInfoRequestAuthorizationList.get(0);
            p.setProcessInfoRequestAuthorization(bpi);
            if (!bpi.getName().equals(oldProcessNameRequestAuthorization)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("REQUEST_AUTHORIZATION_PROCESS", oldProcessNameRequestAuthorization, bpi.getName(), p));
            }
        } else {
            p.setProcessInfoRequestAuthorization(null);
            if (!oldProcessNameRequestAuthorization.isEmpty()) {
                auditList.add(auditService.createAuditTracePipelineAttribute("REQUEST_AUTHORIZATION_PROCESS", oldProcessNameRequestAuthorization, "", p));
            }
        }

        // Process Account Authorization
        String oldProcessNameAccountAuthorization = "";
        if (p.getProcessInfoAccountAuthorization() != null) {
            oldProcessNameAccountAuthorization = p.getProcessInfoAccountAuthorization().getName();
        }

        List<BPMNProcessInfo> bpmnProcessInfoAccountAuthorizationList = new ArrayList<>();
        if(pv.getAcmeConfigItems() != null && pv.getAcmeConfigItems().getProcessInfoNameAccountAuthorization() != null ) {
            bpmnProcessInfoAccountAuthorizationList = bpmnPIRepository.findByNameOrderedBylastChange(pv.getAcmeConfigItems().getProcessInfoNameAccountAuthorization());
        }
        if(!bpmnProcessInfoAccountAuthorizationList.isEmpty()) {
            BPMNProcessInfo bpi = bpmnProcessInfoAccountAuthorizationList.get(0);
            p.setProcessInfoAccountAuthorization(bpi);
            if (!bpi.getName().equals(oldProcessNameAccountAuthorization)) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ACCOUNT_AUTHORIZATION_PROCESS", oldProcessNameAccountAuthorization, bpi.getName(), p));
            }
        } else {
            p.setProcessInfoAccountAuthorization(null);
            if (!oldProcessNameAccountAuthorization.isEmpty()) {
                auditList.add(auditService.createAuditTracePipelineAttribute("ACCOUNT_AUTHORIZATION_PROCESS", oldProcessNameAccountAuthorization, "", p));
            }
        }

        // Reequest Proxy
        Set<RequestProxyConfig> requestProxyConfigList = new HashSet<>();
        for (long requestProxyConfigId : pv.getRequestProxyConfigIds()) {
            Optional<RequestProxyConfig> requestProxyConfigOptional = requestProxyConfigRepository.findById(requestProxyConfigId);
            if (requestProxyConfigOptional.isPresent()) {
                RequestProxyConfig requestProxyConfigNew = requestProxyConfigOptional.get();
                requestProxyConfigList.add(requestProxyConfigNew);
                if (!p.getRequestProxies().contains(requestProxyConfigNew)) {
                    auditList.add(auditService.createAuditTracePipelineProxyAdded(requestProxyConfigNew.getName(), p));
                }
            }
        }

        for (RequestProxyConfig requestProxyConfig : p.getRequestProxies()) {
            if (requestProxyConfigList.contains(requestProxyConfig)) {
                auditList.add(auditService.createAuditTracePipelineProxyRemoved(requestProxyConfig.getName(), p));
            }
        }

        p.setRequestProxies(requestProxyConfigList);

        Set<PipelineAttribute> pipelineOldAttributes = new HashSet<>(p.getPipelineAttributes());
        LOG.debug("PipelineAttributes : cloned old #{}, new {}", pipelineOldAttributes.size(), p.getPipelineAttributes().size());

        Set<PipelineAttribute> pipelineAttributes = new HashSet<>();

        if (pv.getRestriction_C() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_CARDINALITY, pv.getRestriction_C().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_TEMPLATE, pv.getRestriction_C().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_REGEXMATCH, pv.getRestriction_C().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_C_REGEX, pv.getRestriction_C().getRegEx());
        }
        if (pv.getRestriction_CN() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_CARDINALITY, pv.getRestriction_CN().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_TEMPLATE, pv.getRestriction_CN().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_REGEXMATCH, pv.getRestriction_CN().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_CN_REGEX, pv.getRestriction_CN().getRegEx());
        }
        if (pv.getRestriction_O() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_CARDINALITY, pv.getRestriction_O().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_TEMPLATE, pv.getRestriction_O().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_REGEXMATCH, pv.getRestriction_O().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_O_REGEX, pv.getRestriction_O().getRegEx());
        }
        if (pv.getRestriction_OU() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_CARDINALITY, pv.getRestriction_OU().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_TEMPLATE, pv.getRestriction_OU().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_REGEXMATCH, pv.getRestriction_OU().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_OU_REGEX, pv.getRestriction_OU().getRegEx());
        }
        if (pv.getRestriction_L() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_CARDINALITY, pv.getRestriction_L().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_TEMPLATE, pv.getRestriction_L().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_REGEXMATCH, pv.getRestriction_L().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_L_REGEX, pv.getRestriction_L().getRegEx());
        }
        if (pv.getRestriction_S() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_CARDINALITY, pv.getRestriction_S().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_TEMPLATE, pv.getRestriction_S().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_REGEXMATCH, pv.getRestriction_S().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_S_REGEX, pv.getRestriction_S().getRegEx());
        }
        if (pv.getRestriction_E() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_CARDINALITY, pv.getRestriction_E().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_TEMPLATE, pv.getRestriction_E().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_REGEXMATCH, pv.getRestriction_E().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_E_REGEX, pv.getRestriction_E().getRegEx());
        }
        if (pv.getRestriction_SAN() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_CARDINALITY, pv.getRestriction_SAN().getCardinalityRestriction().name());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_TEMPLATE, pv.getRestriction_SAN().getContentTemplate());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_REGEXMATCH, pv.getRestriction_SAN().isRegExMatch());
            addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_SAN_REGEX, pv.getRestriction_SAN().getRegEx());
        }
        addPipelineAttribute(pipelineAttributes, p, auditList, ALLOW_IP_AS_SUBJECT, pv.isIpAsSubjectAllowed());
        addPipelineAttribute(pipelineAttributes, p, auditList, ALLOW_IP_AS_SAN, pv.isIpAsSANAllowed());
        LOG.debug("PipelineAttributes : ALLOW_IP_AS_SAN set to {}", pv.isIpAsSANAllowed());

        addPipelineAttribute(pipelineAttributes, p, auditList, TO_PENDIND_ON_FAILED_RESTRICTIONS, pv.isToPendingOnFailedRestrictions());


        if (pv.getAcmeConfigItems() == null) {
            AcmeConfigItems acmeConfigItems = new AcmeConfigItems();
            pv.setAcmeConfigItems(acmeConfigItems);
        }

        if (PipelineType.ACME.equals(pv.getType())) {
            // ensure that at least HTTP-01 challenge is available
            if (!pv.getAcmeConfigItems().isAllowChallengeDNS()) {
                pv.getAcmeConfigItems().setAllowChallengeHTTP01(true);
                pv.getAcmeConfigItems().setAllowWildcards(false);
            }
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_HTTP01, pv.getAcmeConfigItems().isAllowChallengeHTTP01());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_ALPN, pv.getAcmeConfigItems().isAllowChallengeAlpn());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_DNS, pv.getAcmeConfigItems().isAllowChallengeDNS());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_ALLOW_CHALLENGE_WILDCARDS, pv.getAcmeConfigItems().isAllowWildcards());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_CHECK_CAA, pv.getAcmeConfigItems().isCheckCAA());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_NAME_CAA, pv.getAcmeConfigItems().getCaNameCAA());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_CONTACT_EMAIL_REGEX, pv.getAcmeConfigItems().getContactEMailRegEx());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_CONTACT_EMAIL_REGEX_REJECT, pv.getAcmeConfigItems().getContactEMailRejectRegEx());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_NOTIFY_ACCOUNT_CONTACT_ON_ERROR, pv.getAcmeConfigItems().isNotifyContactsOnError());
            addPipelineAttribute(pipelineAttributes, p, auditList, ACME_EAB_REQUIRED, pv.getAcmeConfigItems().isExternalAccountRequired());
        }

        if( pv.getKeyUniqueness() == null){
            addPipelineAttribute(pipelineAttributes, p, auditList, KEY_UNIQUENESS, KeyUniqueness.KEY_UNIQUE.toString());
        }else {
            addPipelineAttribute(pipelineAttributes, p, auditList, KEY_UNIQUENESS, pv.getKeyUniqueness().toString());
        }

        if( pv.getCnAsSanRestriction() == null){
            addPipelineAttribute(pipelineAttributes, p, auditList, CN_AS_SAN_RESTRICTION, CN_AS_SAN_IGNORE.toString());
        }else {
            addPipelineAttribute(pipelineAttributes, p, auditList, CN_AS_SAN_RESTRICTION, pv.getCnAsSanRestriction().toString());
        }

        addPipelineAttribute(pipelineAttributes, p, auditList, TOS_AGREEMENT_REQUIRED, pv.isTosAgreementRequired());
        addPipelineAttribute(pipelineAttributes, p, auditList, TOS_AGREEMENT_LINK, pv.getTosAgreementLink());
        addPipelineAttribute(pipelineAttributes, p, auditList, WEBSITE_LINK, pv.getWebsite());

        addPipelineAttribute(pipelineAttributes, p, auditList, NETWORK_ACCEPT, concatNetworks(pv.getNetworkAcceptArr()));
        addPipelineAttribute(pipelineAttributes, p, auditList, NETWORK_REJECT, concatNetworks(pv.getNetworkRejectArr()));

        addPipelineAttribute(pipelineAttributes, p, auditList, CSR_USAGE, pv.getCsrUsage().toString());
        if (PipelineType.WEB.equals(pv.getType()) || PipelineType.MANUAL_UPLOAD.equals(pv.getType())) {
            addPipelineAttribute(pipelineAttributes, p, auditList, LIST_ORDER, "" + pv.getListOrder());
        }

        if (pv.getScepConfigItems() == null) {
            SCEPConfigItems scepConfigItems = new SCEPConfigItems();
            pv.setScepConfigItems(scepConfigItems);
        }

        if (PipelineType.SCEP.equals(pv.getType())) {
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CAPABILITY_RENEWAL, pv.getScepConfigItems().isCapabilityRenewal());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CAPABILITY_POST, pv.getScepConfigItems().isCapabilityPostPKIOperation());

            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_PERIOD_DAYS_RENEWAL, Integer.toString(pv.getScepConfigItems().getPeriodDaysRenewal()));
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_PERCENTAGE_OF_VALIDITY_BEFORE_RENEWAL, Integer.toString(pv.getScepConfigItems().getPercentageOfValidtyBeforeRenewal()));

            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_RECIPIENT_DN, pv.getScepConfigItems().getScepRecipientDN());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_RECIPIENT_KEY_TYPE_LEN, pv.getScepConfigItems().getKeyAlgoLength().toString());
            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_CA_CONNECTOR_RECIPIENT_NAME, pv.getScepConfigItems().getCaConnectorRecipientName());
        }

        if (pv.getWebConfigItems() != null) {
            addPipelineAttribute(pipelineAttributes, p, auditList, NOTIFY_RA_OFFICER_ON_PENDING, pv.getWebConfigItems().isNotifyRAOfficerOnPendingRequest());
            addPipelineAttribute(pipelineAttributes, p, auditList, NOTIFY_DOMAIN_RA_OFFICER_ON_PENDING, pv.getWebConfigItems().isNotifyDomainRAOfficerOnPendingRequest());
            addPipelineAttribute(pipelineAttributes, p, auditList, ADDITIONAL_EMAIL_RECIPIENTS, pv.getWebConfigItems().getAdditionalEMailRecipients());
            addPipelineAttribute(pipelineAttributes, p, auditList, CAN_ISSUE_2_FACTOR_CLIENT_CERTS, pv.getWebConfigItems().getIssuesSecondFactorClientCert());
        }

        if (pv.getDomainRaOfficerList() != null) {
            for (String domainOfficer : pv.getDomainRaOfficerList()) {
                addPipelineAttribute(pipelineAttributes, p, auditList, DOMAIN_RA_OFFICER, domainOfficer);
            }
        }

        if (PipelineType.SCEP.equals(pv.getType())) {
            ProtectedContent pc = getProtectedContent(pv, p,
                ProtectedContentType.PASSWORD, ContentRelationType.SCEP_PW,
                auditList);

            addPipelineAttribute(pipelineAttributes, p, auditList, SCEP_SECRET_PC_ID, pc.getId().toString());
        }

        p.setPipelineAttributes(pipelineAttributes);

/*
		for(PipelineAttribute pa: p.getPipelineAttributes()) {
			LOG.debug("PipelineAttribute : " +  pa);
		}

*/
        ARARestriction[] araRestrictions = pv.getAraRestrictions();
        if (araRestrictions != null) {
            int j = 0;
            for (ARARestriction araRestriction : araRestrictions) {
                String araName = araRestriction.getName();
                if (araName != null && !araName.trim().isEmpty()) {
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_NAME, araName.trim());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REQUIRED, araRestriction.isRequired());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_TEMPLATE, araRestriction.getContentTemplate());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_COMMENT, araRestriction.getComment());
                    if(araRestriction.getContentType() != null) {
                        addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_CONTENT_TYPE, araRestriction.getContentType().toString());
                    }
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REGEX, araRestriction.getRegEx());
                    addPipelineAttribute(pipelineAttributes, p, auditList, RESTR_ARA_PREFIX + j + "_" + RESTR_ARA_REGEXMATCH, araRestriction.isRegExMatch());
                    j++;
                }
            }
        }

        if( pv.getSelectedTenantList() != null){

            List<Tenant> tenantList = Arrays.asList(pv.getSelectedTenantList());

            List<Tenant> listOfAdditionalItems = tenantList.stream()
                .filter(item -> !p.getTenants().contains(item)).collect(Collectors.toList());

            for(Tenant t: listOfAdditionalItems) {
                auditList.add(auditService.createAuditTracePipelineAttribute("TENANT", "", "" + t.getId(), p));
            }

            List<Tenant> listOfRemovedItems = p.getTenants().stream()
                .filter(item -> !tenantList.contains(item)).collect(Collectors.toList());

            for(Tenant t: listOfRemovedItems) {
                auditList.add(auditService.createAuditTracePipelineAttribute("TENANT", "" + t.getId(),"", p));
            }

            if( !listOfAdditionalItems.isEmpty() || !listOfRemovedItems.isEmpty()) {
                p.setTenants(new HashSet<>(tenantList));
            }
        }

        if( pv.getSelectedRolesList() != null){

            List<Authority> authorityList = Arrays.asList(pv.getSelectedRolesList());

            List<Authority> listOfAdditionalItems = authorityList.stream()
                .filter(item -> !p.getAuthorities().contains(item)).collect(Collectors.toList());

            for(Authority authority: listOfAdditionalItems) {
                auditList.add(auditService.createAuditTracePipelineAttribute("AUTHORITY", "", authority.getName(), p));
            }

            List<Authority> listOfRemovedItems = p.getAuthorities().stream()
                .filter(item -> !authorityList.contains(item)).collect(Collectors.toList());

            for(Authority authority: listOfRemovedItems) {
                auditList.add(auditService.createAuditTracePipelineAttribute("AUTHORITY", authority.getName(),"", p));
            }

            if( !listOfAdditionalItems.isEmpty() || !listOfRemovedItems.isEmpty()) {
                p.setAuthorities(new HashSet<>(authorityList));
            }
        }


        auditTraceForAttributes(p, auditList, pipelineOldAttributes);

        p.setPipelineAttributes(pipelineAttributes);
        pipelineAttRepository.saveAll(p.getPipelineAttributes());
        pipelineRepository.save(p);
        auditTraceRepository.saveAll(auditList);

        if( pv.getWebConfigItems() != null &&
            ( pv.getWebConfigItems().getIssuesSecondFactorClientCert() != null ) &&
            pv.getWebConfigItems().getIssuesSecondFactorClientCert() ) {

            List<Pipeline> pipelineList2FAClientCert = pipelineRepository.findByAttributePresent(CAN_ISSUE_2_FACTOR_CLIENT_CERTS);
            for (Pipeline p2fa : pipelineList2FAClientCert) {
                if (p2fa != p) {
                    auditList.clear();
                    Set<PipelineAttribute> pipelineAttributes2FA = p2fa.getPipelineAttributes();
                    for(PipelineAttribute att: pipelineAttributes2FA){
                        if( att.getName().equals(CAN_ISSUE_2_FACTOR_CLIENT_CERTS)){
                            if("true".equalsIgnoreCase(att.getValue())) {
                                att.setValue("false");
                                auditList.add(auditService.createAuditTracePipelineAttribute(CAN_ISSUE_2_FACTOR_CLIENT_CERTS, "true", "false", p2fa));
                            }
                        }
                    }
                    pipelineAttRepository.saveAll(pipelineAttributes2FA);
                    pipelineRepository.save(p2fa);
                    auditTraceRepository.saveAll(auditList);
                }
            }
        }

        return p;
    }

    private ProtectedContent getProtectedContent(PipelineView pv, Pipeline p,
                                                 ProtectedContentType protectedContentType,
                                                 ContentRelationType contentRelationType,
                                                 List<AuditTrace> auditList) {
        ProtectedContent pc;
        List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(protectedContentType, contentRelationType, p.getId());
        if (listPC.isEmpty()) {

            pc = new ProtectedContent();
            pc.setType(protectedContentType);
            pc.setRelationType(contentRelationType);
            pc.setRelatedId(p.getId());
            pc.setCreatedOn(Instant.now());
            pc.setLeftUsages(-1);
            pc.setValidTo(ProtectedContentUtil.MAX_INSTANT);
            pc.setDeleteAfter(ProtectedContentUtil.MAX_INSTANT);

            LOG.debug("Protected Content created for {}", contentRelationType);
        } else {
            pc = listPC.get(0);
            LOG.debug("Protected Content found for {}", contentRelationType);
        }

        if( pv.getScepConfigItems() != null && pv.getScepConfigItems().getScepSecret() != null ) {

            String oldContent = protectedContentUtil.unprotectString(pc.getContentBase64());
            Instant validTo = Instant.now().plus(365, ChronoUnit.DAYS);
            if( pv.getScepConfigItems().getScepSecretValidTo() != null) {
                validTo = pv.getScepConfigItems().getScepSecretValidTo();
            }

            if (oldContent == null ||
                !oldContent.equals(pv.getScepConfigItems().getScepSecret()) ||
                pc.getValidTo() == null ||
                !pc.getValidTo().equals(validTo)) {


                pc.setContentBase64(protectedContentUtil.protectString(pv.getScepConfigItems().getScepSecret()));
                pc.setValidTo(validTo);
                pc.setDeleteAfter(validTo.plus(1, ChronoUnit.DAYS));
                protectedContentRepository.save(pc);
//                LOG.debug("SCEP password updated {} -> {}, {} -> {}", oldContent, pv.getScepConfigItems().getScepSecret(), validTo, pc.getValidTo());
                auditList.add(auditService.createAuditTracePipelineAttribute(contentRelationType.toString(), "#######", "******", p));
            }
        }
        return pc;
    }

    String[] splitNetworks(final String networks ){
        if( networks == null || networks.isEmpty()){
            return new String[0];
        }
        return networks.split(";");
    }

    String concatNetworks(final String[] networkArr ){

        String result = "";
        if( networkArr != null) {
            for (String network : networkArr) {
                if (!network.isEmpty()) {
                    if (!result.isEmpty()) {
                        result += (";" + network);
                    } else {
                        result = network;
                    }
                }
            }
        }
        return result;
    }

    private void auditTraceForAttributes(Pipeline p, List<AuditTrace> auditList, Set<PipelineAttribute> pipelineOldAttributes) {
        LOG.debug("matching PipelineAttributes : old #{}, new {}", pipelineOldAttributes.size(), p.getPipelineAttributes().size());

        for (PipelineAttribute pOld : pipelineOldAttributes) {

            boolean bFound = false;
            for (PipelineAttribute pNew : p.getPipelineAttributes()) {
                if (pNew.getName().equals(pOld.getName())) {
                    if (!Objects.equals(pNew.getValue(), pOld.getValue())) {
                        auditList.add(auditService.createAuditTracePipelineAttribute(pOld.getName(), pOld.getValue(), pNew.getValue(), p));
                    }
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                auditList.add(auditService.createAuditTracePipelineAttribute(pOld.getName(), pOld.getValue(), "", p));
            }
        }

        for (PipelineAttribute pNew : p.getPipelineAttributes()) {
            boolean bFound = false;
            for (PipelineAttribute pOld : pipelineOldAttributes) {
                if (pNew.getName().equals(pOld.getName())) {
                    bFound = true;
                    break;
                }
            }
            if (!bFound) {
                auditList.add(auditService.createAuditTracePipelineAttribute(pNew.getName(), "", pNew.getValue(), p));
                LOG.debug("matching PipelineAttributes : new name {} not found in old list", pNew.getName());
            }
        }
    }

    public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, List<AuditTrace> auditList, String name, Boolean value) {
        addPipelineAttribute(pipelineAttributes, p, auditList, name,
            (value == null)? Boolean.FALSE.toString() : value.toString());
    }

    public void addPipelineAttribute(Set<PipelineAttribute> pipelineAttributes, Pipeline p, List<AuditTrace> auditList, String name, String value) {

        if (name == null || name.trim().isEmpty()) {
            new Exception("name == null").printStackTrace();
            return;
        }

        if (value == null || value.trim().isEmpty()) {
            return;
        }

        PipelineAttribute pAtt = new PipelineAttribute();
        pAtt.setPipeline(p);
        pAtt.setName(name);
        pAtt.setValue(value);
        pipelineAttributes.add(pAtt);

        auditList.add(auditService.createAuditTracePipelineAttribute(name, "", value, p));
        LOG.debug("matching PipelineAttributes : new attribute with name '{}' and value  '{}' added", name, value);

    }

    public boolean isPipelineRestrictionsResolved(Pipeline p,
                                                  Pkcs10RequestHolder p10ReqHolder,
                                                  NamedValues[] nvARArr,
                                                  List<String> messageList) {

        // null pipeline means internal requests without an associated pipeline and no restrictions
        if (p == null) {
            return true;
        }

        boolean cnSanMatch = isCnAndSanApplicable( p, p10ReqHolder, messageList);

        if (!isPipelineAdditionalRestrictionsResolved(initAraRestrictions(p), nvARArr, messageList)) {
            return false;
        }

        boolean prSolved = isPipelineRestrictionsResolved(p, p10ReqHolder, messageList);

        return cnSanMatch && prSolved;
    }

    public boolean isPipelineRestrictionsResolved(Pipeline p, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

        if (p == null) {
            return true;
        }

        PipelineView pv = new PipelineView();
        initRdnRestrictions(pv, p);

        return isPipelineRestrictionsResolved(pv, p10ReqHolder, messageList);
    }

    public boolean isPipelineAdditionalRestrictionsResolved(ARARestriction[] araRestrictions, NamedValues[] nvARArr, List<String> messageList) {

        boolean outcome = true;

        for( ARARestriction restriction : araRestrictions) {

            boolean valuePresent = false;
            Optional<NamedValues> optionalNamedValues = findNVSByName(nvARArr, restriction.getName());
            if(optionalNamedValues.isPresent()){
                NamedValues nvs = optionalNamedValues.get();
                if(restriction.isRegExMatch()) {
                    Pattern pattern = Pattern.compile(restriction.getRegEx());
                    for (TypedValue typedValue : nvs.getValues()) {
                        if (!pattern.matcher(typedValue.getValue()).matches()) {
                            String msg = " value '" + typedValue.getValue() + "'attribute '" + nvs.getName() + "' does not match expression";
                            messageList.add(msg);
                            LOG.debug(msg);
                            outcome = false;
                        }
                    }
                }
                for (TypedValue typedValue : nvs.getValues()) {
                    if( typedValue.getValue() != null && !typedValue.getValue().isEmpty()){
                        valuePresent = true;
                        break;
                    }
                }
            }
            if (restriction.isRequired() && !valuePresent) {
                String msg = "required attribute : '" + restriction.getName() + "' missing / has no value";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        }


        if(nvARArr != null) {
            for (NamedValues nvAR : nvARArr) {
                for (ARARestriction araRestriction : araRestrictions) {
                    if (araRestriction.getName().equals(nvAR.getName())) {
                        if (!checkAdditionalRestrictions(araRestriction, nvAR, messageList)) {
                            outcome = false;
                        }
                    }
                }
            }
        }
        for (ARARestriction araRestriction : araRestrictions) {
            if (araRestriction.isRequired()) {
                if (Arrays.stream(nvARArr).noneMatch(nv -> (araRestriction.getName().equals(nv.getName())))) {
                    String msg = "additional restriction mismatch: An value for '" + araRestriction.getName() + "' MUST be present!";
                    messageList.add(msg);
                    LOG.debug(msg);
                    outcome = false;
                }
            }
        }
        return outcome;
    }

    private Optional<NamedValues> findNVSByName(NamedValues[] araArr, final String name) {
        return Arrays.stream(araArr).filter(nvs -> name.equals(nvs.getName())).findFirst();
    }

    public boolean isPipelineRestrictionsResolved(PipelineView pv, Pkcs10RequestHolder p10ReqHolder, List<String> messageList) {

        boolean outcome = algorithmRestrictionUtil.isAlgorithmRestrictionsResolved(p10ReqHolder, messageList);

        Set<GeneralName> gNameSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());
        LOG.debug("#" + gNameSet.size() + " SANs present");

        RDN[] rdnArr = p10ReqHolder.getSubjectRDNs();

        if (!checkRestrictions(BCStyle.C, pv.getRestriction_C(), rdnArr, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.CN, pv.getRestriction_CN(), rdnArr, gNameSet, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.O, pv.getRestriction_O(), rdnArr, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.OU, pv.getRestriction_OU(), rdnArr, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.L, pv.getRestriction_L(), rdnArr, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.ST, pv.getRestriction_S(), rdnArr, messageList)) {
            outcome = false;
        }
        if (!checkRestrictions(BCStyle.E, pv.getRestriction_E(), rdnArr, messageList)) {
            outcome = false;
        }

        if (!checkRestrictions(pv.getRestriction_SAN(), gNameSet, messageList)) {
            outcome = false;
        }

        LOG.debug("pv.isIpAsSubjectAllowed() is {} ", pv.isIpAsSubjectAllowed());
        if (!pv.isIpAsSubjectAllowed()) {
            if (isSubjectIP(rdnArr, messageList)) {
                String msg = "IP not allowed as subject";
                messageList.add(0, msg);
                LOG.info(msg);
                outcome = false;
            }
        }

        LOG.debug("pv.isIpAsSANAllowed() is {} ", pv.isIpAsSANAllowed());
        if (!pv.isIpAsSANAllowed()) {
            if (hasIPinSANList(gNameSet, messageList)) {
                String msg = "IP not allowed as SAN";
                messageList.add(0, msg);
                LOG.info(msg);
                outcome = false;
            }
        }

        return outcome;
    }


    private boolean hasIPinSANList(Set<GeneralName> gNameSet, List<String> messageList) {

        boolean outcome = false;

        for (GeneralName gn : gNameSet) {
            if (GeneralName.iPAddress == gn.getTagNo()) {
                String sanValue = CertificateUtil.getTypedSAN(gn);
                messageList.add("SAN '" + sanValue + "' is an IP address, not allowed.");
                outcome = true;
            }
        }
        return outcome;
    }

    private boolean checkRestrictions(RDNRestriction restriction, Set<GeneralName> gNameSet, List<String> messageList) {

        if (restriction == null) {
            return true; // no restrictions present!!
        }

        boolean outcome = true;

        String regEx = "";
        LOG.debug("checking SANs");

        boolean hasRegEx = false;
        if (restriction.getRegEx() != null) {
            regEx = restriction.getRegEx().trim();
            hasRegEx = !regEx.isEmpty();
        }

        int n = 0;

        for (GeneralName gn : gNameSet) {
            n++;
            if (hasRegEx) {
                String value = CertificateUtil.getTypedSAN(gn);

                if (restriction.isRegExMatch()) {
                    boolean evalResult = false;
                    try {
                        evalResult = value.matches(regEx);
                    } catch (PatternSyntaxException pse) {
                        LOG.warn("pattern '" + regEx + "' is not valid");
                    }
                    if (!evalResult) {
                        String msg = "restriction mismatch: SAN '" + value + "' does not match regular expression '" + regEx + "' !";
                        messageList.add(msg);
                        LOG.debug(msg);
                        outcome = false;
                    }
                } else {
                    if (!regEx.equalsIgnoreCase(value)) {
                        String msg = "restriction mismatch: SAN '" + value + "' does not match expected value '" + regEx + "' !";
                        messageList.add(msg);
                        LOG.debug(msg);
                        outcome = false;
                    }
                }
            }

        }

        RDNCardinalityRestriction cardinality = restriction.getCardinalityRestriction();
        if (RDNCardinalityRestriction.NOT_ALLOWED.equals(cardinality)) {
            if (n > 0) {
                String msg = "restriction mismatch: A SAN MUST NOT occur!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ONE.equals(cardinality)) {
            if (n == 0) {
                String msg = "restriction mismatch: SAN MUST occur once, missing here!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
            if (n != 1) {
                String msg = "restriction mismatch: SAN MUST occur exactly once, found " + n + " times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
            if (n == 0) {
                String msg = "restriction mismatch: SAns MUST occur once or more, missing here!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
            if (n > 1) {
                String msg = "restriction mismatch: SANs MUST occur zero or once, found " + n + " times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        }
        return outcome;
    }

    private boolean checkAdditionalRestrictions(ARARestriction araRestriction, NamedValues nvAR, List<String> messageList) {

        if (araRestriction == null) {
            return true; // no restrictions present!!
        }

        boolean outcome = true;

        LOG.debug("checking AdditionalRestrictions");
        String regEx;
        boolean hasRegEx = false;
        if (araRestriction.getRegEx() != null) {
            regEx = araRestriction.getRegEx().trim();
            hasRegEx = !regEx.isEmpty();
        }

        if (araRestriction.isRequired()) {
            if (nvAR.getValues().length == 0) {
                String msg = "additional restriction mismatch: An value for '" + nvAR.getName() + "' MUST be present!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            } else {
                for (TypedValue typedValue : nvAR.getValues()) {
                    if (typedValue.getValue().isEmpty()) {
                        String msg = "additional restriction mismatch: An value for '" + nvAR.getName() + "' MUST be present!";
                        messageList.add(msg);
                        LOG.debug(msg);
                        outcome = false;
                    }
                    if (hasRegEx) {
                        if (!checkRegEx(araRestriction, typedValue.getValue(), messageList)) {
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


        if (restriction == null) {
            return true; // no restrictions present!!
        }

        boolean outcome = true;

        String template;
        String restrictedName = OidNameMapper.lookupOid(restricted.toString());
        LOG.debug("checking element '{}'", restrictedName);

        boolean hasTemplate = false;
        if (restriction.getContentTemplate() != null) {
            template = restriction.getContentTemplate().trim();
            hasTemplate = !template.isEmpty();
        }

        String regEx = "";
        boolean hasRegEx = false;
        if (restriction.getRegEx() != null) {
            regEx = restriction.getRegEx().trim();
            hasRegEx = !regEx.isEmpty();
        }

        int n = 0;

        for (RDN rdn : rdnArr) {
            AttributeTypeAndValue atv = rdn.getFirst();
            if (restricted.equals(atv.getType())) {
                n++;
                String value = atv.getValue().toString().trim();
                if (hasRegEx && restriction.isRegExMatch()) {
                    boolean evalResult = false;
                    try {
                        evalResult = value.matches(regEx);
                    } catch (PatternSyntaxException pse) {
                        LOG.warn("pattern '" + regEx + "' is not valid");
                    }
                    if (!evalResult) {
                        String msg = "restriction mismatch: '" + value + "' does not match regular expression '" + regEx + "' !";
                        messageList.add(msg);
                        LOG.debug(msg);
                        outcome = false;
                    }
                }
/*
                if( hasTemplate && !template.equalsIgnoreCase(value) ) {
                    String msg = "restriction mismatch: '"+value +"' does not match expected value '"+template+"' !";
                    messageList.add(msg);
                    LOG.debug(msg);
                    outcome = false;
                }

 */
            }
        }

        RDNCardinalityRestriction cardinality = restriction.getCardinalityRestriction();
        if (RDNCardinalityRestriction.NOT_ALLOWED.equals(cardinality)) {
            if (n > 0) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST NOT occur!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ONE.equals(cardinality)) {
            if (n == 0) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST occur once, missing here!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
            if (n != 1) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST occur exactly once, found " + n + " times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ONE_OR_SAN.equals(cardinality)) {
            if (n == 0 && gNameSet.isEmpty()) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST occur once or a SAN entry MUST be present!!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
            if (n > 1) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST not occur more than once, found " + n + " times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ONE_OR_MANY.equals(cardinality)) {
            if (n == 0) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST occur once or more, missing here!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else if (RDNCardinalityRestriction.ZERO_OR_ONE.equals(cardinality)) {
            if (n > 1) {
                String msg = "restricition mismatch: '" + restrictedName + "' MUST occur zero or once, found " + n + " times!";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        }
        return outcome;
    }

    private boolean checkRegEx(ARARestriction araRestriction, String value, List<String> messageList) {

        boolean outcome = true;
        String regEx = araRestriction.getRegEx().trim();

        if (araRestriction.isRegExMatch()) {
            boolean evalResult = false;
            try {
                evalResult = value.matches(regEx);
            } catch (PatternSyntaxException pse) {
                LOG.warn("pattern '" + regEx + "' is not valid");
            }
            if (!evalResult) {
                String msg = "restriction mismatch: '" + value + "' does not match regular expression '" + regEx + "' !";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        } else {
            if (!regEx.equalsIgnoreCase(value)) {
                String msg = "restriction mismatch: '" + value + "' does not match expected value '" + regEx + "' !";
                messageList.add(msg);
                LOG.debug(msg);
                outcome = false;
            }
        }
        return outcome;
    }

    private boolean isSubjectIP(RDN[] rdnArr, List<String> messageList) {


        for (RDN rdn : rdnArr) {
            AttributeTypeAndValue atv = rdn.getFirst();
            if (BCStyle.CN.equals(atv.getType())) {

                String value = atv.getValue().toString().trim();
                InetAddressValidator inv = InetAddressValidator.getInstance();
                if (inv.isValidInet4Address(value)) {
                    messageList.add("CommonName '" + value + "' is a valid IP4 address");
                    return true;
                }
                if (inv.isValidInet6Address(value)) {
                    messageList.add("CommonName '" + value + "' is a valid IP6 address");
                    return true;
                }
            }

        }
        return false;
    }

    public void setPipelineAttribute(Pipeline pipeline, String name, String value) {

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (name.equals(plAtt.getName())) {
                if (!plAtt.getValue().equals(value)) {
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

    public int getPipelineAttribute(Pipeline pipeline, String name, int defaultValue) {

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (name.equals(plAtt.getName())) {
                try {
                    return Integer.parseInt(plAtt.getValue());
                } catch (NumberFormatException nfe) {
                    LOG.warn("unexpected value for attribute '" + name + "'", nfe);
                }
            }
        }
        return defaultValue;
    }

    public Boolean getPipelineAttribute(Pipeline pipeline, String name, boolean defaultValue) {

        for (PipelineAttribute plAtt : pipeline.getPipelineAttributes()) {
            if (name.equals(plAtt.getName())) {
                return Boolean.parseBoolean(plAtt.getValue());
            }
        }
        return defaultValue;
    }

    public List<String> getTypedARAttributeNames(Pipeline pipeline, ARAContentType araContentType) {

        List<String> nameList = new ArrayList<>();
        ARARestriction[] araRestrictions = initAraRestrictions(pipeline);
        for(ARARestriction araRestriction: araRestrictions){
            if(araContentType.equals(araRestriction.getContentType())) {
                nameList.add(araRestriction.getName());
            }
        }
        return nameList;
    }

    public Certificate getSCEPRecipientCertificate(Pipeline pipeline, CertificateProcessingUtil cpUtil) throws IOException, GeneralSecurityException {

        LOG.debug("getSCEPRecipientCertificate() ...");

        if (pipeline == null) {
            throw new GeneralSecurityException("pipeline argument == null!");
        }

        Certificate currentRecepientCert = certUtil.getCurrentSCEPRecipient(pipeline);
        if (currentRecepientCert != null) {
            LOG.debug("found active certificate as scep recipient with id {}", currentRecepientCert.getId());
            return currentRecepientCert;
        }

        if (Boolean.TRUE.equals(pipeline.isActive())) {

            Certificate recipientCert = createSCEPRecipientCertificate(pipeline, cpUtil);
            if (recipientCert == null) {
                LOG.info("creation of scep recipient certificate for pipeline {} failed", pipeline.getId());
            } else {
                LOG.debug("new scep recipient certificate {} created for pipeline {}", recipientCert.getId(), pipeline.getId());
            }
            return recipientCert;
        } else {
            LOG.debug("pipeline {} NOT active, no recipient certificate created", pipeline.getId());
            return null;
        }
    }

    private Certificate createSCEPRecipientCertificate(final Pipeline pipeline, CertificateProcessingUtil cpUtil) throws IOException, GeneralSecurityException {

        String scepRecipientDN = getPipelineAttribute(pipeline, SCEP_RECIPIENT_DN, "CN=SCEPRecepient_" + pipeline.getId());
        LOG.debug("createSCEPRecipientCertificate() with scepRecipientDN '{}'", scepRecipientDN);
        X500Principal subject = new X500Principal(scepRecipientDN);

        CAConnectorConfig caConfig;
        String caConnectorName = getPipelineAttribute(pipeline, SCEP_CA_CONNECTOR_RECIPIENT_NAME, "");
        List<CAConnectorConfig> caConfigList = caConnRepository.findByName(caConnectorName);
        if (caConfigList.isEmpty()) {
            LOG.warn("creation of SCEP recipient certificate failed, connector {} missing, using default!", caConnectorName);
            caConfig = configUtil.getDefaultConfig();
        } else {
            caConfig = caConfigList.get(0);
        }

        String scepRecipientKeyLength = getPipelineAttribute(pipeline, SCEP_RECIPIENT_KEY_TYPE_LEN, defaultKeySpec);
        KeyAlgoLengthOrSpec kal = KeyAlgoLengthOrSpec.from(scepRecipientKeyLength);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(kal.getAlgoName());
        keyPairGenerator.initialize(kal.getKeyLength(), randomUtil.getSecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        String p10ReqPem = CryptoUtil.getCsrAsPEM(subject,
            keyPair.getPublic(),
            keyPair.getPrivate(),
            null
        );

        String requestorName = Constants.SYSTEM_ACCOUNT;
        CSR csr = cpUtil.buildCSR(p10ReqPem, requestorName, AuditService.AUDIT_SCEP_CERTIFICATE_REQUESTED, "", pipeline);
        csrRepository.save(csr);


        Certificate cert = cpUtil.processCertificateRequest(csr, requestorName, AuditService.AUDIT_SCEP_CERTIFICATE_CREATED, caConfig);
        if (cert == null) {
            LOG.warn("creation of SCEP recipient certificate with DN '{}' failed ", scepRecipientDN);
        } else {
            LOG.debug("new certificate id '{}' for SCEP recipient", cert.getId());

            certUtil.storePrivateKey(cert, keyPair, cert.getValidTo());
            certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_SCEP_RECIPIENT, "" + pipeline.getId());

            certRepository.save(cert);
        }

        return cert;
    }

    public Pipeline getPipelineByRealm(final PipelineType pipelineType, final String realm) {
        List<Pipeline> pipelineList = pipelineRepository.findActiveByTypeUrl(pipelineType, realm);
        if (pipelineList.isEmpty()) {
            LOG.info("no matching pipeline for type '{}' request realm {}", pipelineType, realm);
            return null;
        } else {
            for (Pipeline pipeline : pipelineList) {
                LOG.info("matching pipeline for type '{}' and request realm {} found: {}", pipeline.getType(), pipeline.getUrlPart(), pipeline.getName());
            }
        }
        return pipelineList.get(0);
    }

    public boolean isUserValidAsRA(final Pipeline pipeline, User user) {

        if (user.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.RA_OFFICER.equals(a.getName()))) {
            LOG.debug("user '{}' has role 'RA_OFFICER'", user.getLogin());
            return true;
        }

        if (user.getAuthorities().stream().anyMatch(a -> AuthoritiesConstants.DOMAIN_RA_OFFICER.equals(a.getName()))) {
            LOG.debug("user '{}' has role 'DOMAIN_RA_OFFICER'", user.getLogin());
            for (PipelineAttribute pipelineAttribute : pipeline.getPipelineAttributes()) {
                if (DOMAIN_RA_OFFICER.equals(pipelineAttribute.getName()) &&
                    Long.parseLong(pipelineAttribute.getValue()) == user.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isCnAndSanApplicable(Pipeline pipeline,
                                         Pkcs10RequestHolder p10ReqHolder,
                                         List<String> messageList) {
        CnAsSanRestriction cnAsSanRestriction = CnAsSanRestriction.valueOf(
            getPipelineAttribute(pipeline, CN_AS_SAN_RESTRICTION, CN_AS_SAN_IGNORE.toString()));

        if( CN_AS_SAN_IGNORE.equals(cnAsSanRestriction)) {
            LOG.debug("CN / SAN matching not requested.");
            return true;
        }

        boolean cnInSan = cSRUtil.isCNinSANSet(p10ReqHolder);

        if(  CN_AS_SAN_REQUIRED.equals(cnAsSanRestriction)) {
            if( !cnInSan){
                LOG.debug("CN / SAN mismatch: CN must be present in SANs.");
                messageList.add("CN / SAN mismatch: CN must be present in SANs.");
                return false;
            }
            LOG.debug("CN / SAN matching successful.");
            return true;
        } else { // CN_AS_SAN_MUST_NOT_MATCH
            if( cnInSan){
                LOG.debug("CN / SAN matching successful.");
            }else {
                LOG.debug("CN / SAN mismatch: CN recommended be present in SANs.");
                messageList.add("CN / SAN mismatch: CN recommended be present in SANs.");
            }
            return true;
        }
    }


    public boolean isPublicKeyApplicable(Pipeline pipeline, Pkcs10RequestHolder p10ReqHolder,
                                         List<String> messageList) {
        KeyUniqueness keyUniqueness = KeyUniqueness.valueOf(
            getPipelineAttribute(pipeline, KEY_UNIQUENESS, KeyUniqueness.KEY_UNIQUE.toString()));
        return isPublicKeyApplicable(keyUniqueness, p10ReqHolder, null, messageList);
    }

    public boolean isPublicKeyApplicable(Pipeline pipeline,
                                         Pkcs10RequestHolder p10ReqHolder,
                                         AcmeOrder acmeOrder,
                                         List<String> messageList) {
        KeyUniqueness keyUniqueness = KeyUniqueness.valueOf(
            getPipelineAttribute(pipeline, KEY_UNIQUENESS, KeyUniqueness.KEY_UNIQUE.toString()));
        return isPublicKeyApplicable(keyUniqueness, p10ReqHolder, acmeOrder, messageList);
    }

    public boolean isPublicKeyApplicable(KeyUniqueness keyUniqueness,
                                         Pkcs10RequestHolder p10ReqHolder,
                                         List<String> messageList) {
        return isPublicKeyApplicable( keyUniqueness, p10ReqHolder,null, messageList);
    }

    public boolean isPublicKeyApplicable(KeyUniqueness keyUniqueness,
                                         Pkcs10RequestHolder p10ReqHolder,
                                         AcmeOrder acmeOrder,
                                         List<String> messageList) {

        Page<CSR> csrPage = csrRepository.findNonRejectedByPublicKeyHash(
            PageRequest.of(0, 10),
            p10ReqHolder.getPublicKeyHash());

        if( csrPage.isEmpty()){
            LOG.debug("public key with hash '{}' has not been used.", p10ReqHolder.getPublicKeyHash());
            return true;
        }

        LOG.debug("public key with hash '{}' present in DB.", p10ReqHolder.getPublicKeyHash());
        switch (keyUniqueness) {
            case KEY_REUSE:
                LOG.info("public key with hash '{}' was already used, reusable by policy.", p10ReqHolder.getPublicKeyHash());
                return true;
            case KEY_UNIQUE_WARN_ONLY:
                if( acmeOrder == null){
                    LOG.warn("mode 'KEY_REUSE_WARN_ONLY' only applicable for ACME requests.");
                    return false;
                }
                LOG.info("public key with hash '{}' was already used, sending warning to account holder.", p10ReqHolder.getPublicKeyHash());
                notificationService.notifyAccountHolderOnKeyReuse(acmeOrder);
                return true;
            case KEY_UNIQUE:
                messageList.add("Public key already used. Create a new key pair.");
                return false;
            case DOMAIN_REUSE_WARN_ONLY:
                if( acmeOrder == null){
                    LOG.warn("mode 'DOMAIN_REUSE_WARN_ONLY' only applicable for ACME requests.");
                    return false;
                }
                if( usedInDomainOnly(csrPage, p10ReqHolder, messageList)){
                    LOG.info("public key with hash '{}' matches domain-only scope.", p10ReqHolder.getPublicKeyHash());
                }else {
                    LOG.info("public key with hash '{}' does not match domain-only scope, sending warning to account holder.", p10ReqHolder.getPublicKeyHash());
                    notificationService.notifyAccountHolderOnKeyReuse(acmeOrder);
                }
                return true;
            default:
                if( usedInDomainOnly(csrPage, p10ReqHolder, messageList)){
                    LOG.info("public key with hash '{}' matches domain-only scope.", p10ReqHolder.getPublicKeyHash());
                    return true;
                }
                LOG.info("public key with hash '{}' does not match domain-only scope.", p10ReqHolder.getPublicKeyHash());
                return false;
        }
    }

    boolean usedInDomainOnly(Page<CSR> csrPage,
                             Pkcs10RequestHolder p10ReqHolder,
                             List<String> messageList) {

        Set<GeneralName> gNameSet = CSRUtil.getSANList(p10ReqHolder.getReqAttributes());

        RDN[] rdnArr = p10ReqHolder.getSubjectRDNs();
        for( RDN rdn: rdnArr){
            if( rdn.getFirst() != null && BCStyle.CN.equals( rdn.getFirst().getType())){
                String cn = rdn.getFirst().getValue().toString();
                if( CertificateUtil.isIPAddress(cn)) {
                    gNameSet.add( new GeneralName(GeneralName.iPAddress, cn.toLowerCase(Locale.ROOT)));
                }else{
                    gNameSet.add( new GeneralName(GeneralName.dNSName, cn.toLowerCase(Locale.ROOT)));
                }
            }
        }

        CSR csrToCheck = csrPage.getContent().get(0);
        Set<GeneralName> csrTypedSANSet =
            csrToCheck.getCsrAttributes().stream()
            .filter(csrAtt -> isTypedSAN(csrAtt))
            .map(csrAtt -> buildGeneralName(csrAtt))
            .collect(Collectors.toSet());

        if(LOG.isDebugEnabled()) {
            for (GeneralName gname : gNameSet) {
                LOG.debug("gNameSet from request contains {}", gname);
            }
            for (GeneralName gname : csrTypedSANSet) {
                LOG.debug("gNameSet from last CSR contains {}", gname);
            }
        }

        boolean isIdentical = gNameSet.containsAll(csrTypedSANSet) && csrTypedSANSet.containsAll(gNameSet);
        if(!isIdentical){
            if( !gNameSet.containsAll(csrTypedSANSet)) {
                messageList.add("The CSR requests more names than previous certificate while reusing its key.");
            }
            if( !csrTypedSANSet.containsAll(gNameSet)) {
                messageList.add("The CSR requests less names than previous certificate while reusing its key.");
            }
        }
        return isIdentical;
    }

    boolean isTypedSAN(CsrAttribute csrAttribute){
        return ATTRIBUTE_TYPED_SAN.equals(csrAttribute.getName());
    }

    GeneralName buildGeneralName(CsrAttribute csrAttribute){
        return CertificateUtil.getGeneralNameFromTypedSAN(csrAttribute.getValue());
    }

    public void sendProblemNotificationPerEmail(Pipeline pipeline,
                                                AcmeOrder acmeOrder,
                                                ProblemDetail acmeProblem) {

        AcmeAccount acmeAccount = acmeOrder.getAccount();
        if( acmeAccount != null && acmeAccount.getContacts() != null ){
            if( getPipelineAttribute(pipeline, PipelineUtil.ACME_NOTIFY_ACCOUNT_CONTACT_ON_ERROR,false)){
                notificationService.notifyAccountHolderOnACMEProblem(acmeOrder, acmeProblem);
            }
        }
    }


    public boolean checkAcceptNetwork(Pipeline pipeline) {

        String[] acceptedNetworks = splitNetworks(getPipelineAttribute(pipeline, PipelineUtil.NETWORK_ACCEPT,""));
        String[] rejectNetworks = splitNetworks(getPipelineAttribute(pipeline, PipelineUtil.NETWORK_REJECT,""));

        Collection<SubnetUtils.SubnetInfo> acceptedSubnets = new ArrayList<>();
        for (String subnetMask : acceptedNetworks) {
            acceptedSubnets.add(new SubnetUtils(subnetMask).getInfo());
        }

        Collection<SubnetUtils.SubnetInfo> rejectSubnets = new ArrayList<>();
        for (String subnetMask : rejectNetworks) {
            rejectSubnets.add(new SubnetUtils(subnetMask).getInfo());
        }

        String ipAddress = requestUtil.getClientIP();
        for (SubnetUtils.SubnetInfo subnet : rejectSubnets) {
            if (subnet.isInRange(ipAddress)) {
                LOG.warn("IP Address {} is in rejection range {}", ipAddress,subnet.getCidrSignature());
                return false;
            }else{
                LOG.debug("IP Address {} is NOT in rejection range {}", ipAddress,subnet.getCidrSignature());
            }
        }

        for (SubnetUtils.SubnetInfo subnet : acceptedSubnets) {
            if (subnet.isInRange(ipAddress)) {
                LOG.warn("IP Address {} is in acceptance range {}", ipAddress, subnet.getCidrSignature());
                return true;
            }else{
                LOG.debug("IP Address {} is NOT in acceptance range {}", ipAddress,subnet.getCidrSignature());
            }
        }

        if( acceptedSubnets.isEmpty()) {
            LOG.info("IP Address {} acceptance because there is no acceptance range defined", ipAddress);
            return true;
        }

        LOG.info("IP Address {} matches no range", ipAddress);
        return false;
    }
}

