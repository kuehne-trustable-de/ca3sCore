package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;

import java.io.Serializable;
import java.time.Instant;

public class ACMEOrderView implements Serializable {

    private Long id;
    private Long orderId;
    private AcmeOrderStatus status;
    private String realm;
    private ACMEChallengeView[] challenges;
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
    private Long accountId;

    public ACMEOrderView() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public AcmeOrderStatus getStatus() {
        return status;
    }

    public void setStatus(AcmeOrderStatus status) {
        this.status = status;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public ACMEChallengeView[] getChallenges() {
        return challenges;
    }

    public void setChallenges(ACMEChallengeView[] challenges) {
        this.challenges = challenges;
    }
}
