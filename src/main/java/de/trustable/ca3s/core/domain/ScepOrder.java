package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.ScepOrderStatus;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A ScepOrder.
 */
@Entity
@Table(name = "scep_order")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ScepOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "trans_id", nullable = false)
    private String transId;

    @NotNull
    @Column(name = "realm", nullable = false)
    private String realm;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ScepOrderStatus status;

    @Column(name = "requested_on")
    private Instant requestedOn;

    @Column(name = "requested_by")
    private String requestedBy;

    @Column(name = "async_processing")
    private Boolean asyncProcessing;

    @Column(name = "password_authentication")
    private Boolean passwordAuthentication;

    @OneToMany(mappedBy = "order")
    @JsonIgnoreProperties(value = { "order" }, allowSetters = true)
    private Set<ScepOrderAttribute> attributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "comment", "rdns", "ras", "csrAttributes", "pipeline", "certificate" }, allowSetters = true)
    private CSR csr;

    @ManyToOne
    @JsonIgnoreProperties(
        value = { "csr", "comment", "certificateAttributes", "issuingCertificate", "rootCertificate", "revocationCA" },
        allowSetters = true
    )
    private Certificate certificate;

    @ManyToOne
    @JsonIgnoreProperties(
        value = { "csr", "comment", "certificateAttributes", "issuingCertificate", "rootCertificate", "revocationCA" },
        allowSetters = true
    )
    private Certificate authenticatedBy;

    @ManyToOne
    @JsonIgnoreProperties(
        value = { "pipelineAttributes", "requestProxies", "caConnector", "processInfo", "algorithms" },
        allowSetters = true
    )
    private Pipeline pipeline;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ScepOrder id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransId() {
        return this.transId;
    }

    public ScepOrder transId(String transId) {
        this.setTransId(transId);
        return this;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getRealm() {
        return this.realm;
    }

    public ScepOrder realm(String realm) {
        this.setRealm(realm);
        return this;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public ScepOrderStatus getStatus() {
        return this.status;
    }

    public ScepOrder status(ScepOrderStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(ScepOrderStatus status) {
        this.status = status;
    }

    public Instant getRequestedOn() {
        return this.requestedOn;
    }

    public ScepOrder requestedOn(Instant requestedOn) {
        this.setRequestedOn(requestedOn);
        return this;
    }

    public void setRequestedOn(Instant requestedOn) {
        this.requestedOn = requestedOn;
    }

    public String getRequestedBy() {
        return this.requestedBy;
    }

    public ScepOrder requestedBy(String requestedBy) {
        this.setRequestedBy(requestedBy);
        return this;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Boolean getAsyncProcessing() {
        return this.asyncProcessing;
    }

    public ScepOrder asyncProcessing(Boolean asyncProcessing) {
        this.setAsyncProcessing(asyncProcessing);
        return this;
    }

    public void setAsyncProcessing(Boolean asyncProcessing) {
        this.asyncProcessing = asyncProcessing;
    }

    public Boolean getPasswordAuthentication() {
        return this.passwordAuthentication;
    }

    public ScepOrder passwordAuthentication(Boolean passwordAuthentication) {
        this.setPasswordAuthentication(passwordAuthentication);
        return this;
    }

    public void setPasswordAuthentication(Boolean passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    public Set<ScepOrderAttribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Set<ScepOrderAttribute> scepOrderAttributes) {
        if (this.attributes != null) {
            this.attributes.forEach(i -> i.setOrder(null));
        }
        if (scepOrderAttributes != null) {
            scepOrderAttributes.forEach(i -> i.setOrder(this));
        }
        this.attributes = scepOrderAttributes;
    }

    public ScepOrder attributes(Set<ScepOrderAttribute> scepOrderAttributes) {
        this.setAttributes(scepOrderAttributes);
        return this;
    }

    public ScepOrder addAttributes(ScepOrderAttribute scepOrderAttribute) {
        this.attributes.add(scepOrderAttribute);
        scepOrderAttribute.setOrder(this);
        return this;
    }

    public ScepOrder removeAttributes(ScepOrderAttribute scepOrderAttribute) {
        this.attributes.remove(scepOrderAttribute);
        scepOrderAttribute.setOrder(null);
        return this;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public ScepOrder csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public ScepOrder certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    public Certificate getAuthenticatedBy() {
        return this.authenticatedBy;
    }

    public void setAuthenticatedBy(Certificate certificate) {
        this.authenticatedBy = certificate;
    }

    public ScepOrder authenticatedBy(Certificate certificate) {
        this.setAuthenticatedBy(certificate);
        return this;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public ScepOrder pipeline(Pipeline pipeline) {
        this.setPipeline(pipeline);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ScepOrder)) {
            return false;
        }
        return id != null && id.equals(((ScepOrder) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ScepOrder{" +
            "id=" + getId() +
            ", transId='" + getTransId() + "'" +
            ", realm='" + getRealm() + "'" +
            ", status='" + getStatus() + "'" +
            ", requestedOn='" + getRequestedOn() + "'" +
            ", requestedBy='" + getRequestedBy() + "'" +
            ", asyncProcessing='" + getAsyncProcessing() + "'" +
            ", passwordAuthentication='" + getPasswordAuthentication() + "'" +
            "}";
    }
}
