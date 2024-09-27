package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CaConnectorConfigView;
import de.trustable.ca3s.core.service.dto.NamedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class CaConnectorConfigUtil {

    public static final String ATT_ISSUER_NAME = "X500_NAME";
    public static final String ATT_MULTIPLE_MESSAGES = "MULTIPLE_MESSAGES";
    public static final String ATT_IMPLICIT_CONFIRM = "IMPLICIT_CONFIRM";
    public static final String ATT_IGNORE_RESPONSE_MESSAGE_VERIFICATION = "IGNORE_RESPONSE_MESSAGE_VERIFICATION";
    public static final String ATT_ATTRIBUTE_TYPE_AND_VALUE = "ATTRIBUTE_TYPE_AND_VALUE";
    public static final String ATT_CMP_MESSAGE_CONTENT_TYPE = "CMP_MESSAGE_CONTENT_TYPE";
    public static final String ATT_SNI = "SNI";
    public static final String ATT_DISABLE_HOST_NAME_VERIFIER = "DISABLE_HOST_NAME_VERIFIER";
    public static final String ATT_FILL_EMPTY_SUBJECT_WITH_SAN = "FILL_EMPTY_SUBJECT_WITH_SAN";
    public static final String PLAIN_SECRET_PLACEHOLDER = "*****";

    Logger LOG = LoggerFactory.getLogger(CaConnectorConfigUtil.class);

    private final CAConnectorConfigRepository cAConnectorConfigRepository;
    final private ProtectedContentRepository protectedContentRepository;
    final private ProtectedContentUtil protectedContentUtil;
    final private CertificateRepository certificateRepository;
    private final CAConnectorConfigAttributeRepository caConnectorConfigAttributeRepository;
    final private AuditService auditService;
    final private AuditTraceRepository auditTraceRepository;

    public CaConnectorConfigUtil(CAConnectorConfigRepository cAConnectorConfigRepository, ProtectedContentRepository protectedContentRepository, ProtectedContentUtil protectedContentUtil, CertificateRepository certificateRepository, CAConnectorConfigAttributeRepository caConnectorConfigAttributeRepository, AuditService auditService, AuditTraceRepository auditTraceRepository) {
        this.cAConnectorConfigRepository = cAConnectorConfigRepository;
        this.protectedContentRepository = protectedContentRepository;
        this.protectedContentUtil = protectedContentUtil;
        this.certificateRepository = certificateRepository;
        this.caConnectorConfigAttributeRepository = caConnectorConfigAttributeRepository;
        this.auditService = auditService;
        this.auditTraceRepository = auditTraceRepository;
    }

    public CaConnectorConfigView from(CAConnectorConfig cfg) {

        CaConnectorConfigView cv = new CaConnectorConfigView();

        cv.setId(cfg.getId());
        cv.setName(cfg.getName());
        cv.setCaConnectorType(cfg.getCaConnectorType());
        cv.setActive(cfg.getActive());
        cv.setCaUrl(cfg.getCaUrl());
        cv.setInterval(cfg.getInterval());
        cv.setDefaultCA(cfg.getDefaultCA());
        cv.setPlainSecret(PLAIN_SECRET_PLACEHOLDER);
        cv.setActive(cfg.getActive());
        cv.setPollingOffset(cfg.getPollingOffset());
        cv.setLastUpdate(cfg.getLastUpdate());
        cv.setSelector(cfg.getSelector());
        cv.setTrustSelfsignedCertificates(cfg.getTrustSelfsignedCertificates());

        cv.setMessageProtectionId(null);
        cv.setMessageProtectionPassphrase(false);

        if( cfg.getMessageProtection() == null) {
            cv.setMessageProtectionPassphrase(true);
        }else {
            cv.setMessageProtectionId(cfg.getMessageProtection().getId());
        }

        if(cfg.getTlsAuthentication() != null) {
            cv.setTlsAuthenticationId(cfg.getTlsAuthentication().getId());
        }else{
            cv.setTlsAuthenticationId(null);
        }

        List<NamedValue> aTaVList = new ArrayList<>();

        // backward compliant defaults
        cv.setMultipleMessages(false);
        cv.setImplicitConfirm(true);

        for( CAConnectorConfigAttribute cfgAtt: cfg.getCaConnectorAttributes()) {

            if (ATT_ISSUER_NAME.equals(cfgAtt.getName())) {
                cv.setIssuerName(cfgAtt.getValue());
            }else if (ATT_MULTIPLE_MESSAGES.equals(cfgAtt.getName())) {
                cv.setMultipleMessages( Boolean.parseBoolean(cfgAtt.getValue()));
            }else if (ATT_IMPLICIT_CONFIRM.equals(cfgAtt.getName())) {
                cv.setImplicitConfirm( Boolean.parseBoolean(cfgAtt.getValue()));
            }else if (ATT_CMP_MESSAGE_CONTENT_TYPE.equals(cfgAtt.getName())) {
                cv.setMsgContentType(cfgAtt.getValue());
            }else if (ATT_SNI.equals(cfgAtt.getName())) {
                cv.setSni(cfgAtt.getValue());
            }else if (ATT_DISABLE_HOST_NAME_VERIFIER.equals(cfgAtt.getName())) {
                cv.setDisableHostNameVerifier( Boolean.parseBoolean(cfgAtt.getValue()));
            }else if (ATT_IGNORE_RESPONSE_MESSAGE_VERIFICATION.equals(cfgAtt.getName())) {
                cv.setIgnoreResponseMessageVerification( Boolean.parseBoolean(cfgAtt.getValue()));
            }else if (ATT_FILL_EMPTY_SUBJECT_WITH_SAN.equals(cfgAtt.getName())) {
                cv.setFillEmptySubjectWithSAN( Boolean.parseBoolean(cfgAtt.getValue()));
            }else if (ATT_ATTRIBUTE_TYPE_AND_VALUE.equals(cfgAtt.getName())) {
                aTaVList.add( new NamedValue(cfgAtt.getValue()));
            }

        }


        cv.setaTaVArr(aTaVList.toArray(new NamedValue[0]));
        return cv;
    }

    @Transactional
    public CAConnectorConfig to(CaConnectorConfigView cv) {

        List<AuditTrace> auditList = new ArrayList<>();
        CAConnectorConfig caConnectorConfig;
        List<CAConnectorConfig> caConnConfList = cAConnectorConfigRepository.findByName(cv.getName());
        if( cv.getId() != null) {
            Optional<CAConnectorConfig> optP = cAConnectorConfigRepository.findById(cv.getId());
            if(optP.isPresent()) {
                caConnectorConfig = optP.get();
                if(!caConnConfList.isEmpty() && !caConnConfList.get(0).getId().equals(caConnectorConfig.getId())){
                    throw new BadRequestAlertException("Name '" + cv.getName() + "' already assigned", "CAConnectorConfig", "name already used");
                }
//                caConnectorConfigAttributeRepository.deleteAll(caConnectorConfig.getCaConnectorAttributes());
            }else {
                if(!caConnConfList.isEmpty()){
                    throw new BadRequestAlertException("Name '" + cv.getName() + "' already assigned", "CAConnectorConfig", "name already used");
                }
                caConnectorConfig = new CAConnectorConfig();
                cAConnectorConfigRepository.save(caConnectorConfig);
                auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_PIPELINE_CREATED, caConnectorConfig));
            }
        }else {
            if(!caConnConfList.isEmpty()){
                throw new BadRequestAlertException("Name '" + cv.getName() + "' already assigned", "CAConnectorConfig", "name already used");
            }
            caConnectorConfig = new CAConnectorConfig();
            caConnectorConfig.setName(cv.getName());
            caConnectorConfig.setCaConnectorType(cv.getCaConnectorType());
            caConnectorConfig.setActive(cv.getActive());
            caConnectorConfig.setCheckActive(false);
            caConnectorConfig.setCaUrl(cv.getCaUrl());
            caConnectorConfig.setInterval(cv.getInterval());
            caConnectorConfig.setDefaultCA(cv.getDefaultCA());
            caConnectorConfig.setActive(cv.getActive());
            caConnectorConfig.setPollingOffset(cv.getPollingOffset());
            caConnectorConfig.setLastUpdate(cv.getLastUpdate());
            caConnectorConfig.setSelector(cv.getSelector());
            caConnectorConfig.setTrustSelfsignedCertificates(cv.getTrustSelfsignedCertificates());

//            caConnectorConfig.setPlainSecret(cv.getPlainSecret());
            caConnectorConfig.setPlainSecret(PLAIN_SECRET_PLACEHOLDER);

            cAConnectorConfigRepository.save(caConnectorConfig);
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_COPIED, caConnectorConfig));
        }

        if(!Objects.equals(cv.getName(), caConnectorConfig.getName())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_NAME_CHANGED, caConnectorConfig.getName(), cv.getName(), caConnectorConfig));
            caConnectorConfig.setName(cv.getName());
        }

        if(!Objects.equals(cv.getCaConnectorType(), caConnectorConfig.getCaConnectorType())) {
            String oldType = "";
            if( caConnectorConfig.getCaConnectorType() != null){
                oldType = caConnectorConfig.getCaConnectorType().toString();
            }
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_TYPE_CHANGED, oldType, cv.getCaConnectorType().toString(), caConnectorConfig));
            caConnectorConfig.setCaConnectorType(cv.getCaConnectorType());
        }

        if(cv.getActive() != caConnectorConfig.getActive()){
            String isActive = "";
            if( caConnectorConfig.getActive() != null){
                isActive = caConnectorConfig.getActive().toString();
            }
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_ACTIVE_CHANGED, isActive, cv.getActive().toString(), caConnectorConfig));
            caConnectorConfig.setActive(cv.getActive());
        }

        if(!Objects.equals(cv.getCaUrl(), caConnectorConfig.getCaUrl())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_CA_URL_CHANGED, caConnectorConfig.getCaUrl(), cv.getCaUrl(), caConnectorConfig));
            caConnectorConfig.setCaUrl(cv.getCaUrl());
        }

        if(!Objects.equals(cv.getInterval(), caConnectorConfig.getInterval())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_INTERVAL_CHANGED, String.valueOf(caConnectorConfig.getInterval()), String.valueOf(cv.getInterval()), caConnectorConfig));
            caConnectorConfig.setInterval(cv.getInterval());
        }

        if(cv.getDefaultCA() != caConnectorConfig.getDefaultCA()){
            String isDefaultCA = "";
            if( caConnectorConfig.getActive() != null){
                isDefaultCA = caConnectorConfig.getActive().toString();
            }
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_IS_DEFAULT_CHANGED, isDefaultCA, cv.getDefaultCA().toString(), caConnectorConfig));
            caConnectorConfig.setDefaultCA(cv.getDefaultCA());
        }

        if(!Objects.equals(cv.getPollingOffset(), caConnectorConfig.getPollingOffset())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_POLLING_OFFSET_CHANGED, String.valueOf(caConnectorConfig.getPollingOffset()), String.valueOf(cv.getPollingOffset()), caConnectorConfig));
            caConnectorConfig.setPollingOffset(cv.getPollingOffset());
        }

        if(!Objects.equals(cv.getLastUpdate(), caConnectorConfig.getLastUpdate())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_POLLING_OFFSET_CHANGED, caConnectorConfig.getLastUpdate().toString(), cv.getLastUpdate().toString(), caConnectorConfig));
            caConnectorConfig.setLastUpdate(cv.getLastUpdate());
        }

        if(!Objects.equals(cv.getSelector(), caConnectorConfig.getSelector())) {
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_SELECTOR_CHANGED, caConnectorConfig.getSelector(), cv.getSelector(), caConnectorConfig));
            caConnectorConfig.setSelector(cv.getSelector());
        }

        if(cv.getTrustSelfsignedCertificates() != caConnectorConfig.getTrustSelfsignedCertificates()){
            String trustSelfsignedCertificates = "";
            if( caConnectorConfig.getTrustSelfsignedCertificates() != null){
                trustSelfsignedCertificates = caConnectorConfig.getTrustSelfsignedCertificates().toString();
            }
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_TRUST_SELFSIGNED_CHANGED, trustSelfsignedCertificates, cv.getTrustSelfsignedCertificates().toString(), caConnectorConfig));
            caConnectorConfig.setTrustSelfsignedCertificates(cv.getTrustSelfsignedCertificates());
        }

        Certificate tlsAuthCertificate = caConnectorConfig.getTlsAuthentication();
        Long tlsAuthCertificateId = 0L;
        if( tlsAuthCertificate != null) {
            tlsAuthCertificateId = tlsAuthCertificate.getId();
        }
        if( cv.getTlsAuthenticationId() == null){

            if(tlsAuthCertificate != null) {
                auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_TLS_AUTHENTICATION_CHANGED, null, tlsAuthCertificateId.toString(), caConnectorConfig));
                caConnectorConfig.setTlsAuthentication(null);
            }
        }else{
            Optional<Certificate> optionalCertificate = certificateRepository.findById(cv.getTlsAuthenticationId());
            if(optionalCertificate.isPresent()){
                if(!Objects.equals(tlsAuthCertificateId, optionalCertificate.get().getId())){
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_TLS_AUTHENTICATION_CHANGED, tlsAuthCertificateId.toString(), optionalCertificate.get().getId().toString(), caConnectorConfig));
                    caConnectorConfig.setTlsAuthentication(optionalCertificate.get());
                }
            }
        }

        Certificate messageProtectionCertificate = caConnectorConfig.getMessageProtection();
        Long messageProtectionCertificateId = 0L;
        if( messageProtectionCertificate != null) {
            messageProtectionCertificateId = messageProtectionCertificate.getId();
        }

        if( cv.getMessageProtectionId() == null || cv.isMessageProtectionPassphrase()){
            if(messageProtectionCertificate != null) {
                auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MESSAGE_PROTECTION_CHANGED, null, messageProtectionCertificateId.toString(), caConnectorConfig));
                caConnectorConfig.setMessageProtection(null);
            }
        }else{
            Optional<Certificate> optionalCertificate = certificateRepository.findById(cv.getMessageProtectionId());
            if(optionalCertificate.isPresent()){
                if(!Objects.equals(messageProtectionCertificateId, optionalCertificate.get().getId())){
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MESSAGE_PROTECTION_CHANGED, messageProtectionCertificateId.toString(), optionalCertificate.get().getId().toString(), caConnectorConfig));
                    caConnectorConfig.setMessageProtection(optionalCertificate.get());
                }
            }
        }


        if( !PLAIN_SECRET_PLACEHOLDER.equals(cv.getPlainSecret())){
            if ((cv.getCaConnectorType().equals(CAConnectorType.CMP) && cv.isMessageProtectionPassphrase()) ||
                !cv.getCaConnectorType().equals(CAConnectorType.CMP)) {

                ProtectedContent pc;
                List<ProtectedContent> listPC = protectedContentRepository.findByTypeRelationId(ProtectedContentType.PASSWORD, ContentRelationType.CA_CONNECTOR_PW, caConnectorConfig.getId());
                if (listPC.isEmpty()) {

                    pc = new ProtectedContent();
                    pc.setType(ProtectedContentType.PASSWORD);
                    pc.setRelationType(ContentRelationType.CA_CONNECTOR_PW);
                    pc.setRelatedId(caConnectorConfig.getId());
                    pc.setCreatedOn(Instant.now());
                    pc.setLeftUsages(-1);
                    pc.setValidTo(ProtectedContentUtil.MAX_INSTANT);
                    pc.setDeleteAfter(ProtectedContentUtil.MAX_INSTANT);

                    LOG.debug("Protected Content created for ca connector password");
                } else {
                    pc = listPC.get(0);
                    LOG.debug("Protected Content found for ca connector password");
                }

                String oldContent = protectedContentUtil.unprotectString(pc.getContentBase64());
                if (oldContent == null ||
                    !oldContent.equals(cv.getPlainSecret()) ||
                    pc.getValidTo() == null ||
                    !pc.getValidTo().equals(cv.getSecretValidTo())) {

                    if (cv.getPlainSecret() == null) {
                        if (listPC.isEmpty()) {
                            LOG.debug("No CA Connector password defined");
                        } else {
                            LOG.debug("CA Connector password removed");
                            protectedContentRepository.delete(pc);
                            caConnectorConfig.setSecret(pc);
                            auditList.add(auditService.createAuditTraceCaConnectorConfig(AuditService.AUDIT_CA_CONNECTOR_SECRET_CHANGED, "#######", "", caConnectorConfig));
                        }
                    } else {

                        pc.setContentBase64(protectedContentUtil.protectString(cv.getPlainSecret()));
                        Instant secretValidTo = cv.getSecretValidTo();
                        if (secretValidTo == null) {
                            secretValidTo = Instant.now().plus(100 * 360, ChronoUnit.DAYS);
                        }
                        pc.setValidTo(secretValidTo);
                        pc.setDeleteAfter(secretValidTo.plus(1, ChronoUnit.DAYS));
                        protectedContentRepository.save(pc);
                        caConnectorConfig.setSecret(pc);
//                    LOG.debug("CA Connector password updated {} -> {}, {} -> {}", oldContent, cv.getPlainSecret(), secretValidTo, pc.getValidTo());
                        auditList.add(auditService.createAuditTraceCaConnectorConfig(AuditService.AUDIT_CA_CONNECTOR_SECRET_CHANGED, "#######", "******", caConnectorConfig));
                    }
                } else {
                    protectedContentRepository.delete(pc);
                    caConnectorConfig.setSecret(pc);
                    auditList.add(auditService.createAuditTraceCaConnectorConfig(AuditService.AUDIT_CA_CONNECTOR_SECRET_DELETED, "#######", "******", caConnectorConfig));
                }
            }
        }

        boolean hasIssuerName = false;
        boolean hasMultipleMessages = false;
        boolean hasImplicitConfirm = false;
        boolean hasMsgContentType = false;
        boolean hasSniType = false;
        boolean hasDisableHostNameVerifier = false;
        boolean hasIgnoreResponseMessageVerification = false;
        boolean hasFillEmptySubjectWithSAN = false;

        for( CAConnectorConfigAttribute configAttribute : caConnectorConfig.getCaConnectorAttributes()){

            if (ATT_ISSUER_NAME.equals(configAttribute.getName())) {
                if(!Objects.equals(cv.getIssuerName(), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_ISSUER_NAME_CHANGED, configAttribute.getValue(), cv.getIssuerName(), caConnectorConfig));
                    configAttribute.setValue(cv.getIssuerName());
                }
                hasIssuerName = true;
            }else if (ATT_MULTIPLE_MESSAGES.equals(configAttribute.getName())) {
                if(!Objects.equals( Boolean.toString(cv.isMultipleMessages()), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MULTIPLE_MESSAGES_CHANGED, configAttribute.getValue(), Boolean.toString(cv.isMultipleMessages()), caConnectorConfig));
                    configAttribute.setValue(Boolean.toString(cv.isMultipleMessages()));
                }
                hasMultipleMessages = true;
            }else if (ATT_DISABLE_HOST_NAME_VERIFIER.equals(configAttribute.getName())) {
                if(!Objects.equals( Boolean.toString(cv.isDisableHostNameVerifier()), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_DISABLE_HOST_NAME_VERIFIER_CHANGED, configAttribute.getValue(), Boolean.toString(cv.isDisableHostNameVerifier()), caConnectorConfig));
                    configAttribute.setValue(Boolean.toString(cv.isDisableHostNameVerifier()));
                }
                hasDisableHostNameVerifier = true;

            }else if (ATT_IMPLICIT_CONFIRM.equals(configAttribute.getName())) {
                if(!Objects.equals( Boolean.toString(cv.isImplicitConfirm()), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_IMPLICIT_CONFIRM_CHANGED, configAttribute.getValue(), Boolean.toString(cv.isImplicitConfirm()), caConnectorConfig));
                    configAttribute.setValue(Boolean.toString(cv.isImplicitConfirm()));
                }
                hasImplicitConfirm = true;
            }else if (ATT_CMP_MESSAGE_CONTENT_TYPE.equals(configAttribute.getName())) {
                if(!Objects.equals( cv.getMsgContentType(), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MSG_CONTENT_TYPE_CHANGED, configAttribute.getValue(), cv.getMsgContentType(), caConnectorConfig));
                    configAttribute.setValue(cv.getMsgContentType());
                }
                hasMsgContentType = true;
            }else if (ATT_SNI.equals(configAttribute.getName())) {
                if(!Objects.equals( cv.getSni(), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_SNI_CHANGED, configAttribute.getValue(), cv.getSni(), caConnectorConfig));
                    configAttribute.setValue(cv.getSni());
                }
                hasSniType = true;

            }else if (ATT_IGNORE_RESPONSE_MESSAGE_VERIFICATION.equals(configAttribute.getName())) {
                if(!Objects.equals( cv.isIgnoreResponseMessageVerification(), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_IGNORE_RESPONSE_MESSAGE_VERIFICATION_CHANGED, configAttribute.getValue(), Boolean.toString(cv.isIgnoreResponseMessageVerification()), caConnectorConfig));
                    configAttribute.setValue(Boolean.toString(cv.isIgnoreResponseMessageVerification()));
                }
                hasIgnoreResponseMessageVerification = true;

            }else if (ATT_FILL_EMPTY_SUBJECT_WITH_SAN.equals(configAttribute.getName())) {
                if(!Objects.equals( cv.isFillEmptySubjectWithSAN(), configAttribute.getValue())) {
                    auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_FILL_EMPTY_SUBJECT_WITH_SAN_CHANGED, configAttribute.getValue(), Boolean.toString(cv.isFillEmptySubjectWithSAN()), caConnectorConfig));
                    configAttribute.setValue(Boolean.toString(cv.isFillEmptySubjectWithSAN()));
                }
                hasFillEmptySubjectWithSAN = true;

            }else if (ATT_ATTRIBUTE_TYPE_AND_VALUE.equals(configAttribute.getName())) {
                LOG.warn("CA Connector ATaV attribute detected!");
            }
        }
        if( !hasIssuerName){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_ISSUER_NAME_CHANGED, null, cv.getIssuerName(), caConnectorConfig));
            createAttribute(ATT_ISSUER_NAME, cv.getIssuerName(), caConnectorConfig);
        }
        if( !hasMultipleMessages){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MULTIPLE_MESSAGES_CHANGED, null, Boolean.toString(cv.isMultipleMessages()), caConnectorConfig));
            createAttribute(ATT_MULTIPLE_MESSAGES, Boolean.toString(cv.isMultipleMessages()), caConnectorConfig);
        }
        if( !hasImplicitConfirm){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_IMPLICIT_CONFIRM_CHANGED, null, Boolean.toString(cv.isImplicitConfirm()), caConnectorConfig));
            createAttribute(ATT_IMPLICIT_CONFIRM, Boolean.toString(cv.isImplicitConfirm()), caConnectorConfig);
        }
        if( !hasMsgContentType){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_MSG_CONTENT_TYPE_CHANGED, null, cv.getMsgContentType(), caConnectorConfig));
            createAttribute(ATT_CMP_MESSAGE_CONTENT_TYPE, cv.getMsgContentType(), caConnectorConfig);
        }
        if( !hasSniType){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_SNI_CHANGED, null, cv.getSni(), caConnectorConfig));
            createAttribute(ATT_SNI, cv.getSni(), caConnectorConfig);
        }
        if( !hasDisableHostNameVerifier){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_DISABLE_HOST_NAME_VERIFIER_CHANGED, null, Boolean.toString(cv.isDisableHostNameVerifier()), caConnectorConfig));
            createAttribute(ATT_DISABLE_HOST_NAME_VERIFIER, Boolean.toString(cv.isDisableHostNameVerifier()), caConnectorConfig);
        }
        if( !hasIgnoreResponseMessageVerification){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_CA_CONNECTOR_IGNORE_RESPONSE_MESSAGE_VERIFICATION_CHANGED, null, Boolean.toString(cv.isIgnoreResponseMessageVerification()), caConnectorConfig));
            createAttribute(ATT_IGNORE_RESPONSE_MESSAGE_VERIFICATION, Boolean.toString(cv.isIgnoreResponseMessageVerification()), caConnectorConfig);
        }
        if( !hasFillEmptySubjectWithSAN){
            auditList.add(auditService.createAuditTraceCaConnectorConfig( AuditService.AUDIT_FILL_EMPTY_SUBJECT_WITH_SAN_CHANGED, null, Boolean.toString(cv.isFillEmptySubjectWithSAN()), caConnectorConfig));
            createAttribute(ATT_FILL_EMPTY_SUBJECT_WITH_SAN, Boolean.toString(cv.isIgnoreResponseMessageVerification()), caConnectorConfig);
        }

        caConnectorConfigAttributeRepository.saveAll(caConnectorConfig.getCaConnectorAttributes());
        cAConnectorConfigRepository.save(caConnectorConfig);

        auditTraceRepository.saveAll(auditList);

        return caConnectorConfig;
    }

    private void createAttribute(String name, String value, CAConnectorConfig caConnectorConfig) {
        CAConnectorConfigAttribute caConnectorConfigAttribute = new CAConnectorConfigAttribute();
        caConnectorConfigAttribute.setName(name);
        caConnectorConfigAttribute.setValue(Objects.requireNonNullElse(value, ""));
        caConnectorConfigAttribute.setCaConnector(caConnectorConfig);
        caConnectorConfigAttributeRepository.save(caConnectorConfigAttribute);

        caConnectorConfig.getCaConnectorAttributes().add(caConnectorConfigAttribute);
    }

    public String getCAConnectorConfigAttribute(CAConnectorConfig caConnectorConfig, String name, String defaultValue) {

        for (CAConnectorConfigAttribute conAtt : caConnectorConfig.getCaConnectorAttributes()) {
            if (name.equals(conAtt.getName())) {
                return conAtt.getValue();
            }
        }
        return defaultValue;
    }

    public int getCAConnectorConfigAttribute(CAConnectorConfig caConnectorConfig, String name, int defaultValue) {

        for (CAConnectorConfigAttribute conAtt : caConnectorConfig.getCaConnectorAttributes()) {
            if (name.equals(conAtt.getName())) {
                try{
                    return Integer.parseInt(conAtt.getValue());
                }catch( NumberFormatException nfe){
                    LOG.warn("unexpected value for attribute '" + name + "'", nfe);
                }
            }
        }
        return defaultValue;
    }

    public Boolean getCAConnectorConfigAttribute(CAConnectorConfig caConnectorConfig, String name, boolean defaultValue) {

        for (CAConnectorConfigAttribute conAtt : caConnectorConfig.getCaConnectorAttributes()) {
            if (name.equals(conAtt.getName())) {
                return Boolean.parseBoolean(conAtt.getValue());
            }
        }
        return defaultValue;
    }

}
