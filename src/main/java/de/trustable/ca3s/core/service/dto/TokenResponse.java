package de.trustable.ca3s.core.service.dto;

public class TokenResponse {

    private AccountCredentialsType credentialType;
    private String tokenValue;
    private long validitySeconds;
    private String eabKid;
    private String jws;

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

    public String getJws() {
        return jws;
    }

    public void setJws(String jws) {
        this.jws = jws;
    }
}
