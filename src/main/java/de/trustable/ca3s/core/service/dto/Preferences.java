package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class Preferences implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3936765448876549181L;

	private boolean serverSideKeyCreationAllowed = false;
	
	private boolean checkCRL = false;
	
	private long acmeHTTP01TimeoutMilliSec = 2L * 1000L;
	
	private String acmeHTTP01CallbackPorts = "5544,8080,80";
	
	public Preferences() {}

	public boolean isServerSideKeyCreationAllowed() {
		return serverSideKeyCreationAllowed;
	}

	public void setServerSideKeyCreationAllowed(boolean serverSideKeyCreationAllowed) {
		this.serverSideKeyCreationAllowed = serverSideKeyCreationAllowed;
	}

	public long getAcmeHTTP01TimeoutMilliSec() {
		return acmeHTTP01TimeoutMilliSec;
	}

	public void setAcmeHTTP01TimeoutMilliSec(long acmeHTTP01TimeoutMilliSec) {
		this.acmeHTTP01TimeoutMilliSec = acmeHTTP01TimeoutMilliSec;
	}

	public String getAcmeHTTP01CallbackPorts() {
		return acmeHTTP01CallbackPorts;
	}

	public void setAcmeHTTP01CallbackPorts(String acmeHTTP01CallbackPorts) {
		this.acmeHTTP01CallbackPorts = acmeHTTP01CallbackPorts;
	}

	public boolean isCheckCRL() {
		return checkCRL;
	}

	public void setCheckCRL(boolean checkCRL) {
		this.checkCRL = checkCRL;
	}

	
}
