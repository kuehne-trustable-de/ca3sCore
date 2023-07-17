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

    private String recepientCertSubject = "";
    private String recepientCertSerial = "1";
    private Long   recepientCertId = 0L;

    private String scepSecretPCId = null;
    private String scepSecret = "******";
    private Instant scepSecretValidTo = Instant.now();

    private KeyAlgoLengthOrSpec keyAlgoLength = KeyAlgoLengthOrSpec.RSA_2048;
    private String scepRecipientDN = "";
    private String caConnectorRecipientName = "";

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

    public String getRecepientCertSubject() {
        return recepientCertSubject;
    }

    public void setRecepientCertSubject(String recepientCertSubject) {
        this.recepientCertSubject = recepientCertSubject;
    }

    public String getRecepientCertSerial() {
        return recepientCertSerial;
    }

    public void setRecepientCertSerial(String recepientCertSerial) {
        this.recepientCertSerial = recepientCertSerial;
    }

    public Long getRecepientCertId() {
        return recepientCertId;
    }

    public void setRecepientCertId(Long recepientCertId) {
        this.recepientCertId = recepientCertId;
    }

    public KeyAlgoLengthOrSpec getKeyAlgoLength() {
        return keyAlgoLength;
    }

    public void setKeyAlgoLength(KeyAlgoLengthOrSpec keyAlgoLength) {
        this.keyAlgoLength = keyAlgoLength;
    }

    public String getScepRecipientDN() {
        return scepRecipientDN;
    }

    public void setScepRecipientDN(String scepRecipientDN) {
        this.scepRecipientDN = scepRecipientDN;
    }

    public String getCaConnectorRecipientName() {
        return caConnectorRecipientName;
    }

    public void setCaConnectorRecipientName(String caConnectorRecipientName) {
        this.caConnectorRecipientName = caConnectorRecipientName;
    }
}
