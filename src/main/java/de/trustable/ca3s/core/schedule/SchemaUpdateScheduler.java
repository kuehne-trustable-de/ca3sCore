package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentStatus;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SchemaUpdateScheduler {

    transient Logger LOG = LoggerFactory.getLogger(SchemaUpdateScheduler.class);

    final private int maxRecordsPerTransaction;

    final private CertificateRepository certificateRepo;
    final private CertificateUtil certUtil;

    final private CSRRepository csrRepository;
    final private CsrAttributeRepository csrAttributeRepository;
    final private CSRUtil csrUtil;

    final private AcmeOrderRepository acmeOrderRepository;
    final private AcmeAccountRepository acmeAccountRepository;

    final private PipelineRepository pipelineRepository;

    final private AuditService auditService;
    final private AuditTraceRepository auditServiceRepository;
    final private ProtectedContentRepository protectedContentRepository;

    final private PreferenceUtil preferenceUtil;

    public SchemaUpdateScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                                 CertificateRepository certificateRepo, CertificateUtil certUtil, CSRRepository csrRepository, CsrAttributeRepository csrAttributeRepository, CSRUtil csrUtil, AcmeOrderRepository acmeOrderRepository, AcmeAccountRepository acmeAccountRepository, PipelineRepository pipelineRepository, AuditService auditService, AuditTraceRepository auditServiceRepository, ProtectedContentRepository protectedContentRepository, PreferenceUtil preferenceUtil) {
        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
        this.certificateRepo = certificateRepo;
        this.certUtil = certUtil;
        this.csrRepository = csrRepository;
        this.csrAttributeRepository = csrAttributeRepository;
        this.csrUtil = csrUtil;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeAccountRepository = acmeAccountRepository;
        this.pipelineRepository = pipelineRepository;
        this.auditService = auditService;
        this.auditServiceRepository = auditServiceRepository;
        this.protectedContentRepository = protectedContentRepository;
        this.preferenceUtil = preferenceUtil;
    }


    //    @Scheduled(fixedDelay = 3600000)
    @Scheduled(fixedDelay = 600000)
    public void performSchemaUpdates() {

        Instant now = Instant.now();
        updateCertificateAttributes();
        LOG.info("updateCertificateAttributes took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateCSRAttributes();
        LOG.info("updateCSRAttributes took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateAcmeOrder();
        LOG.info("updateAcmeOrder took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateACMEAccount();
        LOG.info("updateACMEAccount took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateProtectedContent();
        LOG.info("updateProtectedContent took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updatePipeline();
        LOG.info("updatePipeline took {} ms", Duration.between(now, Instant.now()));

    }

    public void updateCertificateAttributes() {

        Page<Certificate> updateCertificateList =
            certificateRepo.findByAttributeValueLowerThan(
                PageRequest.of(0, maxRecordsPerTransaction),
                CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION,
                "" + CertificateUtil.CURRENT_ATTRIBUTES_VERSION);

        LOG.info("{} certificates selected for schema update", updateCertificateList.getNumberOfElements());

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

            if (count++ > maxRecordsPerTransaction) {
                LOG.info("limited certificate validity processing to {} per call", maxRecordsPerTransaction);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceCertificateSchemaUpdated(count, CertificateUtil.CURRENT_ATTRIBUTES_VERSION));
        }

    }

    public void updateCSRAttributes() {

        Page<CSR> updateCSRList = csrRepository.findWithoutAttribute(
            PageRequest.of(0, maxRecordsPerTransaction),
            CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION);

        LOG.info("{} CSRs without version attribute selected for schema update", updateCSRList.getNumberOfElements());

        int count = 0;
        for (CSR csr : updateCSRList) {

            try {
                fixIPSan(csr);
                csrUtil.setCSRAttributeVersion(csr, "1");
                csrAttributeRepository.saveAll(csr.getCsrAttributes());
                csrRepository.save(csr);
                LOG.info("attribute schema updated for csr id {} ", csr.getId());
            } catch (UnknownHostException e) {
                LOG.error("problem with attribute schema update for csr id " + csr.getId(), e);
            }

            if (count++ > maxRecordsPerTransaction) {
                LOG.info("limited CSR schema processing to {} per call", maxRecordsPerTransaction);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceCertificateSchemaUpdated(count, CsrAttribute.CURRENT_ATTRIBUTES_VERSION));
        }

        Page<CSR> version1CSRList = csrRepository.findByAttributeValue(
            PageRequest.of(0, maxRecordsPerTransaction),
            CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION, "1");

        count = 0;
        for (CSR csr : version1CSRList) {
            LOG.info("checking audit for csr id {}", csr.getId());
            csrUtil.setCSRAttributeVersion(csr, "" + CsrAttribute.CURRENT_ATTRIBUTES_VERSION);
//            csrAttributeRepository.saveAll(csr.getCsrAttributes());
            List<AuditTrace> auditTraceList =  auditServiceRepository.findByCsrAndTemplate( csr, "CSR_ACCEPTED");
            if( !auditTraceList.isEmpty()){
                csr.setAcceptedBy( auditTraceList.get(0).getActorName());
                LOG.info("csr id {} accepted by '{}'", csr.getId(), csr.getAcceptedBy());
            }
            csrRepository.save(csr);

            if (count++ > maxRecordsPerTransaction) {
                LOG.info("limited CSR 'accepted by' processing to {} per call", maxRecordsPerTransaction);
                break;
            }

        }

    }

    private void fixIPSan(CSR csr) throws UnknownHostException {
        for( CsrAttribute attr: csr.getCsrAttributes()){
            String value = null;
            if( attr.getName().equals(CsrAttribute.ATTRIBUTE_SAN) &&
                attr.getValue().startsWith("#")){
                value = attr.getValue().substring(1);
            }
            if( attr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN) &&
                attr.getValue().startsWith("IP:#")){
                value = attr.getValue().substring(4);
            }
            if( value != null) {
                try {
                    InetAddress inetAddress = InetAddress.getByAddress(Hex.decode(value));

                    if (attr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN)) {
                        attr.setValue("IP:" + inetAddress.getHostAddress());
                        LOG.debug("update TYPED_SAN attribute #{} to {}", attr.getId(), attr.getValue());
                    } else {
                        attr.setValue(inetAddress.getHostAddress());
                        LOG.debug("update SAN attribute #{} to {}", attr.getId(), attr.getValue());
                    }
                }catch (DecoderException de){
                    LOG.info("SAN attribute #{} contains invalid IP address {}, ignoring ...", attr.getId(), value);
                }
            }
        }
    }

    public void updateAcmeOrder() {

        Page<AcmeOrder> acmeOrderList = acmeOrderRepository.findPipelineIsNull(PageRequest.of(0, maxRecordsPerTransaction));

        LOG.info("{} ACME Order without pipeline reference selected for schema update", acmeOrderList.getNumberOfElements());

        int count = 0;
        for (AcmeOrder acmeOrder : acmeOrderList) {
            if( acmeOrder.getAccount() != null ) {
                String realm = acmeOrder.getAccount().getRealm();
                List<Pipeline> pipelineList = pipelineRepository.findByTypeUrl(PipelineType.ACME, realm);
                if (!pipelineList.isEmpty()) {
                    acmeOrder.setPipeline(pipelineList.get(0));
                    acmeOrder.setRealm(realm);
                    acmeOrderRepository.save(acmeOrder);
                    LOG.info("realm and pipeline updated for acme order {} ", acmeOrder);
                }

                if (count++ > maxRecordsPerTransaction) {
                    LOG.info("limited AcmeOrder processing to {} per call", maxRecordsPerTransaction);
                    break;
                }
            }else{
                LOG.info("acme order {} has no account!", acmeOrder.getId());
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeOrderPipelineUpdated(count));
            LOG.info("AcmeOrder pipeline / realm processing of {} orders", count);
        }

    }

    public void updateACMEAccount() {

        Instant now = Instant.now();
        Page<AcmeAccount> acmeAccountList = acmeAccountRepository.findByCreatedOnIsNull(PageRequest.of(0, maxRecordsPerTransaction));

        LOG.info("{} ACME account without 'createdOn'' selected for schema update", acmeAccountList.getNumberOfElements());

        int count = 0;
        for (AcmeAccount acmeAccount : acmeAccountList) {

            Instant oldestOrder = now;

            for( AcmeOrder acmeOrder: acmeAccount.getOrders()){
                if( acmeOrder.getNotBefore().isBefore(oldestOrder) ){
                    oldestOrder = acmeOrder.getNotBefore();
                }
            }

            acmeAccount.setCreatedOn(oldestOrder);
            acmeAccountRepository.save(acmeAccount);
            LOG.info("CreatedOn date updated for acme account {} ", acmeAccount.getAccountId());

            if (count++ > maxRecordsPerTransaction) {
                LOG.info("limited AcmeAccount processing to {} per call", maxRecordsPerTransaction);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeAcountCreatedOnUpdated(count));
            LOG.info("AcmeAccount createdOn update of {} accounts", count);
        }

    }
    public void updateProtectedContent() {

        Page<ProtectedContent> protectedContentList = protectedContentRepository.findByProtectedContentStatusIsNull(PageRequest.of(0, maxRecordsPerTransaction));

        LOG.info("{} ProtectedContents with empty status selected for schema update", protectedContentList.getNumberOfElements());

        int count = 0;
        for (ProtectedContent protectedContent : protectedContentList) {

            protectedContent.setStatus(ProtectedContentStatus.ACTIVE);
            protectedContentRepository.save(protectedContent);
            LOG.info("Status updated for ProtectedContent {} ", protectedContent.getId());

            if (count++ > maxRecordsPerTransaction) {
                LOG.info("limited ProtectedContent processing to {} per call", maxRecordsPerTransaction);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeAcountCreatedOnUpdated(count));
            LOG.info("ProtectedContent status updated of {} items", count);
        }
    }
    public void updatePipeline() {

        int version = preferenceUtil.getPipelineSchemaVersion();
        LOG.info("Current Pipeline schema {}", version);

        Set<Authority> authoritySet = new HashSet<>();
        authoritySet.add(new Authority("ROLE_USER"));

        int count = 0;
        if( version == 0) {
            for (Pipeline pipeline : pipelineRepository.findAll()) {

                if( pipeline.getType() == PipelineType.WEB ) {
                    pipeline.setAuthorities(authoritySet);
                    pipelineRepository.save(pipeline);
                    count++;
                }
            }
            preferenceUtil.setPipelineSchemaVersion(1);
        }

        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeAcountCreatedOnUpdated(count));
            LOG.info("ProtectedContent status updated of {} items", count);
        }
    }
}
