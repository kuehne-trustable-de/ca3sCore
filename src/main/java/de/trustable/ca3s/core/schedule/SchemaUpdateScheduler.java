package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SchemaUpdateScheduler {

    transient Logger LOG = LoggerFactory.getLogger(SchemaUpdateScheduler.class);

    final static int MAX_RECORDS_PER_TRANSACTION = 10000;

    final private CertificateRepository certificateRepo;
    final private CertificateUtil certUtil;

    final private AcmeOrderRepository acmeOrderRepository;
    final private PipelineRepository pipelineRepository;

    final private AuditService auditService;

    public SchemaUpdateScheduler(CertificateRepository certificateRepo, CertificateUtil certUtil, AcmeOrderRepository acmeOrderRepository, PipelineRepository pipelineRepository, AuditService auditService) {
        this.certificateRepo = certificateRepo;
        this.certUtil = certUtil;
        this.acmeOrderRepository = acmeOrderRepository;
        this.pipelineRepository = pipelineRepository;
        this.auditService = auditService;
    }


    //    @Scheduled(fixedDelay = 3600000)
    @Scheduled(fixedDelay = 60000)
    public void performSchemaApdates() {

        Instant now = Instant.now();
        updateCertificateAttributes();
        LOG.info("updateCertificateAttributes took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateACMEOrder();
        LOG.info("updateACMEOrder took {} ms", Duration.between(now, Instant.now()));

    }

    public void updateCertificateAttributes() {

        List<Certificate> updateCertificateList = certificateRepo.findByAttributeValueLowerThan(CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION,
            "" + CertificateUtil.CURRENT_ATTRIBUTES_VERSION);

        int count = 0;
        for (Certificate cert : updateCertificateList) {

            X509Certificate x509Cert;
            try {
                int currentVersion = Integer.parseInt(certUtil.getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION));

                x509Cert = CryptoService.convertPemToCertificate(cert.getContent());

                if (currentVersion < 4) {
                    certUtil.interpretBasicConstraint(x509Cert, cert);
                }
                certUtil.addAdditionalCertificateAttributes(x509Cert, cert);

                certificateRepo.save(cert);
                LOG.info("attribute schema updated for certificate id {} ", cert.getId());
            } catch (GeneralSecurityException | IOException e) {
                LOG.error("problem with attribute schema update for certificate id " + cert.getId(), e);
            }

            if (count++ > MAX_RECORDS_PER_TRANSACTION) {
                LOG.info("limited certificate validity processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceCertificateSchemaUpdated(count, CertificateUtil.CURRENT_ATTRIBUTES_VERSION));
        }

    }

    public void updateACMEOrder() {

        Instant now = Instant.now();
        List<AcmeOrder> acmeOrderList = acmeOrderRepository.findPipelineIsNull();

        int count = 0;
        for (AcmeOrder acmeOrder : acmeOrderList) {
            String realm = acmeOrder.getAccount().getRealm();
            List<Pipeline> pipelineList = pipelineRepository.findByTypeUrl(PipelineType.ACME, realm);
            if( !pipelineList.isEmpty() ){
                acmeOrder.setPipeline(pipelineList.get(0));
                acmeOrder.setRealm(realm);
                acmeOrderRepository.save(acmeOrder);
                LOG.info("realm and pipeljne updated for acme order {} ", acmeOrder);
            }

            if (count++ > MAX_RECORDS_PER_TRANSACTION) {
                LOG.info("limited AcmeOrder processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeOrderPipelineUpdated(count));
        }

    }
}
