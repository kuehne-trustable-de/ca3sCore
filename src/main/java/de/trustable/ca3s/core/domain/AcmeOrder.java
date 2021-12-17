package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;

/**
 * A AcmeOrder.
 */
@Entity
@Table(name = "acme_order")
@NamedQueries({
    @NamedQuery(name = "AcmeOrder.findByOrderId",
        query = "SELECT a FROM AcmeOrder a WHERE " +
            "a.orderId = :orderId"
    ),
    @NamedQuery(name = "AcmeOrder.countByAccountId",
        query = "SELECT count(a) FROM AcmeOrder a WHERE " +
            "a.account.accountId = :accountId"
    ),
})
public class AcmeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcmeOrderStatus status;

    @Column(name = "expires")
    private Instant expires;

    @Column(name = "not_before")
    private Instant notBefore;

    @Column(name = "not_after")
    private Instant notAfter;

    @Column(name = "error")
    private String error;

    @Column(name = "finalize_url")
    private String finalizeUrl;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @OneToMany(mappedBy = "order")
    private Set<AcmeAuthorization> acmeAuthorizations = new HashSet<>();

    @OneToMany(mappedBy = "order")
    private Set<AcmeIdentifier> acmeIdentifiers = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("acmeOrders")
    private CSR csr;

    @ManyToOne
    @JsonIgnoreProperties("acmeOrders")
    private Certificate certificate;

    @ManyToOne
    @JsonIgnoreProperties("orders")
    private ACMEAccount account;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public AcmeOrder orderId(Long orderId) {
        this.orderId = orderId;
        return this;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public AcmeOrderStatus getStatus() {
        return status;
    }

    public AcmeOrder status(AcmeOrderStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(AcmeOrderStatus status) {
        this.status = status;
    }

    public Instant getExpires() {
        return expires;
    }

    public AcmeOrder expires(Instant expires) {
        this.expires = expires;
        return this;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    public Instant getNotBefore() {
        return notBefore;
    }

    public AcmeOrder notBefore(Instant notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    public void setNotBefore(Instant notBefore) {
        this.notBefore = notBefore;
    }

    public Instant getNotAfter() {
        return notAfter;
    }

    public AcmeOrder notAfter(Instant notAfter) {
        this.notAfter = notAfter;
        return this;
    }

    public void setNotAfter(Instant notAfter) {
        this.notAfter = notAfter;
    }

    public String getError() {
        return error;
    }

    public AcmeOrder error(String error) {
        this.error = error;
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getFinalizeUrl() {
        return finalizeUrl;
    }

    public AcmeOrder finalizeUrl(String finalizeUrl) {
        this.finalizeUrl = finalizeUrl;
        return this;
    }

    public void setFinalizeUrl(String finalizeUrl) {
        this.finalizeUrl = finalizeUrl;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public AcmeOrder certificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
        return this;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public Set<AcmeAuthorization> getAcmeAuthorizations() {
        return acmeAuthorizations;
    }

    public AcmeOrder acmeAuthorizations(Set<AcmeAuthorization> acmeAuthorizations) {
        this.acmeAuthorizations = acmeAuthorizations;
        return this;
    }

    public AcmeOrder addAcmeAuthorizations(AcmeAuthorization acmeAuthorization) {
        this.acmeAuthorizations.add(acmeAuthorization);
        acmeAuthorization.setOrder(this);
        return this;
    }

    public AcmeOrder removeAcmeAuthorizations(AcmeAuthorization acmeAuthorization) {
        this.acmeAuthorizations.remove(acmeAuthorization);
        acmeAuthorization.setOrder(null);
        return this;
    }

    public void setAcmeAuthorizations(Set<AcmeAuthorization> acmeAuthorizations) {
        this.acmeAuthorizations = acmeAuthorizations;
    }

    public Set<AcmeIdentifier> getAcmeIdentifiers() {
        return acmeIdentifiers;
    }

    public AcmeOrder acmeIdentifiers(Set<AcmeIdentifier> acmeIdentifiers) {
        this.acmeIdentifiers = acmeIdentifiers;
        return this;
    }

    public AcmeOrder addAcmeIdentifiers(AcmeIdentifier acmeIdentifier) {
        this.acmeIdentifiers.add(acmeIdentifier);
        acmeIdentifier.setOrder(this);
        return this;
    }

    public AcmeOrder removeAcmeIdentifiers(AcmeIdentifier acmeIdentifier) {
        this.acmeIdentifiers.remove(acmeIdentifier);
        acmeIdentifier.setOrder(null);
        return this;
    }

    public void setAcmeIdentifiers(Set<AcmeIdentifier> acmeIdentifiers) {
        this.acmeIdentifiers = acmeIdentifiers;
    }

    public CSR getCsr() {
        return csr;
    }

    public AcmeOrder csr(CSR cSR) {
        this.csr = cSR;
        return this;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public AcmeOrder certificate(Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public ACMEAccount getAccount() {
        return account;
    }

    public AcmeOrder account(ACMEAccount aCMEAccount) {
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
        if (!(o instanceof AcmeOrder)) {
            return false;
        }
        return id != null && id.equals(((AcmeOrder) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "AcmeOrder{" +
            "id=" + getId() +
            ", orderId=" + getOrderId() +
            ", status='" + getStatus() + "'" +
            ", expires='" + getExpires() + "'" +
            ", notBefore='" + getNotBefore() + "'" +
            ", notAfter='" + getNotAfter() + "'" +
            ", error='" + getError() + "'" +
            ", finalizeUrl='" + getFinalizeUrl() + "'" +
            ", certificateUrl='" + getCertificateUrl() + "'" +
            "}";
    }
}
