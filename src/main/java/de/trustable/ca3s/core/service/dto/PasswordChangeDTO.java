package de.trustable.ca3s.core.service.dto;

/**
 * A DTO representing a password change required data - current and new password.
 */
public class PasswordChangeDTO {

    private CredentialUpdateType credentialUpdateType;
    private String currentPassword;
    private String newPassword;
    private String seed;
    private String otpTestValue;
    private String apiTokenValue;
    private long apiTokenValiditySeconds;
    private Long clientAuthCertId;
    private Long pipelineId;
    private String eabKid;

    private boolean secondFactorRequired;

    public PasswordChangeDTO() {
        // Empty constructor needed for Jackson.
    }

    public PasswordChangeDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getSeed() {
        return seed;
    }

    public void setSeed(String seed) {
        this.seed = seed;
    }

    public Long getClientAuthCertId() {
        return clientAuthCertId;
    }

    public void setClientAuthCertId(Long clientAuthCertId) {
        this.clientAuthCertId = clientAuthCertId;
    }

    public String getOtpTestValue() {
        return otpTestValue;
    }

    public void setOtpTestValue(String otpTestValue) {
        this.otpTestValue = otpTestValue;
    }

    public String getApiTokenValue() {
        return apiTokenValue;
    }

    public long getApiTokenValiditySeconds() {
        return apiTokenValiditySeconds;
    }

    public void setApiTokenValiditySeconds(long apiTokenValiditySeconds) {
        this.apiTokenValiditySeconds = apiTokenValiditySeconds;
    }

    public void setApiTokenValue(String apiTokenValue) {
        this.apiTokenValue = apiTokenValue;
    }

    public CredentialUpdateType getCredentialUpdateType() {
        return credentialUpdateType;
    }

    public void setCredentialUpdateType(CredentialUpdateType credentialUpdateType) {
        this.credentialUpdateType = credentialUpdateType;
    }

    public boolean isSecondFactorRequired() {
        return secondFactorRequired;
    }

    public void setSecondFactorRequired(boolean secondFactorRequired) {
        this.secondFactorRequired = secondFactorRequired;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getEabKid() {
        return eabKid;
    }

    public void setEabKid(String eabKid) {
        this.eabKid = eabKid;
    }
}
