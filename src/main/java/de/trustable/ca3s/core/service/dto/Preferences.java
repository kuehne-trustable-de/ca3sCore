package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class Preferences implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3936765448876549181L;

	private boolean serverSideKeyCreationAllowed = false;

    private int deleteKeyAfterDays = 5;
    private int deleteKeyAfterUses = 10;

    private boolean checkCRL = false;

    private boolean notifyRAOnRequest = false;

    private long maxNextUpdatePeriodCRLHour = 24L;

    private long acmeHTTP01TimeoutMilliSec = 2L * 1000L;

    private int[] acmeHTTP01CallbackPortArr = {5544,8080,80};

    private String[] availableHashes = {"sha-256", "sha-512"};
    private String[] availableSigningAlgos = {"rsa-2048","rsa-3072","rsa-4096", "rsa-8192"};

    private String[] selectedHashes = {"sha-256", "sha-512"};
    private String[] selectedSigningAlgos = {"rsa-2048","rsa-3072","rsa-4096", "rsa-8192"};

    private boolean authClientCert = false;
    private boolean authTotp = false;
    private boolean authEmail = false;
    private boolean sms = false;

    private boolean authClientCertEnabled = false;
    private boolean smsEnabled = false;

    public Preferences() {}

	public boolean isServerSideKeyCreationAllowed() {
		return serverSideKeyCreationAllowed;
	}

	public void setServerSideKeyCreationAllowed(boolean serverSideKeyCreationAllowed) {
		this.serverSideKeyCreationAllowed = serverSideKeyCreationAllowed;
	}

    public int getDeleteKeyAfterDays() {
        return deleteKeyAfterDays;
    }

    public void setDeleteKeyAfterDays(int deleteKeyAfterDays) {
        this.deleteKeyAfterDays = deleteKeyAfterDays;
    }

    public int getDeleteKeyAfterUses() {
        return deleteKeyAfterUses;
    }

    public void setDeleteKeyAfterUses(int deleteKeyAfterUses) {
        this.deleteKeyAfterUses = deleteKeyAfterUses;
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

    public String[] getAvailableHashes() {
        return availableHashes;
    }

    public void setAvailableHashes(String[] availableHashes) {
        this.availableHashes = availableHashes;
    }

    public String[] getAvailableSigningAlgos() {
        return availableSigningAlgos;
    }

    public void setAvailableSigningAlgos(String[] availableSigningAlgos) {
        this.availableSigningAlgos = availableSigningAlgos;
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

    public boolean isNotifyRAOnRequest() {
        return notifyRAOnRequest;
    }

    public void setNotifyRAOnRequest(boolean notifyRAOnRequest) {
        this.notifyRAOnRequest = notifyRAOnRequest;
    }

    public boolean isAuthClientCert() {
        return authClientCert;
    }

    public void setAuthClientCert(boolean authClientCert) {
        this.authClientCert = authClientCert;
    }

    public boolean isAuthTotp() {
        return authTotp;
    }

    public void setAuthTotp(boolean authTotp) {
        this.authTotp = authTotp;
    }

    public boolean isAuthEmail() {
        return authEmail;
    }

    public void setAuthEmail(boolean authEmail) {
        this.authEmail = authEmail;
    }

    public boolean isSms() {
        return sms;
    }

    public void setSms(boolean sms) {
        this.sms = sms;
    }

    public boolean isAuthClientCertEnabled() {
        return authClientCertEnabled;
    }

    public void setAuthClientCertEnabled(boolean authClientCertEnabled) {
        this.authClientCertEnabled = authClientCertEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
}
