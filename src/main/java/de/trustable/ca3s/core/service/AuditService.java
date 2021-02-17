package de.trustable.ca3s.core.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.config.audit.AuditEventConverter;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.AuditTraceRepository;
import de.trustable.ca3s.core.repository.PersistenceAuditEventRepository;
import de.trustable.ca3s.core.service.util.AuditUtil;
import io.github.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.ManyToOne;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Service for managing audit events.
 * <p>
 * This is the default implementation to support SpringBoot Actuator {@code AuditEventRepository}.
 */
@Service
@Transactional
public class AuditService {

    private final Logger log = LoggerFactory.getLogger(AuditService.class);

    private AuditTraceRepository auditTraceRepository;

    private ApplicationEventPublisher applicationEventPublisher;

    public AuditService( AuditTraceRepository auditTraceRepository, ApplicationEventPublisher applicationEventPublisher) {

        this.auditTraceRepository = auditTraceRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void createAuditTraceCsrAccepted(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_CSR_ACCEPTED, csr);
    }

    public void createAuditTraceCsrRejected(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_CSR_REJECTED, csr);
    }

    public void createAuditTraceACMERequest(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_ACME_CERTIFICATE_REQUESTED, csr);
    }

    public void createAuditTraceSCEPRequest(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_SCEP_CERTIFICATE_REQUESTED, csr);
    }

    public void createAuditTraceWebRequest(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_WEB_CERTIFICATE_REQUESTED, csr);
    }


    public void createAuditTraceCsrRestrictionFailed(final String actor, String actorRole, final CSR csr){
        createAuditTraceRequest(actor, actorRole, AuditUtil.AUDIT_REQUEST_RESTRICTIONS_FAILED, csr);
    }


    public void createAuditTraceRequest(final String actor, final String actorRole, final String template, final CSR csr){

        createAuditTrace(actor, actorRole, template,
            csr,
            null,
            null,
            null,
            null );
    }

    public void createAuditTraceCertificateRevoked(final String actor, String actorRole, final Certificate certificate){
        createAuditTraceCertificateCreated(actor, actorRole, AuditUtil.AUDIT_CERTIFICATE_REVOKED, certificate);
    }

    public void createAuditTraceWebCertificateCreated(final String actor, String actorRole, final Certificate certificate){
        createAuditTraceCertificateCreated(actor, actorRole, AuditUtil.AUDIT_WEB_CERTIFICATE_CREATED, certificate);
    }

    public void createAuditTraceACMECertificateCreated(final String actor, String actorRole, final Certificate certificate){
        createAuditTraceCertificateCreated(actor, actorRole, AuditUtil.AUDIT_ACME_CERTIFICATE_CREATED, certificate);
    }

    public void createAuditTraceScepCertificateCreated(final String actor, String actorRole, final Certificate certificate){
        createAuditTraceCertificateCreated(actor, actorRole, AuditUtil.AUDIT_SCEP_CERTIFICATE_CREATED, certificate);
    }

    public void createAuditTraceCertificateCreated(final String actor, String actorRole, final String template, final Certificate certificate){

        createAuditTrace(actor, actorRole, template,
            null,
            certificate,
            null,
            null,
            null );
    }


    public void createAuditTrace(final String actor, final String actorRole, final String template,
                            final CSR csr,
                            final Certificate certificate,
                            final Pipeline pipeline,
                            final CAConnectorConfig caConnector,
                            final BPMNProcessInfo processInfo ){

        String msg = template;
		applicationEventPublisher.publishEvent(
		    new AuditApplicationEvent( actor, AuditUtil.AUDIT_CSR_ACCEPTED, msg));

		AuditTrace auditTrace = new AuditTrace();
        auditTrace.setActorName(actor);
        auditTrace.setActorRole(actorRole);
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
