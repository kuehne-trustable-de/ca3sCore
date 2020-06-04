package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class SCEPConfigItems implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7551079211047239363L;

	private boolean capabilityRenewal = true;
	private boolean capabilityPostPKIOperation = true;
	
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

	
	
}
