package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CertificateAdministrationData implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -106242086994826660L;

	@NotNull
	@JsonProperty("certificateId")
	private Long certificateId;

	@JsonProperty("revocationReason")
	private String revocationReason;

	@JsonProperty("comment")
	private String comment;

    @NotNull
    @JsonProperty("administrationType")
    private AdministrationType administrationType;

    @JsonProperty("arAttributes")
    private NamedValue[] arAttributeArr;


    public Long getCertificateId() {
		return certificateId;
	}


	public String getComment() {
		return comment;
	}

	public void setCertificateId(Long certificateId) {
		this.certificateId = certificateId;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}


	public String getRevocationReason() {
		return revocationReason;
	}


	public void setRevocationReason(String revovationReason) {
		this.revocationReason = revovationReason;
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
