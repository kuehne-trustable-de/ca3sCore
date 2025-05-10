package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeAccount.
 */
@Entity
@Table(name = "acme_account")
@NamedQueries({
	@NamedQuery(name = "Account.findByPublicKeyHash",
	query = "SELECT a FROM AcmeAccount a WHERE " +
			"a.publicKeyHash = :publicKeyHashBase64"
    ),
	@NamedQuery(name = "Account.findByAccountId",
	query = "SELECT a FROM AcmeAccount a WHERE " +
			"a.accountId = :accountId"
    ),
    @NamedQuery(name = "Account.findByCreatedOnIsNull",
        query = "SELECT a FROM AcmeAccount a WHERE " +
            "a.createdOn is null"),
})
public class AcmeAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @NotNull
    @Column(name = "realm", nullable = false)
    private String realm;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatus status;

    @NotNull
    @Column(name = "terms_of_service_agreed", nullable = false)
    private Boolean termsOfServiceAgreed;

    @NotNull
    @Column(name = "public_key_hash", nullable = false)
    private String publicKeyHash;

    @Lob
    @Column(name = "public_key", nullable = false)
    private String publicKey;

    @Column(name = "created_on")
    private Instant createdOn;


    @OneToMany(fetch = FetchType.EAGER, mappedBy = "account")
    @JsonIgnoreProperties(value = { "account" }, allowSetters = true)
    private Set<AcmeContact> contacts = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<AcmeOrder> orders = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeAccount id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return this.accountId;
    }

    public AcmeAccount accountId(Long accountId) {
        this.setAccountId(accountId);
        return this;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getRealm() {
        return this.realm;
    }

    public AcmeAccount realm(String realm) {
        this.setRealm(realm);
        return this;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public AccountStatus getStatus() {
        return this.status;
    }

    public AcmeAccount status(AccountStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Boolean isTermsOfServiceAgreed() {
        return termsOfServiceAgreed;
    }

    public Boolean getTermsOfServiceAgreed() {
        return this.termsOfServiceAgreed;
    }

    public AcmeAccount termsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.setTermsOfServiceAgreed(termsOfServiceAgreed);
        return this;
    }

    public void setTermsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    public String getPublicKeyHash() {
        return this.publicKeyHash;
    }

    public AcmeAccount publicKeyHash(String publicKeyHash) {
        this.setPublicKeyHash(publicKeyHash);
        return this;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public AcmeAccount publicKey(String publicKey) {
        this.setPublicKey(publicKey);
        return this;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Instant getCreatedOn() {
        return this.createdOn;
    }

    public AcmeAccount createdOn(Instant createdOn) {
        this.setCreatedOn(createdOn);
        return this;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Set<AcmeContact> getContacts() {
        return this.contacts;
    }

    public void setContacts(Set<AcmeContact> acmeContacts) {
        if (this.contacts != null) {
            this.contacts.forEach(i -> i.setAccount(null));
        }
        if (acmeContacts != null) {
            acmeContacts.forEach(i -> i.setAccount(this));
        }
        this.contacts = acmeContacts;
    }

    public AcmeAccount contacts(Set<AcmeContact> acmeContacts) {
        this.setContacts(acmeContacts);
        return this;
    }

    public AcmeAccount addContacts(AcmeContact acmeContact) {
        this.contacts.add(acmeContact);
        acmeContact.setAccount(this);
        return this;
    }

    public AcmeAccount removeContacts(AcmeContact acmeContact) {
        this.contacts.remove(acmeContact);
        acmeContact.setAccount(null);
        return this;
    }

    public Set<AcmeOrder> getOrders() {
        return this.orders;
    }

    public void setOrders(Set<AcmeOrder> acmeOrders) {
        if (this.orders != null) {
            this.orders.forEach(i -> i.setAccount(null));
        }
        if (acmeOrders != null) {
            acmeOrders.forEach(i -> i.setAccount(this));
        }
        this.orders = acmeOrders;
    }

    public AcmeAccount orders(Set<AcmeOrder> acmeOrders) {
        this.setOrders(acmeOrders);
        return this;
    }

    public AcmeAccount addOrders(AcmeOrder acmeOrder) {
        this.orders.add(acmeOrder);
        acmeOrder.setAccount(this);
        return this;
    }

    public AcmeAccount removeOrders(AcmeOrder acmeOrder) {
        this.orders.remove(acmeOrder);
        acmeOrder.setAccount(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeAccount)) {
            return false;
        }
        return id != null && id.equals(((AcmeAccount) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeAccount{" +
            "id=" + getId() +
            ", accountId=" + getAccountId() +
            ", realm='" + getRealm() + "'" +
            ", status='" + getStatus() + "'" +
            ", termsOfServiceAgreed='" + getTermsOfServiceAgreed() + "'" +
            ", publicKeyHash='" + getPublicKeyHash() + "'" +
            ", publicKey='" + getPublicKey() + "'" +
            "}";
    }
}
