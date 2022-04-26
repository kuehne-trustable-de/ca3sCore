package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.AuditTraceRepository;
import de.trustable.ca3s.core.service.util.NameAndRole;
import de.trustable.ca3s.core.service.util.NameAndRoleUtil;
import de.trustable.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator {@code AuditEventRepository}.
 */
@Service
@Transactional
public class AuditService {

    public static final String AUDIT_CA3S_STARTED = "CA3S_STARTED";
    public static final String AUDIT_CA3S_STOPPED = "CA3S_STOPPED";

    public static final String AUDIT_EXPIRY_NOTIFICATION_SENT = "EXPIRY_NOTIFICATION_SENT";

    public static final String AUDIT_CSR_ACCEPTED = "CSR_ACCEPTED";
    public static final String AUDIT_CSR_REJECTED = "CSR_REJECTED";
    public static final String AUDIT_CSR_SIGNING_FAILED = "AUDIT_CSR_SIGNING_FAILED";
    public static final String AUDIT_WEB_CERTIFICATE_REQUESTED = "WEB_CERTIFICATE_REQUESTED";
    public static final String AUDIT_WEB_CERTIFICATE_AUTO_ACCEPTED = "WEB_CERTIFICATE_AUTO_ACCEPTED";
    public static final String AUDIT_ACME_CERTIFICATE_REQUESTED = "ACME_CERTIFICATE_REQUESTED";

    public static final String AUDIT_ACME_CERTIFICATE_CREATED = "ACME_CERTIFICATE_CREATED";
    public static final String AUDIT_SCEP_CERTIFICATE_REQUESTED = "SCEP_CERTIFICATE_REQUESTED";
    public static final String AUDIT_SCEP_CERTIFICATE_CREATED = "SCEP_CERTIFICATE_CREATED";
    public static final String AUDIT_RA_CERTIFICATE_CREATED = "RA_CERTIFICATE_CREATED";
    public static final String AUDIT_REQUEST_RESTRICTIONS_FAILED = "REQUEST_RESTRICTIONS_FAILED";
    public static final String AUDIT_WEB_CERTIFICATE_CREATED = "WEB_CERTIFICATE_CREATED";
    public static final String AUDIT_CERTIFICATE_REVOKED = "CERTIFICATE_REVOKED";
    public static final String AUDIT_CERTIFICATE_REVOKED_BY_CRL = "CERTIFICATE_REVOKED_BY_CRL";
    public static final String AUDIT_MANUAL_CERTIFICATE_IMPORTED = "MANUAL_CERTIFICATE_IMPORTED";
    public static final String AUDIT_ADCS_CERTIFICATE_IMPORTED = "ADCS_CERTIFICATE_IMPORTED";
    public static final String AUDIT_TLS_CERTIFICATE_IMPORTED = "TLS_CERTIFICATE_IMPORTED";
    public static final String AUDIT_TLS_INTERMEDIATE_CERTIFICATE_IMPORTED = "TLS_INTERMEDIATE_CERTIFICATE_IMPORTED";

    public static final String AUDIT_CERTIFICATE_SET_TRUSTED_BY_DIRECTORY_IMPORT = "CERTIFICATE_SET_TRUSTED_BY_DIRECTORY_IMPORT";
    public static final String AUDIT_CERTIFICATE_SET_TRUSTED = "CERTIFICATE_SET_TRUSTED";
    public static final String AUDIT_CERTIFICATE_UNSET_TRUSTED = "CERTIFICATE_UNSET_TRUSTED";

    public static final String AUDIT_PIPELINE_ATTRIBUTE_CHANGED = "PIPELINE_ATTRIBUTE_CHANGED";
    public static final String AUDIT_CSR_ATTRIBUTE_CHANGED = "CSR_ATTRIBUTE_CHANGED";

    public static final String AUDIT_CERTIFICATE_IMPORTED = "CERTIFICATE_IMPORTED";
    public static final String AUDIT_CERTIFICATE_ATTRIBUTE_CHANGED = "CERTIFICATE_ATTRIBUTE_CHANGED";

