package de.trustable.ca3s.core.service.dto;

import java.time.Instant;

public class AuthenticationParameter {

    private KDFType kdfType;

    private String plainSecret;

    private Instant secretValidTo;

    private String salt;

    private long cycles;

    private String apiKeySalt;

    private long apiKeyCycles;

    public KDFType getKdfType() {
        return kdfType;
    }

    public void setKdfType(KDFType kdfType) {
        this.kdfType = kdfType;
    }

    public String getPlainSecret() {
        return plainSecret;
    }

    public void setPlainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
    }

    public Instant getSecretValidTo() {
        return secretValidTo;
    }

    public void setSecretValidTo(Instant secretValidTo) {
        this.secretValidTo = secretValidTo;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public long getCycles() {
        return cycles;
    }

    public void setCycles(long cycles) {
        this.cycles = cycles;
    }

    public String getApiKeySalt() {
        return apiKeySalt;
    }

    public void setApiKeySalt(String apiKeySalt) {
        this.apiKeySalt = apiKeySalt;
    }

    public long getApiKeyCycles() {
        return apiKeyCycles;
    }

    public void setApiKeyCycles(long apiKeyCycles) {
        this.apiKeyCycles = apiKeyCycles;
    }
}
