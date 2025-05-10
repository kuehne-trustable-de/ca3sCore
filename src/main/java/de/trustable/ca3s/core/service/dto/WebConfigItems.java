package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class WebConfigItems implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = -3033819425667579550L;

    private String additionalEMailRecipients = "";

    private boolean notifyRAOfficerOnPendingRequest = false;

    private String processInfoNameRequestAuthorization;

    private Boolean issuesSecondFactorClientCert;


    public WebConfigItems(){}

    public String getAdditionalEMailRecipients() {
        return additionalEMailRecipients;
    }

    public void setAdditionalEMailRecipients(String additionalEMailRecipients) {
        this.additionalEMailRecipients = additionalEMailRecipients;
    }

    public boolean isNotifyRAOfficerOnPendingRequest() {
        return notifyRAOfficerOnPendingRequest;
    }

    public void setNotifyRAOfficerOnPendingRequest(boolean notifyRAOfficerOnPendingRequest) {
        this.notifyRAOfficerOnPendingRequest = notifyRAOfficerOnPendingRequest;
    }

    public String getProcessInfoNameRequestAuthorization() {
        return processInfoNameRequestAuthorization;
    }

    public void setProcessInfoNameRequestAuthorization(String processInfoNameRequestAuthorization) {
        this.processInfoNameRequestAuthorization = processInfoNameRequestAuthorization;
    }

    public Boolean getIssuesSecondFactorClientCert() {
        return issuesSecondFactorClientCert;
    }

    public void setIssuesSecondFactorClientCert(Boolean issuesSecondFactorClientCert) {
        this.issuesSecondFactorClientCert = issuesSecondFactorClientCert;
    }

}
