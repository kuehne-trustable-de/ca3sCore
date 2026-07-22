package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.config.Constants;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.dto.bpmn.CSRDecisionResultInput;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;

import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION;
import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER;
import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_CSR_DECISION_NOTIFICATION;
import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER;

@Service
public class BPMNAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNAsyncUtil.class);
    public static final String NOTIFICATION_SEPARATOR = ":";

    private final BPMNExecutor bpmnExecutor;
    private final CSRAsyncUtil csrAsyncUtil;
    private final CertificateAsyncUtil certificateAsyncUtil;
    private final CertificateRepository certificateRepository;
    private final CertificateUtil certificateUtil;
    private final CSRRepository csrRepository;
    private final CSRUtil cSRUtil;

    public BPMNAsyncUtil(BPMNExecutor bpmnExecutor,
                         CertificateAsyncUtil certificateAsyncUtil,
                         CertificateRepository certificateRepository,
                         CertificateUtil certificateUtil,
                         CSRAsyncUtil csrAsyncUtil,
                         CSRRepository csrRepository,
                         CSRUtil cSRUtil) {
        this.bpmnExecutor = bpmnExecutor;
        this.csrAsyncUtil = csrAsyncUtil;
        this.certificateAsyncUtil = certificateAsyncUtil;
        this.certificateRepository = certificateRepository;
        this.certificateUtil = certificateUtil;
        this.csrRepository = csrRepository;
        this.cSRUtil = cSRUtil;
    }

    @Async
    @Transactional
    public void onCertificateStatusChange(String processName, Long certificateId, Authentication auth) {

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

    @Async
    @Transactional
    public void onCSRDecisionResult(BPMNProcessInfo processInfo, Long csrId, Authentication auth) {

        LOG.info("******************  Async call to onCSRDecisionResult( '{}', {})", processInfo.getName(), csrId);

        Optional<CSR> optionalCsr = csrRepository.findById(csrId);
        if( optionalCsr.isEmpty()){
            return;
        }

        CSR csr = optionalCsr.get();
        cSRUtil.setCsrAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION, processInfo.getName(), false);
        cSRUtil.setCsrAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER, "0", false);

        processCSRDecisionResult( csr, auth);
    }

    private void executeAfterTransactionCompletes(Certificate certificate) {
        LOG.info( "********* register transaction sync 'afterComplete'");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            public void afterCompletion(int status) {
                LOG.info( "in afterCompletion:  {}", status);
                if( status == TransactionSynchronization.STATUS_COMMITTED){
                    LOG.debug( "in afterCompletion:  drop notification attributes");
                    certificateAsyncUtil.deleteNotificationCounter(certificate);
                }else{
                    LOG.info( "in afterCompletion:  increment notification counter");
                    certificateAsyncUtil.incrementNotificationCounter(certificate);
                }
            }
        });
    }

    private void executeAfterTransactionCompletes(CSR csr) {
        LOG.info( "********* register transaction sync 'afterComplete'");

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            public void afterCompletion(int status) {
                LOG.info( "in afterCompletion:  {}", status);
                if( status == TransactionSynchronization.STATUS_COMMITTED){
                    LOG.debug( "in afterCompletion: drop notification attributes");
                    csrAsyncUtil.deleteNotificationCounter(csr);
                }else{
                    LOG.info( "in afterCompletion:  increment notification counter");
                    csrAsyncUtil.incrementNotificationCounter(csr);
                }
            }
        });
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processChange( Certificate certificate , Authentication auth) {

        LOG.info("******************  processChange({}, {})", certificate.getId(), (auth == null ? null:auth.getName()));

        Authentication effectiveAuth;
        if( auth == null){

            if( certificate.getCsr() != null &&
                certificate.getCsr().getRequestedBy() != null ){
                effectiveAuth = createUserAuthentication(certificate.getCsr().getRequestedBy());
                LOG.info("running processChange as requestedBy user '{}'", effectiveAuth);
            }else {
                effectiveAuth = createUserAuthentication(Constants.SYSTEM_ACCOUNT);
                LOG.info("running processChange as fallback user '{}'", effectiveAuth);
            }
        }else{
            effectiveAuth = auth;
        }

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
        variables.put("certificate", certificate);
        variables.put("certificateId", certificate.getId().toString());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuth = securityContext.getAuthentication();

        try {
            LOG.info("Setting authentication to : {}", effectiveAuth.getName());
            securityContext.setAuthentication(effectiveAuth);

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
            }
        }finally{
            String currentAuthName = currentAuth == null ? null : currentAuth.getName();
            LOG.info("Restoring authentication to : {}", currentAuthName);
            securityContext.setAuthentication(currentAuth);
        }
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processCSRDecisionResult( CSR csr, Authentication auth) {

        LOG.info("******************  processCSRDecisionResult({}, {})", csr.getId(), (auth == null ? null:auth.getName()));

        Authentication effectiveAuth;
        if( auth == null){

            if( csr != null ){
                effectiveAuth = createUserAuthentication(csr.getRequestedBy());
                LOG.info("running processCSRDecisionResult as requestedBy user '{}'", effectiveAuth);
            }else {
                effectiveAuth = createUserAuthentication(Constants.SYSTEM_ACCOUNT);
                LOG.info("running processCSRDecisionResult as fallback user '{}'", effectiveAuth);
            }
        }else{
            effectiveAuth = auth;
        }

        executeAfterTransactionCompletes(csr);

        String processName = cSRUtil.getCSRAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION);
        if( processName == null){
            return;
        }

        String counterString = cSRUtil.getCSRAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER);
        if( counterString == null){
            return;
        }

        int retryCount = Integer.parseInt(counterString);

        CSRDecisionResultInput csrDecisionResultInput = new CSRDecisionResultInput(csr);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuth = securityContext.getAuthentication();

        try {
            LOG.info("Setting authentication to : {}", effectiveAuth.getName());
            securityContext.setAuthentication(effectiveAuth);

            try {
                ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByName(processName,
                    csrDecisionResultInput.getVariables());

                if( processInstance != null) {
                    String status = processInstance.getVariables().get("status").toString();
                    LOG.info("ProcessInstance '{}' terminates with status {}", processInstance.getId(), status);
                }
            } catch (Exception processException) {
                retryCount++;
                String msg = "Exception while calling bpmn process '" + processName + "' fails, incrementing retryCount to " + retryCount;
                LOG.info(msg, processException);
            }
        }finally{
            String currentAuthName = currentAuth == null ? null : currentAuth.getName();
            LOG.info("Restoring authentication to : {}", currentAuthName);
            securityContext.setAuthentication(currentAuth);
        }
    }

    private Authentication createUserAuthentication(String userName) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        return new UsernamePasswordAuthenticationToken(userName, "anonymous", authorities);
    }

}
