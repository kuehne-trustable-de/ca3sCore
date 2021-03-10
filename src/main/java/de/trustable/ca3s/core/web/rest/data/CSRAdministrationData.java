package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CSRAdministrationData implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -1062400568494856660L;

	@NotNull
	@JsonProperty("csrId")
	private Long csrId;

	@NotNull
	@JsonProperty("administrationType")
	private AdministrationType administrationType;

	@JsonProperty("rejectionReason")
	private String rejectionReason;

	@JsonProperty("comment")
	private String comment;

    @JsonProperty("arAttributes")
	private NamedValue[] arAttributeArr;

	public Long getCsrId() {
		return csrId;
	}

	public String getRejectionReason() {
		return rejectionReason;
	}

	public String getComment() {
		return comment;
	}

	public void setCsrId(Long csrId) {
		this.csrId = csrId;
	}

	public void setRejectionReason(String rejectionReason) {
		this.rejectionReason = rejectionReason;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public AdministrationType getAdministrationType() {
		return administrationType;
	}

	public void setAdministrationType(AdministrationType administrationType) {
		this.administrationType = administrationType;
	}

    public NamedValue[] getArAttributeArr() {
        return arAttributeArr;
    }

    public void setArAttributeArr(NamedValue[] arAttributeArr) {
        this.arAttributeArr = arAttributeArr;
    }
}
