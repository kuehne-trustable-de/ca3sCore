package de.trustable.ca3s.core.domain;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3s.core.domain.enumeration.AccountStatus;

/**
 * A ACMEAccount.
 */
@Entity
@Table(name = "acme_account")
@NamedQueries({
	@NamedQuery(name = "Account.findByPublicKeyHash",
	query = "SELECT a FROM ACMEAccount a WHERE " +
			"a.publicKeyHash = :publicKeyHashBase64"
    ),
    @NamedQuery(name = "Account.findByAccountId",
        query = "SELECT a FROM ACMEAccount a WHERE " +
            "a.accountId = :accountId"
    ),
    @NamedQuery(name = "Account.findByCreatedOnIsNull",
        query = "SELECT a FROM ACMEAccount a WHERE " +
            "a.createdOn is null"
    ),
})
public class ACMEAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    @OneToMany(mappedBy = "account")
    private Set<AcmeContact> contacts = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account")
    private Set<AcmeOrder> orders = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public ACMEAccount accountId(Long accountId) {
        this.accountId = accountId;
        return this;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getRealm() {
        return realm;
    }

    public ACMEAccount realm(String realm) {
        this.realm = realm;
        return this;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public ACMEAccount status(AccountStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public Boolean isTermsOfServiceAgreed() {
        return termsOfServiceAgreed;
    }

    public ACMEAccount termsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
        return this;
    }

    public void setTermsOfServiceAgreed(Boolean termsOfServiceAgreed) {
        this.termsOfServiceAgreed = termsOfServiceAgreed;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public ACMEAccount publicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
        return this;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public ACMEAccount publicKey(String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Instant getCreatedOn() {
        return this.createdOn;
    }

    public ACMEAccount createdOn(Instant createdOn) {
        this.setCreatedOn(createdOn);
        return this;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Set<AcmeContact> getContacts() {
        return contacts;
    }

    public ACMEAccount contacts(Set<AcmeContact> acmeContacts) {
        this.contacts = acmeContacts;
        return this;
    }

    public ACMEAccount addContacts(AcmeContact acmeContact) {
        this.contacts.add(acmeContact);
        acmeContact.setAccount(this);
        return this;
    }

    public ACMEAccount removeContacts(AcmeContact acmeContact) {
        this.contacts.remove(acmeContact);
        acmeContact.setAccount(null);
        return this;
    }

    public void setContacts(Set<AcmeContact> acmeContacts) {
        this.contacts = acmeContacts;
    }

    public Set<AcmeOrder> getOrders() {
        return orders;
    }

    public ACMEAccount orders(Set<AcmeOrder> acmeOrders) {
        this.orders = acmeOrders;
        return this;
    }

    public ACMEAccount addOrders(AcmeOrder acmeOrder) {
        this.orders.add(acmeOrder);
        acmeOrder.setAccount(this);
        return this;
    }

    public ACMEAccount removeOrders(AcmeOrder acmeOrder) {
        this.orders.remove(acmeOrder);
        acmeOrder.setAccount(null);
        return this;
    }

    public void setOrders(Set<AcmeOrder> acmeOrders) {
        this.orders = acmeOrders;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ACMEAccount)) {
            return false;
        }
        return id != null && id.equals(((ACMEAccount) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "ACMEAccount{" +
            "id=" + getId() +
            ", accountId=" + getAccountId() +
            ", realm='" + getRealm() + "'" +
            ", status='" + getStatus() + "'" +
            ", termsOfServiceAgreed='" + isTermsOfServiceAgreed() + "'" +
            ", publicKeyHash='" + getPublicKeyHash() + "'" +
            ", publicKey='" + getPublicKey() + "'" +
            "}";
    }
}
