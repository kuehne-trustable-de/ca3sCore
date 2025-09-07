package de.trustable.ca3s.core.service.dto.bpmn;

import java.util.HashMap;
import java.util.Map;

public class BpmnInput {

    public static final String FAILED = "Failed";
    public static final String SUCCESS = "Success";

    final private String action;
    final private String status;
    final private String failureReason;

    public BpmnInput(String action) {
        this.action = action;
        this.status = FAILED;
        this.failureReason = "";
    }

    public Map<String, Object> getVariables(){
        Map<String, Object> variables = new HashMap<>();
        variables.put("action", action);
        variables.put("failureReason", failureReason);
        variables.put("status", status);

        addVariables(variables);

        return variables;
    }

    protected void addVariables(Map<String, Object> variables) {
    }

    public String getAction() {
        return action;
    }

    public String getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
