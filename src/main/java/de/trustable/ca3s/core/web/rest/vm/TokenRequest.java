package de.trustable.ca3s.core.web.rest.vm;

import de.trustable.ca3s.core.service.dto.AccountCredentialsType;

public class TokenRequest {

    private AccountCredentialsType credentialType;
    private long validitySeconds;

    public AccountCredentialsType getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(AccountCredentialsType credentialType) {
        this.credentialType = credentialType;
    }

    public long getValiditySeconds() {
        return validitySeconds;
    }

    public void setValiditySeconds(long validitySeconds) {
        this.validitySeconds = validitySeconds;
    }
}
