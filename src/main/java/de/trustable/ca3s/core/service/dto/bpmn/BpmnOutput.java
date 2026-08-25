package de.trustable.ca3s.core.service.dto.bpmn;

import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;

import java.util.Collections;
import java.util.Map;

public class BpmnOutput {

    final private String status;
    final private String failureReason;
    private String processInstanceId = "";

    public BpmnOutput(){
        this.status = BpmnInput.FAILED;
        this.failureReason = "";
    }

    public BpmnOutput( ProcessInstanceWithVariables processInstance){
        this(processInstance == null ? (Map<String, Object>)Collections.EMPTY_MAP :  processInstance.getVariables());
        this.processInstanceId = processInstance.getProcessInstanceId();
    }

    public BpmnOutput(Map<String, Object> variables){
        this.status = variables.get("status").toString();
        this.failureReason =  variables.get("failureReason").toString();
    }

    public BpmnOutput(Exception e) {
        this.status = BpmnInput.FAILED;
        this.failureReason = e.getLocalizedMessage();
    }

    public String getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }
}
