package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.ProtectedContentStatus;
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

    private String pipelineName;

    private ProtectedContentStatus status;

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

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public ProtectedContentStatus getStatus() {
        return status;
    }

    public void setStatus(ProtectedContentStatus status) {
        this.status = status;
    }
}
