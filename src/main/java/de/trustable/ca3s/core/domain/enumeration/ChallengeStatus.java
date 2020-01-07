package de.trustable.ca3s.core.domain.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The ChallengeStatus enumeration.
 */
public enum ChallengeStatus {
    PENDING, VALID, INVALID, DEACTIVATED, EXPIRED, REVOKED;
    
	@JsonValue
    public String getValue() {
        return this.toString().toLowerCase();
    }

}