    public static final String AUDIT_EMAIL_SEND_NOTIFICATION_FAILED = "EMAIL_SEND_NOTIFICATION_FAILED";

    public static final String AUDIT_PIPELINE_CREATED = "PIPELINE_CREATED";
    public static final String AUDIT_PIPELINE_COPIED = "PIPELINE_COPIED";
    public static final String AUDIT_PIPELINE_DELETED = "PIPELINE_DELETED";
    public static final String AUDIT_PIPELINE_NAME_CHANGED = "PIPELINE_NAME_CHANGED";
    public static final String AUDIT_PIPELINE_DESCRIPTION_CHANGED = "PIPELINE_DESCRIPTION_CHANGED";
    public static final String AUDIT_PIPELINE_TYPE_CHANGED = "PIPELINE_TYPE_CHANGED";
    public static final String AUDIT_PIPELINE_URLPART_CHANGED = "PIPELINE_URLPART_CHANGED";
    public static final String AUDIT_PIPELINE_APPROVAL_REQUIRED_CHANGED = "PIPELINE_APPROVAL_REQUIRED_CHANGED";
    public static final String AUDIT_PIPELINE_ACTIVE_CHANGED = "PIPELINE_ACTIVE_CHANGED";
    public static final String AUDIT_CRAWLER_CERTIFICATE_IMPORTED = "CRAWLER_CERTIFICATE_IMPORTED";
    public static final String AUDIT_DIRECTORY_CERTIFICATE_IMPORTED = "DIRECTORY_CERTIFICATE_IMPORTED";

    public static final String AUDIT_CA_CONNECTOR_CREATED = "CA_CONNECTOR_CREATED";
    public static final String AUDIT_CA_CONNECTOR_COPIED = "CA_CONNECTOR_COPIED";
    public static final String AUDIT_CA_CONNECTOR_DELETED = "CA_CONNECTOR_DELETED";
    public static final String AUDIT_CA_CONNECTOR_ATTRIBUTE_CHANGED = "CA_CONNECTOR_ATTRIBUTE_CHANGED";
    public static final String AUDIT_CERTIFICATE_SCHEMA_UPDATED = "CERTIFICATE_SCHEMA_UPDATED";
    public static final String AUDIT_ACME_ORDER_PIPELINE_UPDATED = "AUDIT_ACME_ORDER_PIPELINE_UPDATED";


    private final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditTraceRepository auditTraceRepository;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final NameAndRoleUtil nameAndRoleUtil;

    public AuditService(AuditTraceRepository auditTraceRepository, ApplicationEventPublisher applicationEventPublisher, NameAndRoleUtil nameAndRoleUtil) {

        this.auditTraceRepository = auditTraceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.nameAndRoleUtil = nameAndRoleUtil;
    }


