package de.trustable.ca3s.core.domain;

import javax.persistence.*;

import java.io.Serializable;
import java.time.Instant;

/**
 * A AcmeNonce.
 */
@Entity
@Table(name = "acme_nonce")
@NamedQueries({
	@NamedQuery(name = "AcmeNonce.findByNonceValue",
	query = "SELECT n FROM AcmeNonce n WHERE " +
			"n.nonceValue = :nonceValue"
    ),
	@NamedQuery(name = "AcmeNonce.findByNonceExpiredBefore",
	query = "SELECT n FROM AcmeNonce n WHERE " +
			"n.expiresAt < :expiredBefore"
    ),	
})
public class AcmeNonce implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nonce_value")
    private String nonceValue;

    @Column(name = "expires_at")
    private Instant expiresAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNonceValue() {
        return nonceValue;
    }

    public AcmeNonce nonceValue(String nonceValue) {
        this.nonceValue = nonceValue;
        return this;
    }

    public void setNonceValue(String nonceValue) {
        this.nonceValue = nonceValue;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public AcmeNonce expiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeNonce)) {
            return false;
        }
        return id != null && id.equals(((AcmeNonce) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AcmeNonce{" +
            "id=" + getId() +
            ", nonceValue='" + getNonceValue() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            "}";
    }
}
