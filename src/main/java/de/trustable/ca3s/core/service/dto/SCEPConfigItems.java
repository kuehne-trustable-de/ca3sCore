package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;
import java.time.Instant;

public class SCEPConfigItems implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7551079211047239363L;

	private boolean capabilityRenewal = true;
	private boolean capabilityPostPKIOperation = true;

    private String scepSecretPCId = null;
    private String scepSecret = "true";
    private Instant scepSecretValidTo = Instant.now();

    public SCEPConfigItems() {}

	public boolean isCapabilityRenewal() {
		return capabilityRenewal;
	}

	public boolean isCapabilityPostPKIOperation() {
		return capabilityPostPKIOperation;
	}

	public void setCapabilityRenewal(boolean capabilityRenewal) {
		this.capabilityRenewal = capabilityRenewal;
	}

	public void setCapabilityPostPKIOperation(boolean capabilityPostPKIOperation) {
		this.capabilityPostPKIOperation = capabilityPostPKIOperation;
	}

    public String getScepSecretPCId() {
        return scepSecretPCId;
    }

    public void setScepSecretPCId(String scepSecretPCId) {
        this.scepSecretPCId = scepSecretPCId;
    }

    public String getScepSecret() {
        return scepSecret;
    }

    public void setScepSecret(String scepSecret) {
        this.scepSecret = scepSecret;
    }

    public Instant getScepSecretValidTo() {
        return scepSecretValidTo;
    }

    public void setScepSecretValidTo(Instant scepSecretValidTo) {
        this.scepSecretValidTo = scepSecretValidTo;
    }
}
