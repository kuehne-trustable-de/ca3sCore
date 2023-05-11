package de.trustable.ca3s.core.web.rest.data;

import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;

import java.io.Serializable;

/**
 * A AcmeChallengeValidation object.
 */

public class AcmeChallengeValidation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long challengeId;
    private Long requestProxyConfigId;

    private String[] responses = new String[0];
    private String error;
    private ChallengeStatus status;


    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public Long getRequestProxyConfigId() {
        return requestProxyConfigId;
    }

    public void setRequestProxyConfigId(Long requestProxyConfigId) {
        this.requestProxyConfigId = requestProxyConfigId;
    }

    public String[] getResponses() {
        return responses;
    }

    public void setResponses(String[] responses) {
        this.responses = responses;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }

    // prettier-ignore
    @Override
    public String toString() {
        String result =  "AcmeChallenge{" +
            ", challengeId=" + getChallengeId() +
            ", requestProxyConfigId=" + getRequestProxyConfigId();
        if( getResponses() == null){
            result += ", responses=''";
        }else {
            result += ", responses='" + String.join(",", getResponses()) + "'";
        }
        result += ", error='" + getError() + "'" +
            ", status='" + getStatus() + "'" +
            "}";

        return result;
    }

}
