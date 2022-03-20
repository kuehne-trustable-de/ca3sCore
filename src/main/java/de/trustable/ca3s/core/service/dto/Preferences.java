package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class Preferences implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3936765448876549181L;

	private boolean serverSideKeyCreationAllowed = false;

	private boolean checkCRL = false;

    private long maxNextUpdatePeriodCRLHour = 24L;

    private long acmeHTTP01TimeoutMilliSec = 2L * 1000L;

    private int[] acmeHTTP01CallbackPortArr = {5544,8080,80};

    private String[] selectedHashes = {"sha256", "sha512"};
    private String[] selectedSigningAlgos = {"rsa3072","rsa4096", "rsa8192"};


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

	public boolean isCheckCRL() {
		return checkCRL;
	}

	public void setCheckCRL(boolean checkCRL) {
		this.checkCRL = checkCRL;
	}

    public int[] getAcmeHTTP01CallbackPortArr() {
        return acmeHTTP01CallbackPortArr;
    }

    public void setAcmeHTTP01CallbackPortArr(int[] acmeHTTP01CallbackPortArr) {
        this.acmeHTTP01CallbackPortArr = acmeHTTP01CallbackPortArr;
    }

    public long getMaxNextUpdatePeriodCRLHour() {
        return maxNextUpdatePeriodCRLHour;
    }

    public void setMaxNextUpdatePeriodCRLHour(long maxNextUpdatePeriodCRLHour) {
        this.maxNextUpdatePeriodCRLHour = maxNextUpdatePeriodCRLHour;
    }

    public String[] getSelectedHashes() {
        return selectedHashes;
    }

    public void setSelectedHashes(String[] selectedHashes) {
        this.selectedHashes = selectedHashes;
    }

    public String[] getSelectedSigningAlgos() {
        return selectedSigningAlgos;
    }

    public void setSelectedSigningAlgos(String[] selectedSigningAlgos) {
        this.selectedSigningAlgos = selectedSigningAlgos;
    }
}
