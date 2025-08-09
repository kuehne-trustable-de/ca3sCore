package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION;
import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER;

@Service
public class BPMNAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNAsyncUtil.class);
    public static final String NOTIFICATION_SEPARATOR = ":";

    private final BPMNExecutor bpmnExecutor;
    private final CertificateAsyncUtil certificateAsyncUtil;
    private final CertificateRepository certificateRepository;
    private final CertificateUtil certificateUtil;

    public BPMNAsyncUtil(BPMNExecutor bpmnExecutor, CertificateAsyncUtil certificateAsyncUtil, CertificateRepository certificateRepository, CertificateUtil certificateUtil) {
        this.bpmnExecutor = bpmnExecutor;
        this.certificateAsyncUtil = certificateAsyncUtil;
        this.certificateRepository = certificateRepository;
        this.certificateUtil = certificateUtil;
    }

    @Async
    @Transactional
    public void onChange(String processName, Long certificateId, Authentication auth) {

        LOG.info("******************  Async call to onChange( '{}', {})", processName, certificateId);

        if( certificateId == null){
            return;
        }

        Optional<Certificate> optionalCertificate = certificateRepository.findById(certificateId);
        if( optionalCertificate.isEmpty()){
            return;
        }

        Certificate certificate = optionalCertificate.get();
        certificateUtil.setCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION, processName, false);
        certificateUtil.setCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER, "0", false);

        processChange( certificate, auth);

    }

    private void executeAfterTransactionCompletes(Certificate certificate) {
        LOG.info( "********* register transaction sync 'afterComplete'");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            public void afterCompletion(int status) {
                LOG.info( "in afterCompletion:  {}", status);
                if( status == TransactionSynchronization.STATUS_COMMITTED){
                    LOG.debug( "in afterCompletion:  drop notification attributes");
                    certificateUtil.deleteCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION);
                    certificateUtil.deleteCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER);
                    certificateRepository.save(certificate);
                }else{
                    LOG.info( "in afterCompletion:  increment notification counter");
                    certificateAsyncUtil.incrementNotificationCounter(certificate);
                }
            }
        });
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processChange( Certificate certificate , Authentication auth) {

        LOG.info("******************  processChange({}, {})", certificate.getId(), (auth == null ? null:auth.getName()));

        executeAfterTransactionCompletes(certificate);

        String processName = certificateUtil.getCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION);
        if( processName == null){
            return;
        }

        String counterString = certificateUtil.getCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER);
        if( counterString == null){
            return;
        }
        int retryCount = Integer.parseInt(counterString);

        Map<String, Object> variables = new HashMap<>();
        variables.put("certificateId", certificate.getId().toString());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuth = securityContext.getAuthentication();

        try {
            if( auth != null){
                LOG.info("Setting authentication to : {}", auth.getName());
                securityContext.setAuthentication(auth);
            }

            variables.put("currentAuth", securityContext.getAuthentication());

            try {
                ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByName(processName, variables);
                if( processInstance != null) {
                    String status = processInstance.getVariables().get("status").toString();
                    LOG.info("ProcessInstance '{}' terminates with status {}", processInstance.getId(), status);
                }
            } catch (Exception processException) {
                retryCount++;
                String msg = "Exception while calling bpmn process '" + processName + "' fails, incrementing retryCount to " + retryCount;
                LOG.info(msg, processException);
                certificateUtil.setCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION, processName + NOTIFICATION_SEPARATOR + retryCount, false);
            }
        }finally{
            String currentAuthName = currentAuth == null ? null : currentAuth.getName();
            LOG.info("Restoring authentication to : {}", currentAuthName);
            securityContext.setAuthentication(currentAuth);
        }
    }

}
