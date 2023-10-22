package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;

import java.io.Serializable;
import java.time.Instant;

public class AcmeOrderView implements Serializable {

    private Long id;
    private String orderId;
    private AcmeOrderStatus status;
    private String realm;
    private AcmeChallengeView[] challenges;
    private String challengeTypes;
    private String challengeUrls;

    private Boolean wildcard;
    private Instant createdOn;
    private Instant expires;
    private Instant notBefore;
    private Instant notAfter;
    private String error;
    private String finalizeUrl;
    private String certificateUrl;

//    private Set<AcmeAuthorization> acmeAuthorizations = new HashSet<>();
//    private Set<AcmeIdentifier> acmeIdentifiers = new HashSet<>();

    private Long csrId;
    private Long certificateId;
    private String accountId;

    public AcmeOrderView() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AcmeOrderStatus getStatus() {
        return status;
    }

    public void setStatus(AcmeOrderStatus status) {
        this.status = status;
    }

    public Boolean getWildcard() {
        return wildcard;
    }

    public void setWildcard(Boolean wildcard) {
        this.wildcard = wildcard;
    }

    public Instant getExpires() {
        return expires;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    public Instant getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Instant notBefore) {
        this.notBefore = notBefore;
    }

    public Instant getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Instant notAfter) {
        this.notAfter = notAfter;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getFinalizeUrl() {
        return finalizeUrl;
    }

    public void setFinalizeUrl(String finalizeUrl) {
        this.finalizeUrl = finalizeUrl;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public Long getCsrId() {
        return csrId;
    }

    public void setCsrId(Long csrId) {
        this.csrId = csrId;
    }

    public Long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Long certificateId) {
        this.certificateId = certificateId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public AcmeChallengeView[] getChallenges() {
        return challenges;
    }

    public void setChallenges(AcmeChallengeView[] challenges) {
        this.challenges = challenges;
    }

    public String getChallengeTypes() {
        return challengeTypes;
    }

    public void setChallengeTypes(String challengeTypes) {
        this.challengeTypes = challengeTypes;
    }

    public String getChallengeUrls() {
        return challengeUrls;
    }

    public void setChallengeUrls(String challengeUrls) {
        this.challengeUrls = challengeUrls;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }
}
