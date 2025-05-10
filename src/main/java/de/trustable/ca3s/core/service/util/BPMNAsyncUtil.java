package de.trustable.ca3s.core.service.util;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class BPMNAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNAsyncUtil.class);

    private final BPMNExecutor bpmnExecutor;

    public BPMNAsyncUtil(BPMNExecutor bpmnExecutor) {
        this.bpmnExecutor = bpmnExecutor;
    }

    @Async
    @Transactional
    public void onChange(String processName, Long certificateId, Authentication auth) {

        LOG.info("******************  Async call to onChange( '{}', {})", processName, certificateId);

        if( certificateId == null){
            return;
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("certificateId", certificateId.toString());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication currentAuth = securityContext.getAuthentication();

        try {
            if( auth != null){
                LOG.info("Setting authentication to : {}", auth.getName());
                securityContext.setAuthentication(auth);
            }

            for( int retryCounter = 0; retryCounter < 3; retryCounter ++) {
                try {
                    ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByName(processName, variables);
                    String status = processInstance.getVariables().get("status").toString();
                    LOG.info("ProcessInstance '{}' terminates with status {}", processInstance.getId(), status);
                    break;
                } catch (RuntimeException processException) {
                    String msg = "Exception while calling bpmn process '" + processName + "', retry: " + retryCounter;
                    LOG.debug(msg, processException);
                }
            }
        }finally{
            String currentAuthName = currentAuth == null ? null : currentAuth.getName();
            LOG.info("Restoring authentication to : {}", currentAuthName);
            securityContext.setAuthentication(currentAuth);
        }
    }
}
