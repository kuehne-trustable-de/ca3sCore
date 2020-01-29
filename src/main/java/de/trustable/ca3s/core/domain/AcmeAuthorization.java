package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A AcmeAuthorization.
 */
@Entity
@Table(name = "acmeauthorization")
@NamedQueries({
	@NamedQuery(name = "AcmeAuthorization.findByAuthorizationId",
	query = "SELECT a FROM AcmeAuthorization a WHERE " +
			"a.authorizationId = :authorizationId"
    ),    
})
public class AcmeAuthorization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "authorization_id", nullable = false)
    private Long authorizationId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @OneToMany(mappedBy = "authorization")
    private Set<AcmeChallenge> challenges = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("authorizations")
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAuthorizationId() {
        return authorizationId;
    }

    public AcmeAuthorization authorizationId(Long authorizationId) {
        this.authorizationId = authorizationId;
        return this;
    }

    public void setAuthorizationId(Long authorizationId) {
        this.authorizationId = authorizationId;
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
        acmeChallenge.setAuthorization(this);
        return this;
    }

    public AcmeAuthorization removeChallenges(AcmeChallenge acmeChallenge) {
        this.challenges.remove(acmeChallenge);
        acmeChallenge.setAuthorization(null);
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
            ", authorizationId=" + getAuthorizationId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
