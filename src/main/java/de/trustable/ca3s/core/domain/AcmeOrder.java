package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
    @NamedQuery(name = "AcmeOrder.findPipelineIsNull",
        query = "SELECT a FROM AcmeOrder a WHERE " +
            "a.pipeline IS NULL"
    ),
    @NamedQuery(name = "AcmeOrder.countByAccountId",
        query = "SELECT count(a) FROM AcmeOrder a WHERE " +
            "a.account.accountId = :accountId"
    ),
    @NamedQuery(name = "AcmeOrder.findByPendingExpiryBefore",
        query = "SELECT a FROM AcmeOrder a WHERE " +
            "a.expires < :expiresBefore AND " +
            "a.status = 'PENDING'"
    ),
})
public class AcmeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(AcmeOrder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @NotNull
    @Column(name = "realm", nullable = false)
    private String realm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AcmeOrderStatus status;

    @Column(name = "expires")
    private Instant expires;
    @Column(name = "created_on")
    private Instant createdOn;

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
    @JsonIgnoreProperties(value = { "challenges", "order" }, allowSetters = true)
    private Set<AcmeAuthorization> acmeAuthorizations = new HashSet<>();

    @OneToMany(mappedBy = "order")
    @JsonIgnoreProperties(value = { "order" }, allowSetters = true)
    private Set<AcmeOrderAttribute> attributes = new HashSet<>();

    @OneToMany(mappedBy = "order")
    @JsonIgnoreProperties(value = { "order" }, allowSetters = true)
    private Set<AcmeIdentifier> acmeIdentifiers = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "comment", "rdns", "ras", "csrAttributes", "pipeline", "certificate" }, allowSetters = true)
    private CSR csr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(
        value = { "csr", "comment", "certificateAttributes", "issuingCertificate", "rootCertificate", "revocationCA" },
        allowSetters = true
    )
    private Certificate certificate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "pipelineAttributes", "caConnector", "processInfo" }, allowSetters = true)
    private Pipeline pipeline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties(value = { "contacts", "orders" }, allowSetters = true)
    private AcmeAccount account;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public AcmeOrder orderId(Long orderId) {
        this.setOrderId(orderId);
        return this;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getRealm() {
        return this.realm;
    }

    public AcmeOrder realm(String realm) {
        this.setRealm(realm);
        return this;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public AcmeOrderStatus getStatus() {
        return this.status;
    }

    public AcmeOrder status(AcmeOrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(AcmeOrderStatus status) {

        if(Objects.equals(this.status, status)){
            LOG.info("status  {} unchanged of order {}", this.status, this.orderId);
        }else{
            LOG.info("status change {} -> {} of order {}", this.status, status, this.orderId);
        }
        this.status = status;
    }

    public Instant getExpires() {
        return this.expires;
    }

    public AcmeOrder expires(Instant expires) {
        this.setExpires(expires);
        return this;
    }

    public void setExpires(Instant expires) {
        this.expires = expires;
    }

    public Instant getNotBefore() {
        return this.notBefore;
    }

    public AcmeOrder notBefore(Instant notBefore) {
        this.setNotBefore(notBefore);
        return this;
    }

    public void setNotBefore(Instant notBefore) {
        this.notBefore = notBefore;
    }

    public Instant getNotAfter() {
        return this.notAfter;
    }

    public AcmeOrder notAfter(Instant notAfter) {
        this.setNotAfter(notAfter);
        return this;
    }

    public void setNotAfter(Instant notAfter) {
        this.notAfter = notAfter;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public String getError() {
        return this.error;
    }

    public AcmeOrder error(String error) {
        this.setError(error);
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getFinalizeUrl() {
        return this.finalizeUrl;
    }

    public AcmeOrder finalizeUrl(String finalizeUrl) {
        this.setFinalizeUrl(finalizeUrl);
        return this;
    }

    public void setFinalizeUrl(String finalizeUrl) {
        this.finalizeUrl = finalizeUrl;
    }

    public String getCertificateUrl() {
        return this.certificateUrl;
    }

    public AcmeOrder certificateUrl(String certificateUrl) {
        this.setCertificateUrl(certificateUrl);
        return this;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public Set<AcmeAuthorization> getAcmeAuthorizations() {
        return this.acmeAuthorizations;
    }

    public void setAcmeAuthorizations(Set<AcmeAuthorization> acmeAuthorizations) {
        if (this.acmeAuthorizations != null) {
            this.acmeAuthorizations.forEach(i -> i.setOrder(null));
        }
        if (acmeAuthorizations != null) {
            acmeAuthorizations.forEach(i -> i.setOrder(this));
        }
        this.acmeAuthorizations = acmeAuthorizations;
    }

    public AcmeOrder acmeAuthorizations(Set<AcmeAuthorization> acmeAuthorizations) {
        this.setAcmeAuthorizations(acmeAuthorizations);
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

    public Set<AcmeOrderAttribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Set<AcmeOrderAttribute> acmeOrderAttributes) {
        if (this.attributes != null) {
            this.attributes.forEach(i -> i.setOrder(null));
        }
        if (acmeOrderAttributes != null) {
            acmeOrderAttributes.forEach(i -> i.setOrder(this));
        }
        this.attributes = acmeOrderAttributes;
    }

    public AcmeOrder attributes(Set<AcmeOrderAttribute> acmeOrderAttributes) {
        this.setAttributes(acmeOrderAttributes);
        return this;
    }

    public AcmeOrder addAttributes(AcmeOrderAttribute acmeOrderAttribute) {
        this.attributes.add(acmeOrderAttribute);
        acmeOrderAttribute.setOrder(this);
        return this;
    }

    public AcmeOrder removeAttributes(AcmeOrderAttribute acmeOrderAttribute) {
        this.attributes.remove(acmeOrderAttribute);
        acmeOrderAttribute.setOrder(null);
        return this;
    }


    public Set<AcmeIdentifier> getAcmeIdentifiers() {
        return this.acmeIdentifiers;
    }

    public void setAcmeIdentifiers(Set<AcmeIdentifier> acmeIdentifiers) {
        if (this.acmeIdentifiers != null) {
            this.acmeIdentifiers.forEach(i -> i.setOrder(null));
        }
        if (acmeIdentifiers != null) {
            acmeIdentifiers.forEach(i -> i.setOrder(this));
        }
        this.acmeIdentifiers = acmeIdentifiers;
    }

    public AcmeOrder acmeIdentifiers(Set<AcmeIdentifier> acmeIdentifiers) {
        this.setAcmeIdentifiers(acmeIdentifiers);
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

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public AcmeOrder csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public AcmeOrder certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public AcmeOrder pipeline(Pipeline pipeline) {
        this.setPipeline(pipeline);
        return this;
    }

    public AcmeAccount getAccount() {
        return this.account;
    }

    public void setAccount(AcmeAccount acmeAccount) {
        this.account = acmeAccount;
    }

    public AcmeOrder account(AcmeAccount acmeAccount) {
        this.setAccount(acmeAccount);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeOrder{" +
            "id=" + getId() +
            ", orderId=" + getOrderId() +
            ", realm='" + getRealm() + "'" +
            ", status='" + getStatus() + "'" +
            ", expires='" + getExpires() + "'" +
            ", createdOn='" + getCreatedOn() + "'" +
            ", notBefore='" + getNotBefore() + "'" +
            ", notAfter='" + getNotAfter() + "'" +
            ", error='" + getError() + "'" +
            ", finalizeUrl='" + getFinalizeUrl() + "'" +
            ", certificateUrl='" + getCertificateUrl() + "'" +
            "}";

    }
}
