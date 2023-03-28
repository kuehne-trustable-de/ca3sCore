package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeAuthorization.
 */
@Entity
@Table(name = "acme_authorization")
@NamedQueries({
	@NamedQuery(name = "AcmeAuthorization.findByAcmeAuthorizationId",
	query = "SELECT a FROM AcmeAuthorization a WHERE " +
			"a.acmeAuthorizationId = :acmeAuthorizationId"
    )
})
public class AcmeAuthorization implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "acme_authorization_id", nullable = false)
    private Long acmeAuthorizationId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value_", nullable = false)
    private String value;

    @OneToMany(mappedBy = "acmeAuthorization")
    @JsonIgnoreProperties(value = { "acmeAuthorization" }, allowSetters = true)
    private Set<AcmeChallenge> challenges = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("acmeAuthorizations")
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeAuthorization id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAcmeAuthorizationId() {
        return this.acmeAuthorizationId;
    }

    public AcmeAuthorization acmeAuthorizationId(Long acmeAuthorizationId) {
        this.setAcmeAuthorizationId(acmeAuthorizationId);
        return this;
    }

    public void setAcmeAuthorizationId(Long acmeAuthorizationId) {
        this.acmeAuthorizationId = acmeAuthorizationId;
    }

    public String getType() {
        return this.type;
    }

    public AcmeAuthorization type(String type) {
        this.setType(type);
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
        return this.challenges;
    }

    public void setChallenges(Set<AcmeChallenge> acmeChallenges) {
        if (this.challenges != null) {
            this.challenges.forEach(i -> i.setAcmeAuthorization(null));
        }
        if (acmeChallenges != null) {
            acmeChallenges.forEach(i -> i.setAcmeAuthorization(this));
        }
        this.challenges = acmeChallenges;
    }

    public AcmeAuthorization challenges(Set<AcmeChallenge> acmeChallenges) {
        this.setChallenges(acmeChallenges);
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

    public AcmeOrder getOrder() {
        return this.order;
    }

    public void setOrder(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
    }

    public AcmeAuthorization order(AcmeOrder acmeOrder) {
        this.setOrder(acmeOrder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
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
