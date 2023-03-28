package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeContact.
 */
@Entity
@Table(name = "acme_contact")
@NamedQueries({
    @NamedQuery(name = "AcmeContact.findByAccountId",
        query = "SELECT ac FROM AcmeContact ac WHERE " +
            "ac.account.accountId = :accountId"
    )
})
public class AcmeContact implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "contact_id", nullable = false)
    private Long contactId;

    @NotNull
    @Column(name = "contact_url", nullable = false)
    private String contactUrl;

    @ManyToOne
    @JsonIgnoreProperties("contacts")
    private AcmeAccount account;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeContact id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContactId() {
        return this.contactId;
    }

    public AcmeContact contactId(Long contactId) {
        this.setContactId(contactId);
        return this;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactUrl() {
        return this.contactUrl;
    }

    public AcmeContact contactUrl(String contactUrl) {
        this.setContactUrl(contactUrl);
        return this;
    }

    public void setContactUrl(String contactUrl) {
        this.contactUrl = contactUrl;
    }

    public AcmeAccount getAccount() {
        return this.account;
    }

    public void setAccount(AcmeAccount acmeAccount) {
        this.account = acmeAccount;
    }

    public AcmeContact account(AcmeAccount acmeAccount) {
        this.setAccount(acmeAccount);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeContact{" +
            "id=" + getId() +
            ", contactId=" + getContactId() +
            ", contactUrl='" + getContactUrl() + "'" +
            "}";
    }
}
