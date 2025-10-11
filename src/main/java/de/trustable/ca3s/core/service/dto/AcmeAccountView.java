package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;

import java.io.Serializable;
import java.time.Instant;

public class AcmeAccountView implements Serializable {

    private Long id;
    private Long accountId;
    private String eabKid;
    private User eabUser;

    private String realm;

    @JsonFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdOn;

    private AccountStatus status;
    private Boolean termsOfServiceAgreed;
    private String publicKeyHash;
    private String publicKey;

    private String keyLength;
    private String keyAlgorithm;
    private String altKeyAlgorithm;

    private String[] contactUrls;
    private Long orderCount;

    public AcmeAccountView() {}

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

    public String getEabKid() {
        return eabKid;
    }

    public void setEabKid(String eabKid) {
        this.eabKid = eabKid;
    }

    public User getEabUser() {
        return eabUser;
    }

    public void setEabUser(User eabUser) {
        this.eabUser = eabUser;
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

    public String getKeyLength() {
        return keyLength;
    }

    public void setKeyLength(String keyLength) {
        this.keyLength = keyLength;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getAltKeyAlgorithm() {
        return altKeyAlgorithm;
    }

    public void setAltKeyAlgorithm(String altKeyAlgorithm) {
        this.altKeyAlgorithm = altKeyAlgorithm;
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
