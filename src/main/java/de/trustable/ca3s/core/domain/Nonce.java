package de.trustable.ca3s.core.domain;


import javax.persistence.*;

import java.io.Serializable;
import java.util.Objects;
import java.time.LocalDate;

/**
 * A Nonce.
 */
@Entity
@Table(name = "nonce")
public class Nonce implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nonce_value")
    private String nonceValue;

    @Column(name = "expires_at")
    private LocalDate expiresAt;

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

    public Nonce nonceValue(String nonceValue) {
        this.nonceValue = nonceValue;
        return this;
    }

    public void setNonceValue(String nonceValue) {
        this.nonceValue = nonceValue;
    }

    public LocalDate getExpiresAt() {
        return expiresAt;
    }

    public Nonce expiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public void setExpiresAt(LocalDate expiresAt) {
        this.expiresAt = expiresAt;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nonce)) {
            return false;
        }
        return id != null && id.equals(((Nonce) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Nonce{" +
            "id=" + getId() +
            ", nonceValue='" + getNonceValue() + "'" +
            ", expiresAt='" + getExpiresAt() + "'" +
            "}";
    }
}
