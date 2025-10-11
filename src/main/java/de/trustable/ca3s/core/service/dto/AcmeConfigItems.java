package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class AcmeConfigItems implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -4813895760525985553L;

    private boolean allowChallengeHTTP01;
    private boolean allowChallengeAlpn;
	private boolean allowChallengeDNS;
	private boolean allowWildcards;
    private boolean checkCAA;
    private boolean notifyContactsOnError;
    private String contactEMailRegEx;
    private String contactEMailRejectRegEx;
    private String caNameCAA;
    private String processInfoNameAccountAuthorization;
    private String processInfoNameOrderValidation;
    private String processInfoNameChallengeValidation;
    private boolean externalAccountRequired;


    public AcmeConfigItems() {}

	public boolean isAllowChallengeHTTP01() {
		return allowChallengeHTTP01;
	}

	public boolean isAllowChallengeDNS() {
		return allowChallengeDNS;
	}

	public boolean isAllowWildcards() {
		return allowWildcards;
	}

	public boolean isCheckCAA() {
		return checkCAA;
	}

	public String getCaNameCAA() {
		return caNameCAA;
	}

    public boolean isNotifyContactsOnError() {
        return notifyContactsOnError;
    }

    public void setNotifyContactsOnError(boolean notifyContactsOnError) {
        this.notifyContactsOnError = notifyContactsOnError;
    }

    public String getProcessInfoNameAccountAuthorization() {
		return processInfoNameAccountAuthorization;
	}

	public String getProcessInfoNameOrderValidation() {
		return processInfoNameOrderValidation;
	}

	public String getProcessInfoNameChallengeValidation() {
		return processInfoNameChallengeValidation;
	}

    public boolean isAllowChallengeAlpn() {
        return allowChallengeAlpn;
    }

    public void setAllowChallengeAlpn(boolean allowChallengeAlpn) {
        this.allowChallengeAlpn = allowChallengeAlpn;
    }


    public void setAllowChallengeHTTP01(boolean allowChallengeHTTP01) {
		this.allowChallengeHTTP01 = allowChallengeHTTP01;
	}

	public void setAllowChallengeDNS(boolean allowChallengeDNS) {
		this.allowChallengeDNS = allowChallengeDNS;
	}

	public void setAllowWildcards(boolean allowWildcards) {
		this.allowWildcards = allowWildcards;
	}

	public void setCheckCAA(boolean checkCAA) {
		this.checkCAA = checkCAA;
	}

	public void setCaNameCAA(String caNameCAA) {
		this.caNameCAA = caNameCAA;
	}

	public void setProcessInfoNameAccountAuthorization(String processInfoNameAccountAuthorization) {
		this.processInfoNameAccountAuthorization = processInfoNameAccountAuthorization;
	}

	public void setProcessInfoNameOrderValidation(String processInfoNameOrderValidation) {
		this.processInfoNameOrderValidation = processInfoNameOrderValidation;
	}

	public void setProcessInfoNameChallengeValidation(String processInfoNameChallengeValidation) {
		this.processInfoNameChallengeValidation = processInfoNameChallengeValidation;
	}

    public String getContactEMailRegEx() {
        return contactEMailRegEx;
    }

    public void setContactEMailRegEx(String contactEMailRegEx) {
        this.contactEMailRegEx = contactEMailRegEx;
    }

    public String getContactEMailRejectRegEx() {
        return contactEMailRejectRegEx;
    }

    public void setContactEMailRejectRegEx(String contactEMailRejectRegEx) {
        this.contactEMailRejectRegEx = contactEMailRejectRegEx;
    }

    public boolean isExternalAccountRequired() {
        return externalAccountRequired;
    }

    public void setExternalAccountRequired(boolean externalAccountRequired) {
        this.externalAccountRequired = externalAccountRequired;
    }
}
