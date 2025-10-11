package de.trustable.ca3s.core.web.rest.vm;

import de.trustable.ca3s.core.service.dto.AccountCredentialsType;

public class TokenResponse {

    private AccountCredentialsType credentialType;
    private String tokenValue;
    private long validitySeconds;
    private String eabKid;

    public AccountCredentialsType getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(AccountCredentialsType credentialType) {
        this.credentialType = credentialType;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public long getValiditySeconds() {
        return validitySeconds;
    }

    public void setValiditySeconds(long validitySeconds) {
        this.validitySeconds = validitySeconds;
    }

    public String getEabKid() {
        return eabKid;
    }

    public void setEabKid(String eabKid) {
        this.eabKid = eabKid;
    }
}
