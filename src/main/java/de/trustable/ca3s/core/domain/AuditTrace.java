package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AuditTrace.
 */
@Entity
@Table(name = "audit_trace")
@NamedQueries({
    @NamedQuery(name = "AuditTrace.findByCsrAndCert",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "(a.certificate = :certificate) or " +
            "(a.csr = :csr) "+
            "order by a.createdOn asc"
    ),
    @NamedQuery(name = "AuditTrace.findByCsr",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.csr = :csr "+
            "order by a.createdOn asc"
    ),
    @NamedQuery(name = "AuditTrace.findByCsrAndTemplate",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.csr = :csr " +
            "and a.contentTemplate = :template "
    ),
    @NamedQuery(name = "AuditTrace.findByPipeline",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.pipeline = :pipeline "+
            "order by a.createdOn asc"
    ),
    @NamedQuery(name = "AuditTrace.findByCaConnector",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.caConnector = :caConnector "+
            "order by a.createdOn asc"
    ),
    @NamedQuery(name = "AuditTrace.findByProcessInfo",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.processInfo = :processInfo "+
            "order by a.createdOn asc"
    ),
    @NamedQuery(name = "AuditTrace.findByRequestProxyConfig",
        query = "SELECT a FROM AuditTrace a WHERE " +
            "a.requestProxyConfig = :requestProxyConfig "+
            "order by a.createdOn asc"
    )
})
public class AuditTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "actor_name", nullable = false)
    private String actorName;

    @NotNull
    @Column(name = "actor_role", nullable = false)
    private String actorRole;

    @NotNull
    @Lob
    @Column(name = "plain_content", nullable = false)
    private String plainContent;

    @NotNull
    @Column(name = "content_template", nullable = false)
    private String contentTemplate;

    @NotNull
    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private CSR csr;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private Certificate certificate;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private Pipeline pipeline;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private CAConnectorConfig caConnector;

    @ManyToOne
    @JsonIgnoreProperties(value = "auditTraces", allowSetters = true)
    private BPMNProcessInfo processInfo;

    @ManyToOne
    @JsonIgnoreProperties(value = { "contacts", "orders" }, allowSetters = true)
    private AcmeAccount acmeAccount;

    @ManyToOne
    @JsonIgnoreProperties(value = { "acmeAuthorizations", "acmeIdentifiers", "csr", "certificate", "account" }, allowSetters = true)
    private AcmeOrder acmeOrder;

    @ManyToOne
    @JsonIgnoreProperties(value = { "csr", "certificate", "authenticatedBy" }, allowSetters = true)
    private ScepOrder scepOrder;

    @ManyToOne
    @JsonIgnoreProperties(value = { "pipelines" }, allowSetters = true)
    private RequestProxyConfig requestProxyConfig;

    @ManyToOne
    @JsonIgnoreProperties(value = { "secret", "caConnectorAttributes", "tlsAuthentication", "messageProtection" }, allowSetters = true)
    private CAConnectorConfig caConnectorConfig;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AuditTrace id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActorName() {
        return this.actorName;
    }

    public AuditTrace actorName(String actorName) {
        this.setActorName(actorName);
        return this;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return this.actorRole;
    }

    public AuditTrace actorRole(String actorRole) {
        this.setActorRole(actorRole);
        return this;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getPlainContent() {
        return this.plainContent;
    }

    public AuditTrace plainContent(String plainContent) {
        this.setPlainContent(plainContent);
        return this;
    }

    public void setPlainContent(String plainContent) {
        this.plainContent = plainContent;
    }

    public String getContentTemplate() {
        return this.contentTemplate;
    }

    public AuditTrace contentTemplate(String contentTemplate) {
        this.setContentTemplate(contentTemplate);
        return this;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public Instant getCreatedOn() {
        return this.createdOn;
    }

    public AuditTrace createdOn(Instant createdOn) {
        this.setCreatedOn(createdOn);
        return this;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public AuditTrace csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public AuditTrace certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public AuditTrace pipeline(Pipeline pipeline) {
        this.setPipeline(pipeline);
        return this;
    }

    public CAConnectorConfig getCaConnector() {
        return this.caConnector;
    }

    public void setCaConnector(CAConnectorConfig cAConnectorConfig) {
        this.caConnector = cAConnectorConfig;
    }

    public AuditTrace caConnector(CAConnectorConfig cAConnectorConfig) {
        this.setCaConnector(cAConnectorConfig);
        return this;
    }

    public BPMNProcessInfo getProcessInfo() {
        return this.processInfo;
    }

    public void setProcessInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.processInfo = bPMNProcessInfo;
    }

    public AuditTrace processInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.setProcessInfo(bPMNProcessInfo);
        return this;
    }

    public AcmeAccount getAcmeAccount() {
        return this.acmeAccount;
    }

    public void setAcmeAccount(AcmeAccount acmeAccount) {
        this.acmeAccount = acmeAccount;
    }

    public AuditTrace acmeAccount(AcmeAccount acmeAccount) {
        this.setAcmeAccount(acmeAccount);
        return this;
    }

    public AcmeOrder getAcmeOrder() {
        return this.acmeOrder;
    }

    public void setAcmeOrder(AcmeOrder acmeOrder) {
        this.acmeOrder = acmeOrder;
    }

    public AuditTrace acmeOrder(AcmeOrder acmeOrder) {
        this.setAcmeOrder(acmeOrder);
        return this;
    }

    public ScepOrder getScepOrder() {
        return this.scepOrder;
    }

    public void setScepOrder(ScepOrder scepOrder) {
        this.scepOrder = scepOrder;
    }

    public AuditTrace scepOrder(ScepOrder scepOrder) {
        this.setScepOrder(scepOrder);
        return this;
    }

    public RequestProxyConfig getRequestProxyConfig() {
        return this.requestProxyConfig;
    }

    public void setRequestProxyConfig(RequestProxyConfig requestProxyConfig) {
        this.requestProxyConfig = requestProxyConfig;
    }

    public AuditTrace requestProxyConfig(RequestProxyConfig requestProxyConfig) {
        this.setRequestProxyConfig(requestProxyConfig);
        return this;
    }

    public CAConnectorConfig getCaConnectorConfig() {
        return this.caConnectorConfig;
    }

    public void setCaConnectorConfig(CAConnectorConfig cAConnectorConfig) {
        this.caConnectorConfig = cAConnectorConfig;
    }

    public AuditTrace caConnectorConfig(CAConnectorConfig cAConnectorConfig) {
        this.setCaConnectorConfig(cAConnectorConfig);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditTrace)) {
            return false;
        }
        return id != null && id.equals(((AuditTrace) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AuditTrace{" +
            "id=" + getId() +
            ", actorName='" + getActorName() + "'" +
            ", actorRole='" + getActorRole() + "'" +
            ", plainContent='" + getPlainContent() + "'" +
            ", contentTemplate='" + getContentTemplate() + "'" +
            ", createdOn='" + getCreatedOn() + "'" +
            "}";
    }
}
