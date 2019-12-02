package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A AcmeContact.
 */
@Entity
@Table(name = "acme_contact")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AcmeContact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @NotNull
    @Column(name = "contact_url", nullable = false)
    private String contactUrl;

    @ManyToOne
    @JsonIgnoreProperties("contacts")
    private ACMEAccount account;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContactId() {
        return contactId;
    }

    public AcmeContact contactId(Long contactId) {
        this.contactId = contactId;
        return this;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactUrl() {
        return contactUrl;
    }

    public AcmeContact contactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
        return this;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public ACMEAccount getAccount() {
        return account;
    }

    public AcmeContact account(ACMEAccount aCMEAccount) {
        this.account = aCMEAccount;
        return this;
    }

    public void setAccount(ACMEAccount aCMEAccount) {
        this.account = aCMEAccount;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeContact)) {
            return false;
        }
        return id != null && id.equals(((AcmeContact) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AcmeContact{" +
            "id=" + getId() +
            ", contactId=" + getContactId() +
            ", contactUrl='" + getContactUrl() + "'" +
            "}";
    }
}
