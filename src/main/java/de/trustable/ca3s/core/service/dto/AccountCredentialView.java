package de.trustable.ca3s.core.service.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;

public class AccountCredentialView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Logger LOG = LoggerFactory.getLogger(AccountCredentialView.class);

    private Long id;

    private Instant createdOn;

    private Instant validTo;

    private Integer leftUsages;

    private AccountCredentialsType relationType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Instant getValidTo() {
        return validTo;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public Integer getLeftUsages() {
        return leftUsages;
    }

    public void setLeftUsages(Integer leftUsages) {
        this.leftUsages = leftUsages;
    }

    public AccountCredentialsType getRelationType() {
        return relationType;
    }

    public void setRelationType(AccountCredentialsType relationType) {
        this.relationType = relationType;
    }
}
