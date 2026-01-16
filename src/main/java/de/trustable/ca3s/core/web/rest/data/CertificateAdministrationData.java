package de.trustable.ca3s.core.web.rest.data;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.trustable.ca3s.core.service.dto.NamedValue;

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

    @NotNull
    @JsonProperty("trusted")
    private Boolean trusted;

    @NotNull
    @JsonProperty("notificationBlocked")
    private Boolean notificationBlocked;

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

    public Boolean getTrusted() {
        return trusted;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public Boolean getNotificationBlocked() {
        return notificationBlocked;
    }

    public void setNotificationBlocked(Boolean notificationBlocked) {
        this.notificationBlocked = notificationBlocked;
    }

    public NamedValue[] getArAttributeArr() {
        return arAttributeArr;
    }

    public void setArAttributeArr(NamedValue[] arAttributeArr) {
        this.arAttributeArr = arAttributeArr;
    }
}
