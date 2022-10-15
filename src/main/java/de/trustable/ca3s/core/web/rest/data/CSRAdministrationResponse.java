package de.trustable.ca3s.core.web.rest.data;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class CSRAdministrationResponse implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -10605276485876560L;

    @JsonProperty("csrId")
    private Long csrId;

    @JsonProperty("certId")
    private Long certId;

    @NotNull
	@JsonProperty("administrationType")
	private AdministrationType administrationType;

	@JsonProperty("problemOccured")
	private String problemOccured;

    public Long getCsrId() {
        return csrId;
    }

    public void setCsrId(Long csrId) {
        this.csrId = csrId;
    }

    public Long getCertId() {
        return certId;
    }

    public void setCertId(Long certId) {
        this.certId = certId;
    }

    public AdministrationType getAdministrationType() {
        return administrationType;
    }

    public void setAdministrationType(AdministrationType administrationType) {
        this.administrationType = administrationType;
    }

    public String getProblemOccured() {
        return problemOccured;
    }

    public void setProblemOccured(String problemOccured) {
        this.problemOccured = problemOccured;
    }
}
