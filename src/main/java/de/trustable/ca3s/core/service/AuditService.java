package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.AuditTraceRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator {@code AuditEventRepository}.
 */
@Service
@Transactional
public class AuditService {

    public static final String AUDIT_CSR_ACCEPTED = "CSR_ACCEPTED";
    public static final String AUDIT_CSR_REJECTED = "CSR_REJECTED";
    public static final String AUDIT_WEB_CERTIFICATE_REQUESTED = "WEB_CERTIFICATE_REQUESTED";
    public static final String AUDIT_ACME_CERTIFICATE_REQUESTED = "ACME_CERTIFICATE_REQUESTED";
    public static final String AUDIT_ACME_CERTIFICATE_CREATED = "ACME_CERTIFICATE_CREATED";
    public static final String AUDIT_SCEP_CERTIFICATE_REQUESTED = "SCEP_CERTIFICATE_REQUESTED";
    public static final String AUDIT_SCEP_CERTIFICATE_CREATED = "SCEP_CERTIFICATE_CREATED";
    public static final String AUDIT_REQUEST_RESTRICTIONS_FAILED = "REQUEST_RESTRICTIONS_FAILED";
    public static final String AUDIT_WEB_CERTIFICATE_CREATED = "WEB_CERTIFICATE_CREATED";
    public static final String AUDIT_CERTIFICATE_REVOKED = "CERTIFICATE_REVOKED";

    public static final String AUDIT_PIPELINE = "PIPELINE_";
    public static final String AUDIT_CHANGED = "_CHANGED";

    public static final String AUDIT_PIPELINE_CREATED = "PIPELINE_CREATED";
    public static final String AUDIT_PIPELINE_DELETED = "PIPELINE_DELETED";
    public static final String AUDIT_PIPELINE_NAME_CHANGED = "PIPELINE_NAME_CHANGED";
    public static final String AUDIT_PIPELINE_DESCRIPTION_CHANGED = "PIPELINE_DESCRIPTION_CHANGED";
    public static final String AUDIT_PIPELINE_TYPE_CHANGED = "PIPELINE_TYPE_CHANGED";
    public static final String AUDIT_PIPELINE_URLPART_CHANGED = "PIPELINE_URLPART_CHANGED";
    public static final String AUDIT_PIPELINE_APPROVAL_REQUIRED_CHANGED = "PIPELINE_APPROVAL_REQUIRED_CHANGED";


    private final Logger log = LoggerFactory.getLogger(AuditService.class);

    private AuditTraceRepository auditTraceRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    public AuditService( AuditTraceRepository auditTraceRepository, ApplicationEventPublisher applicationEventPublisher) {

        this.auditTraceRepository = auditTraceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }


    String getRole(Authentication auth){

        log.debug( "Authorities #{} present", auth.getAuthorities().size());

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.ADMIN))){
            return "ADMIN";
        }

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.RA_OFFICER))){
            return "RA";
        }

        if( auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(AuthoritiesConstants.USER))){
            return "USER";
        }

        for( GrantedAuthority ga: auth.getAuthorities()){
            log.debug( "Authority: {}", ga.getAuthority());
        }
        return "ANON";
    }

    public void createAuditTraceCsrAccepted(final CSR csr){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createAuditTraceRequest(auth.getName(), getRole(auth), AUDIT_CSR_ACCEPTED, csr);
    }

    public void createAuditTraceCsrRejected(final CSR csr){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createAuditTraceRequest(auth.getName(), getRole(auth), AUDIT_CSR_REJECTED, csr);
    }

    public void createAuditTraceACMERequest(final CSR csr){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createAuditTraceRequest(auth.getName(), getRole(auth), AUDIT_ACME_CERTIFICATE_REQUESTED, csr);
    }

    public void createAuditTraceWebRequest(final CSR csr){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createAuditTraceRequest(auth.getName(), getRole(auth), AUDIT_WEB_CERTIFICATE_REQUESTED, csr);
    }

    public void createAuditTraceCsrRestrictionFailed( final CSR csr){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        createAuditTraceRequest(auth.getName(), getRole(auth), AUDIT_REQUEST_RESTRICTIONS_FAILED, csr);
    }


    public void createAuditTraceRequest(final String actor, final String actorRole, final String template, final CSR csr){

        createAuditTrace(actor, actorRole, template,
            csr,
            null,
            null,
            null,
            null );
    }

    public void createAuditTraceRequest(final String template, final CSR csr){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        createAuditTrace(auth.getName(), getRole(auth), template,
            csr,
            null,
            null,
            null,
            null );
    }

    public void createAuditTraceCertificateCreated(final String template, final Certificate certificate){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        createAuditTrace(auth.getName(), getRole(auth), template,
            null,
            certificate,
            null,
            null,
            null );
    }

    public void createAuditTracePipeline(final String template, final Pipeline pipeline){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        createAuditTrace(auth.getName(), getRole(auth), template,
            null,
            null,
            pipeline,
            null,
            null );
    }

    public void createAuditTracePipeline(final String template, final String oldVal, final String newVal, final Pipeline pipeline){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        createAuditTrace(auth.getName(), getRole(auth), template,
            oldVal, newVal,
            null,
            null,
            pipeline,
            null,
            null );
    }

    public void createAuditTracePipelineAttribute(final String attributeName, final String oldVal, final String newVal, final Pipeline pipeline){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        createAuditTrace(auth.getName(), getRole(auth),
            AUDIT_PIPELINE + attributeName + AUDIT_CHANGED,
            oldVal, newVal,
            null,
            null,
            pipeline,
            null,
            null );
    }


    public void createAuditTrace(final String actor, final String actorRole, final String template,
                            final CSR csr,
                            final Certificate certificate,
                            final Pipeline pipeline,
                            final CAConnectorConfig caConnector,
                            final BPMNProcessInfo processInfo ) {

        createAuditTrace(actor, actorRole, template,
        null, null,
        csr,
        certificate,
        pipeline,
        caConnector,
        processInfo );
    }

    public void createAuditTrace(final String actor, final String actorRole, final String template,
                                 final String oldVal, final String newVal,
                                 final CSR csr,
                                 final Certificate certificate,
                                 final Pipeline pipeline,
                                 final CAConnectorConfig caConnector,
                                 final BPMNProcessInfo processInfo ){

        String msg = template;

        if( oldVal != null || newVal != null) {
            msg = template + ",'" + CryptoUtil.limitLength(oldVal, 100) + "', '" + CryptoUtil.limitLength(newVal, 100) + "'";
        }

        applicationEventPublisher.publishEvent(new AuditApplicationEvent( actor, template, msg));

		AuditTrace auditTrace = new AuditTrace();
        auditTrace.setActorName(CryptoUtil.limitLength(actor, 50));
        auditTrace.setActorRole(CryptoUtil.limitLength(actorRole, 50));
        auditTrace.setPlainContent(msg);
        auditTrace.setContentTemplate(template);
        auditTrace.setCreatedOn(Instant.now());
        auditTrace.setCsr(csr);
        auditTrace.setCertificate(certificate);
        auditTrace.setPipeline(pipeline);
        auditTrace.setCaConnector(caConnector);
        auditTrace.setProcessInfo(processInfo);

        auditTraceRepository.save(auditTrace);

    }

}
