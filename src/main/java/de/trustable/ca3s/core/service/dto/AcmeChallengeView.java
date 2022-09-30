package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.ChallengeStatus;

import java.io.Serializable;
import java.time.Instant;

public class AcmeChallengeView implements Serializable {

    private String authorizationType;
    private String authorizationValue;

    private Long challengeId;
    private String type;
    private String value;
    private Instant validated;
    private ChallengeStatus status;

    public String getAuthorizationType() {
        return authorizationType;
    }

    public void setAuthorizationType(String authorizationType) {
        this.authorizationType = authorizationType;
    }

    public String getAuthorizationValue() {
        return authorizationValue;
    }

    public void setAuthorizationValue(String authorizationValue) {
        this.authorizationValue = authorizationValue;
    }

    public Long getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Long challengeId) {
        this.challengeId = challengeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Instant getValidated() {
        return validated;
    }

    public void setValidated(Instant validated) {
        this.validated = validated;
    }

    public ChallengeStatus getStatus() {
        return status;
    }

    public void setStatus(ChallengeStatus status) {
        this.status = status;
    }
}
