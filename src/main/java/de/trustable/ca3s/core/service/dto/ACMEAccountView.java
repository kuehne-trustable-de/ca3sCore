package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.AccountStatus;

import java.io.Serializable;
import java.time.Instant;

public class ACMEAccountView implements Serializable {

    private Long id;
    private Long accountId;
    private String realm;
    private Instant createdOn;
    private AccountStatus status;
    private Boolean termsOfServiceAgreed;
    private String publicKeyHash;
    private String publicKey;
    private String[] contactUrls;
    private Long orderCount;

    public ACMEAccountView() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Boolean getTermsOfServiceAgreed() {
        return termsOfServiceAgreed;
    }

    public void setTermsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public String[] getContactUrls() {
        return contactUrls;
    }

    public void setContactUrls(String[] contactUrls) {
        this.contactUrls = contactUrls;
    }

    public Long getOrderCount() {
        return orderCount;
    }
    public void setOrderCount(Long orderCount) {
        this.orderCount = orderCount;
    }
}