    public AuditTrace createAuditTraceStarted(){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(), AUDIT_CA3S_STARTED,
            null,
            null,
            null,
            null,
            null );

    }

    public AuditTrace createAuditTraceStopped(){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(), AUDIT_CA3S_STOPPED,
            null,
            null,
            null,
            null,
            null );

    }

    public AuditTrace createAuditTraceExpiryNotificationSent(int nExpiringCertificates){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();

        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_EXPIRY_NOTIFICATION_SENT,
            null,
            null, "" + nExpiringCertificates,
            null,
            null,
            null,
            null,
            null );

    }

    public AuditTrace createAuditTraceCsrAccepted(final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_CSR_ACCEPTED, csr);
    }

    public AuditTrace createAuditTraceCsrRejected(final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_CSR_REJECTED, csr);
    }

    public AuditTrace createAuditTraceCsrRejected(final CSR csr, final String reason){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();

        return createAuditTrace(nar.getName(), nar.getRole(), AUDIT_CSR_REJECTED,
            reason,
            null, null,
            csr,
            null,
            null,
            null,
            null );

    }

    public AuditTrace createAuditTraceCsrSigningFailed(final CSR csr, final String reason){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();

        return createAuditTrace(nar.getName(), nar.getRole(), AUDIT_CSR_SIGNING_FAILED,
            reason,
            null, null,
            csr,
            null,
            null,
            null,
            null );

    }

    public AuditTrace createAuditTraceACMERequest(final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_ACME_CERTIFICATE_REQUESTED, csr);
    }

    public AuditTrace createAuditTraceWebRequest(final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_WEB_CERTIFICATE_REQUESTED, csr);
    }

    public AuditTrace createAuditTraceWebAutoAccepted(final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_WEB_CERTIFICATE_AUTO_ACCEPTED, csr);
    }

    public AuditTrace createAuditTraceCsrRestrictionFailed( final CSR csr){
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTraceRequest(nar.getName(), nar.getRole(), AUDIT_REQUEST_RESTRICTIONS_FAILED, csr);
    }


    public AuditTrace createAuditTraceRequest(final String actor, final String actorRole, final String template, final CSR csr){

        return createAuditTrace(actor, actorRole, template,
            csr,
            null,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceRequest(final String template, final CSR csr){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            template,
            csr,
            null,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCertificate(final String template, final Certificate certificate){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            template,
            null,
            certificate,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCAConfigCreated(final CAConnectorConfig caConnectorConfig){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CA_CONNECTOR_CREATED,
            null,
            caConnectorConfig.getName(),
            null,
            null,
            null,
            caConnectorConfig,
            null );
    }


    public AuditTrace createAuditTraceCAConfigDeleted(final CAConnectorConfig caConnectorConfig){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CA_CONNECTOR_DELETED,
            null,
            caConnectorConfig.getName(),
            null,
            null,
            null,
            caConnectorConfig,
            null );
    }


    public AuditTrace createAuditTraceCAConfigSecretChanged(final CAConnectorConfig caConnectorConfig){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            caConnectorConfig.getName(),
            null,
            null,
            null,
            caConnectorConfig,
            null );
    }

    public AuditTrace createAuditTraceNotificationFailed(String email) {

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_EMAIL_SEND_NOTIFICATION_FAILED,
            email,
            null, null,
            null,
            null,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCertificateTrusted(String filename, Certificate certificate, final CAConnectorConfig caConnectorConfig) {
        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CERTIFICATE_SET_TRUSTED_BY_DIRECTORY_IMPORT,
            filename,
            null, null,
            null,
            certificate,
            null,
            caConnectorConfig,
            null );
    }

    public AuditTrace  createAuditTraceCAConfigCreatedChange(final String attributeName, final String oldVal, final String newVal, final CAConnectorConfig caConnectorConfig){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CA_CONNECTOR_ATTRIBUTE_CHANGED,
            attributeName,
            oldVal, newVal,
            null,
            null,
            null,
            caConnectorConfig,
            null );

    }

    public AuditTrace createAuditTracePipeline(final String template, final Pipeline pipeline){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            template,
            null,
            null,
            pipeline,
            null,
            null );
    }

    public AuditTrace createAuditTracePipeline(final String template, final String oldVal, final String newVal, final Pipeline pipeline){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            template,
            oldVal, newVal,
            null,
            null,
            pipeline,
            null,
            null );
    }

    public AuditTrace createAuditTracePipelineAttribute(final String attributeName, final String oldVal, final String newVal, final Pipeline pipeline){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_PIPELINE_ATTRIBUTE_CHANGED,
            attributeName,
            oldVal, newVal,
            null,
            null,
            pipeline,
            null,
            null );
    }

    public AuditTrace createAuditTraceCsrAttribute(final String attributeName, final String oldVal, final String newVal, final CSR csr){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CSR_ATTRIBUTE_CHANGED,
            attributeName,
            oldVal, newVal,
            csr,
            null,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCertificateAttribute(final String attributeName, final String oldVal, final String newVal, final Certificate certificate){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CERTIFICATE_ATTRIBUTE_CHANGED,
            attributeName,
            oldVal, newVal,
            null,
            certificate,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCertificateImported(final String source, final Certificate certificate, CAConnectorConfig caConfig){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CERTIFICATE_IMPORTED,
            source,
            null, null,
            null,
            certificate,
            null,
            null,
            null );
    }

    public AuditTrace createAuditTraceCertificateSchemaUpdated(final int nUpdated, final int version){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_CERTIFICATE_SCHEMA_UPDATED,
            "" + nUpdated,
            null, "" + version,
            null,
            null,
            null,
            null,
            null );
    }


    public AuditTrace createAuditTraceAcmeOrderPipelineUpdated(final int nUpdated){

        NameAndRole nar = nameAndRoleUtil.getNameAndRole();
        return createAuditTrace(nar.getName(), nar.getRole(),
            AUDIT_ACME_ORDER_PIPELINE_UPDATED,
            "" + nUpdated,
            null, "",
            null,
            null,
            null,
            null,
            null );
    }


    public AuditTrace createAuditTrace(final String actor, final String actorRole, final String template,
                            final CSR csr,
                            final Certificate certificate,
                            final Pipeline pipeline,
                            final CAConnectorConfig caConnector,
                            final BPMNProcessInfo processInfo ) {

        return createAuditTrace(actor, actorRole, template,
        null, null,
        csr,
        certificate,
        pipeline,
        caConnector,
        processInfo );
    }

    public AuditTrace createAuditTrace(final String actor, final String actorRole, final String template,
                                       final String oldVal, final String newVal,
                                       final CSR csr,
                                       final Certificate certificate,
                                       final Pipeline pipeline,
                                       final CAConnectorConfig caConnector,
                                       final BPMNProcessInfo processInfo ){

        return createAuditTrace(actor, actorRole, template,
        null,
        oldVal, newVal,
        csr,
        certificate,
        pipeline,
        caConnector,
        processInfo );
    }

    public AuditTrace createAuditTrace(final String actor, final String actorRole, final String template,
       final String attributeName,
       final String oldVal, final String newVal,
        final CSR csr,
        final Certificate certificate,
        final Pipeline pipeline,
        final CAConnectorConfig caConnector,
        final BPMNProcessInfo processInfo ){

        String msg = "";
        String content = "";

        if(attributeName != null) {
            msg = limitAndEscapeContent(attributeName, 30) ;
            content = limitAndEscapeContent(attributeName, 50) ;
        }

        if(oldVal != null || newVal != null) {
            msg += "," + limitAndEscapeContent(oldVal, 100) + "," + limitAndEscapeContent(newVal, 100);
            content += "," + limitAndEscapeContent(oldVal, 1000) + "," + limitAndEscapeContent(newVal, 1000);
        }

        Map<String,Object> eventData = new HashMap<>();
        if( !msg.isEmpty()) {
            eventData.put("content", CryptoUtil.limitLength(msg, 250));
        }
        applicationEventPublisher.publishEvent(new AuditApplicationEvent( actor, template, eventData));

        log.debug("Audit trace for {}, attribute {}, oldVal {}, newVal {} ", template, attributeName, oldVal, newVal);

		AuditTrace auditTrace = new AuditTrace();
        auditTrace.setActorName(CryptoUtil.limitLength(actor, 50));
        auditTrace.setActorRole(CryptoUtil.limitLength(actorRole, 50));
        auditTrace.setPlainContent(content);
        auditTrace.setContentTemplate(template);
        auditTrace.setCreatedOn(Instant.now());
        auditTrace.setCsr(csr);
        auditTrace.setCertificate(certificate);
        auditTrace.setPipeline(pipeline);
        auditTrace.setCaConnector(caConnector);
        auditTrace.setProcessInfo(processInfo);

        return auditTrace;
    }

    private String limitAndEscapeContent(String in, int maxLen){
        if( in == null){
            return "";
        }

        if( in.length() > maxLen){
            in = CryptoUtil.limitLength(in, maxLen -3 ) + "...";
        }
        return in.replace("%", "%25").replace(",", "%2C");
    }

    public void saveAuditTrace(final AuditTrace auditTrace){
        auditTraceRepository.save(auditTrace);
    }

    public void saveAuditTrace(final List<AuditTrace> auditTraceList){
        auditTraceRepository.saveAll(auditTraceList);
    }

}
