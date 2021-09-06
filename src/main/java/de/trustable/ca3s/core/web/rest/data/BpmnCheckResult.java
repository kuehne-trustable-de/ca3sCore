package de.trustable.ca3s.core.web.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BpmnCheckResult implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 56333245580621L;

	@JsonProperty("failureReason")
	private String failureReason;

    @JsonProperty("isActive")
    private Boolean isActive;

    @JsonProperty("status")
    private String status;

    @JsonProperty("csrAttributes")
    private  List<Pair<String, Object>> csrAttributes = new ArrayList<>();


    public BpmnCheckResult() {
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Pair<String, Object>> getCsrAttributes() {
        return csrAttributes;
    }

    public void setCsrAttributes(List<Pair<String, Object>> csrAttributes) {
        this.csrAttributes = csrAttributes;
    }
}
