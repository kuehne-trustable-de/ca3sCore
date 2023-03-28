package de.trustable.ca3s.core.domain;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A CRLExpirationNotification.
 */
@Entity
@Table(name = "crl_expiration_notification")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CRLExpirationNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "crl_url", nullable = false)
    private String crlUrl;

    @NotNull
    @Column(name = "notify_before", nullable = false)
    private Duration notifyBefore;

    @NotNull
    @Column(name = "notify_until", nullable = false)
    private Instant notifyUntil;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CRLExpirationNotification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCrlUrl() {
        return this.crlUrl;
    }

    public CRLExpirationNotification crlUrl(String crlUrl) {
        this.setCrlUrl(crlUrl);
        return this;
    }

    public void setCrlUrl(String crlUrl) {
        this.crlUrl = crlUrl;
    }

    public Duration getNotifyBefore() {
        return this.notifyBefore;
    }

    public CRLExpirationNotification notifyBefore(Duration notifyBefore) {
        this.setNotifyBefore(notifyBefore);
        return this;
    }

    public void setNotifyBefore(Duration notifyBefore) {
        this.notifyBefore = notifyBefore;
    }

    public Instant getNotifyUntil() {
        return this.notifyUntil;
    }

    public CRLExpirationNotification notifyUntil(Instant notifyUntil) {
        this.setNotifyUntil(notifyUntil);
        return this;
    }

    public void setNotifyUntil(Instant notifyUntil) {
        this.notifyUntil = notifyUntil;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CRLExpirationNotification)) {
            return false;
        }
        return id != null && id.equals(((CRLExpirationNotification) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CRLExpirationNotification{" +
            "id=" + getId() +
            ", crlUrl='" + getCrlUrl() + "'" +
            ", notifyBefore='" + getNotifyBefore() + "'" +
            ", notifyUntil='" + getNotifyUntil() + "'" +
            "}";
    }
}
