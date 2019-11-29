package de.trustable.ca3sjh.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3sjh.domain.enumeration.OrderStatus;

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
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
    private OrderStatus status;

    @Column(name = "expires")
    private LocalDate expires;

    @Column(name = "not_before")
    private LocalDate notBefore;

    @Column(name = "not_after")
    private LocalDate notAfter;

    @Column(name = "error")
    private String error;

    @Column(name = "finalize_url")
    private String finalizeUrl;

    @Column(name = "certificate_url")
    private String certificateUrl;

    @OneToMany(mappedBy = "order")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Authorization> authorizations = new HashSet<>();

    @OneToMany(mappedBy = "order")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Identifier> identifiers = new HashSet<>();

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

    public OrderStatus getStatus() {
        return status;
    }

    public AcmeOrder status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getExpires() {
        return expires;
    }

    public AcmeOrder expires(LocalDate expires) {
        this.expires = expires;
        return this;
    }

    public void setExpires(LocalDate expires) {
        this.expires = expires;
    }

    public LocalDate getNotBefore() {
        return notBefore;
    }

    public AcmeOrder notBefore(LocalDate notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    public void setNotBefore(LocalDate notBefore) {
        this.notBefore = notBefore;
    }

    public LocalDate getNotAfter() {
        return notAfter;
    }

    public AcmeOrder notAfter(LocalDate notAfter) {
        this.notAfter = notAfter;
        return this;
    }

    public void setNotAfter(LocalDate notAfter) {
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

    public Set<Authorization> getAuthorizations() {
        return authorizations;
    }

    public AcmeOrder authorizations(Set<Authorization> authorizations) {
        this.authorizations = authorizations;
        return this;
    }

    public AcmeOrder addAuthorizations(Authorization authorization) {
        this.authorizations.add(authorization);
        authorization.setOrder(this);
        return this;
    }

    public AcmeOrder removeAuthorizations(Authorization authorization) {
        this.authorizations.remove(authorization);
        authorization.setOrder(null);
        return this;
    }

    public void setAuthorizations(Set<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public AcmeOrder identifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    public AcmeOrder addIdentifiers(Identifier identifier) {
        this.identifiers.add(identifier);
        identifier.setOrder(this);
        return this;
    }

    public AcmeOrder removeIdentifiers(Identifier identifier) {
        this.identifiers.remove(identifier);
        identifier.setOrder(null);
        return this;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
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
