package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.TimedElementNotificationType;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A TimedElementNotification.
 */
@Entity
@Table(name = "timed_element_notification")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class TimedElementNotification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TimedElementNotificationType type;

    @NotNull
    @Column(name = "notify_on", nullable = false)
    private Instant notifyOn;

    @Column(name = "custom_message")
    private String customMessage;

    @ManyToOne
    @JsonIgnoreProperties(
        value = { "csr", "comment", "certificateAttributes", "issuingCertificate", "rootCertificate", "revocationCA" },
        allowSetters = true
    )
    private Certificate certificate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public TimedElementNotification id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TimedElementNotificationType getType() {
        return this.type;
    }

    public TimedElementNotification type(TimedElementNotificationType type) {
        this.setType(type);
        return this;
    }

    public void setType(TimedElementNotificationType type) {
        this.type = type;
    }

    public Instant getNotifyOn() {
        return this.notifyOn;
    }

    public TimedElementNotification notifyOn(Instant notifyOn) {
        this.setNotifyOn(notifyOn);
        return this;
    }

    public void setNotifyOn(Instant notifyOn) {
        this.notifyOn = notifyOn;
    }

    public String getCustomMessage() {
        return this.customMessage;
    }

    public TimedElementNotification customMessage(String customMessage) {
        this.setCustomMessage(customMessage);
        return this;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public TimedElementNotification certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TimedElementNotification)) {
            return false;
        }
        return id != null && id.equals(((TimedElementNotification) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TimedElementNotification{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", notifyOn='" + getNotifyOn() + "'" +
            ", customMessage='" + getCustomMessage() + "'" +
            "}";
    }
}
