package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    final private CSRRepository csrRepository;
    final private CsrAttributeRepository csrAttributeRepository;
    final private CSRUtil csrUtil;

    final private AcmeOrderRepository acmeOrderRepository;
    final private AcmeAccountRepository acmeAccountRepository;

    final private PipelineRepository pipelineRepository;

    final private AuditService auditService;

    public SchemaUpdateScheduler(CertificateRepository certificateRepo, CertificateUtil certUtil, CSRRepository csrRepository, CsrAttributeRepository csrAttributeRepository, CSRUtil csrUtil, AcmeOrderRepository acmeOrderRepository, AcmeAccountRepository acmeAccountRepository, PipelineRepository pipelineRepository, AuditService auditService) {
        this.certificateRepo = certificateRepo;
        this.certUtil = certUtil;
        this.csrRepository = csrRepository;
        this.csrAttributeRepository = csrAttributeRepository;
        this.csrUtil = csrUtil;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeAccountRepository = acmeAccountRepository;
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
        updateCSRAttributes();
        LOG.info("updateCSRAttributes took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateAcmeOrder();
        LOG.info("updateAcmeOrder took {} ms", Duration.between(now, Instant.now()));

        now = Instant.now();
        updateACMEAccount();
        LOG.info("updateACMEAccount took {} ms", Duration.between(now, Instant.now()));

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

    public void updateCSRAttributes() {

        List<CSR> updateCSRList = csrRepository.findWithoutAttribute(CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION);

        int count = 0;
        for (CSR csr : updateCSRList) {

            try {
                fixIPSan(csr);
                csrUtil.setCSRAttributeVersion(csr);
                csrAttributeRepository.saveAll(csr.getCsrAttributes());
                csrRepository.save(csr);
                LOG.info("attribute schema updated for csr id {} ", csr.getId());
            } catch (UnknownHostException e) {
                LOG.error("problem with attribute schema update for csr id " + csr.getId(), e);
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
            LOG.info("AcmeOrder pipeline / realm processing of {} orders", count);
        }

    }

    public void updateACMEAccount() {

        Instant now = Instant.now();
        List<AcmeAccount> acmeAccountList = acmeAccountRepository.findByCreatedOnIsNull();

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

            if (count++ > MAX_RECORDS_PER_TRANSACTION) {
                LOG.info("limited AcmeAccount processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
                break;
            }
        }
        if (count > 0) {
            auditService.saveAuditTrace(auditService.createAuditTraceAcmeAcountCreatedOnUpdated(count));
            LOG.info("AcmeAccount createdOn update of {} accounts", count);
        }

    }
}
