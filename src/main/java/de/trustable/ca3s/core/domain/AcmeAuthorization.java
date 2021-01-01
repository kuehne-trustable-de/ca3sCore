package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

/**
 * A AcmeAuthorization.
 */
@Entity
@Table(name = "acme_authorization")
public class AcmeAuthorization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "acme_authorization_id", nullable = false)
    private Long acmeAuthorizationId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @OneToMany(mappedBy = "acmeAuthorization")
    private Set<AcmeChallenge> challenges = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("acmeAuthorizations")
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAcmeAuthorizationId() {
        return acmeAuthorizationId;
    }

    public AcmeAuthorization acmeAuthorizationId(Long acmeAuthorizationId) {
        this.acmeAuthorizationId = acmeAuthorizationId;
        return this;
    }

    public void setAcmeAuthorizationId(Long acmeAuthorizationId) {
        this.acmeAuthorizationId = acmeAuthorizationId;
    }

    public String getType() {
        return type;
    }

    public AcmeAuthorization type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public AcmeAuthorization value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Set<AcmeChallenge> getChallenges() {
        return challenges;
    }

    public AcmeAuthorization challenges(Set<AcmeChallenge> acmeChallenges) {
        this.challenges = acmeChallenges;
        return this;
    }

    public AcmeAuthorization addChallenges(AcmeChallenge acmeChallenge) {
        this.challenges.add(acmeChallenge);
        acmeChallenge.setAcmeAuthorization(this);
        return this;
    }

    public AcmeAuthorization removeChallenges(AcmeChallenge acmeChallenge) {
        this.challenges.remove(acmeChallenge);
        acmeChallenge.setAcmeAuthorization(null);
        return this;
    }

    public void setChallenges(Set<AcmeChallenge> acmeChallenges) {
        this.challenges = acmeChallenges;
    }

    public AcmeOrder getOrder() {
        return order;
    }

    public AcmeAuthorization order(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
        return this;
    }

    public void setOrder(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeAuthorization)) {
            return false;
        }
        return id != null && id.equals(((AcmeAuthorization) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AcmeAuthorization{" +
            "id=" + getId() +
            ", acmeAuthorizationId=" + getAcmeAuthorizationId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
