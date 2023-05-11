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

    private String caNameCAA;

    private String processInfoNameAccountValidation;
    private String processInfoNameOrderValidation;
    private String processInfoNameChallengeValidation;

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

	public String getProcessInfoNameAccountValidation() {
		return processInfoNameAccountValidation;
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

	public void setProcessInfoNameAccountValidation(String processInfoNameAccountValidation) {
		this.processInfoNameAccountValidation = processInfoNameAccountValidation;
	}

	public void setProcessInfoNameOrderValidation(String processInfoNameOrderValidation) {
		this.processInfoNameOrderValidation = processInfoNameOrderValidation;
	}

	public void setProcessInfoNameChallengeValidation(String processInfoNameChallengeValidation) {
		this.processInfoNameChallengeValidation = processInfoNameChallengeValidation;
	}

}
