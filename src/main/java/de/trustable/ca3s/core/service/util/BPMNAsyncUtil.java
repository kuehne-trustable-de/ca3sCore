package de.trustable.ca3s.core.service.util;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class BPMNAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNAsyncUtil.class);

    private final RuntimeService runtimeService;

    public BPMNAsyncUtil(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Async
    @Transactional
    public void onChange(String processName, Long certificateId) {

        LOG.info("Async call to onChange( '{}', {})", processName, certificateId);

        if( certificateId == null){
            return;
        }

        Map<String, Object> variables = new HashMap<>();

        variables.put("certificateId", certificateId.toString());

        try {
            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceById(processName).setVariables(variables).executeWithVariablesInReturn();
            String processInstanceId = processInstance.getId();
            LOG.info("ProcessInstance: {}", processInstanceId);

            String status = processInstance.getVariables().get("status").toString();

        }catch(RuntimeException processException){
            if(LOG.isDebugEnabled()){
                LOG.debug("Exception while calling bpmn process '"+processName+"'", processException);
            }
        }
    }
}
