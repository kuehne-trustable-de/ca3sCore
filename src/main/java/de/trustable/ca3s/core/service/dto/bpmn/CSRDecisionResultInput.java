package de.trustable.ca3s.core.service.dto.bpmn;

import de.trustable.ca3s.core.domain.CSR;

import java.util.Map;

public class CSRDecisionResultInput extends BpmnInput {

    final private CSR csr;

    public CSRDecisionResultInput(CSR csr) {
        super("CSRDecisionResult");
        this.csr = csr;
    }

    protected void addVariables(Map<String, Object> variables) {
        variables.put("csr", csr);
    }

    public CSR getCsr() {
        return csr;
    }
}
