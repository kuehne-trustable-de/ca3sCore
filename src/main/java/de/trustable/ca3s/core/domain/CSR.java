package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3s.core.domain.enumeration.PipelineType;

import de.trustable.ca3s.core.domain.enumeration.CsrStatus;


/**
 * A CSR.
 */
@Entity
@Table(name = "csr")
@NamedQueries({
    @NamedQuery(name = "CSR.findByPublicKeyHash",
        query = "SELECT c FROM CSR c WHERE " +
            "c.publicKeyHash = :hash"
    ),
    @NamedQuery(name = "CSR.findNonRejectedByPublicKeyHash",
        query = "SELECT c FROM CSR c WHERE " +
            " c.publicKeyHash = :hash  and " +
            " c.status <> 'REJECTED' "
    ),
    @NamedQuery(name = "CSR.countAll",
        query = "SELECT count(c) FROM CSR c "
    ),
    @NamedQuery(name = "CSR.findWithoutAttribute",
        query = "SELECT c FROM CSR c WHERE NOT EXISTS( select 1 FROM CsrAttribute attr WHERE attr.csr = c AND attr.name = :name)"
    ),
    @NamedQuery(name = "CSR.findByAttributeValue",
        query = "SELECT c FROM CSR c JOIN c.csrAttributes attr  WHERE attr.name = :name and attr.value = :value"
    ),
    @NamedQuery(name = "CSR.findByRequestor",
        query = "SELECT c FROM CSR c WHERE " +
            " c.requestedBy = :requestor"
    ),
    @NamedQuery(name = "CSR.findPendingByDay",
        query = "SELECT c FROM CSR c WHERE " +
            " c.requestedOn >= :after and " +
            " c.requestedOn <= :before and " +
            " c.status = 'PENDING' "
    ),
    @NamedQuery(name = "CSR.findPendingGroupedByDay",
    query = "SELECT concat(YEAR(c.requestedOn), '.', MONTH(c.requestedOn), '.', DAY(c.requestedOn)), count(c) FROM CSR c WHERE " +
        " c.requestedOn >= :after and " +
        " c.requestedOn <= :before and " +
        " c.status = 'PENDING' " +
        " group by YEAR(c.requestedOn), MONTH(c.requestedOn), DAY(c.requestedOn)"
    ),

    @NamedQuery(name = "CSR.groupIssuedByIssuanceMonth",
        query = "SELECT concat(MONTH(requested_on), '.',YEAR(requested_on)), pipelineType, count(c) FROM CSR c WHERE " +
            " c.status = 'ISSUED' and" +
            " c.requestedOn > :after" +
            " group by MONTH(requested_on), YEAR(requested_on), pipelineType"
    ),
})
public class CSR implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "csr_base_64", nullable = false)
    private String csrBase64;

    @NotNull
    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "sans")
    private String sans;

    @NotNull
    @Column(name = "requested_on", nullable = false)
    private Instant requestedOn;

    @NotNull
    @Column(name = "requested_by", nullable = false)
    private String requestedBy;

    @Column(name = "accepted_by")
    private String acceptedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "pipeline_type", nullable = false)
    private PipelineType pipelineType;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CsrStatus status;

    @Column(name = "administered_by")
    private String administeredBy;

    @Column(name = "approved_on")
    private Instant approvedOn;

    @Column(name = "rejected_on")
    private Instant rejectedOn;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "process_instance_id")
    private String processInstanceId;

    @Column(name = "signing_algorithm")
    private String signingAlgorithm;

    @Column(name = "is_csr_valid")
    private Boolean isCSRValid;

    @Column(name = "x_509_key_spec")
    private String x509KeySpec;

    @Column(name = "public_key_algorithm")
    private String publicKeyAlgorithm;

    @Column(name = "key_algorithm")
    private String keyAlgorithm;

    @Column(name = "key_length")
    private Integer keyLength;

    @Column(name = "public_key_hash")
    private String publicKeyHash;

    @Column(name = "serverside_key_generation")
    private Boolean serversideKeyGeneration;

    @Lob
    @Column(name = "subject_public_key_info_base_64", nullable = false)
    private String subjectPublicKeyInfoBase64;

    @Lob
    @Column(name = "requestor_comment")
    private String requestorComment;

    @Lob
    @Column(name = "administration_comment")
    private String administrationComment;

    @JsonIgnoreProperties(value = { "csr" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private CSRComment comment;

    @OneToMany(mappedBy = "csr", fetch = FetchType.LAZY)
    private Set<RDN> rdns = new HashSet<>();

    @OneToMany(mappedBy = "csr", fetch = FetchType.LAZY)
    private Set<RequestAttribute> ras = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "csr", cascade = {CascadeType.ALL})
    @JsonIgnoreProperties({"csr"})
    private Set<CsrAttribute> csrAttributes = new HashSet<>();

    @ManyToOne
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("cSRS")
    private Pipeline pipeline;

    @OneToOne(mappedBy = "csr")
    @JsonIgnore
    private Certificate certificate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CSR id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCsrBase64() {
        return this.csrBase64;
    }

    public CSR csrBase64(String csrBase64) {
        this.setCsrBase64(csrBase64);
        return this;
    }

    public void setCsrBase64(String csrBase64) {
        this.csrBase64 = csrBase64;
    }

    public String getSubject() {
        return this.subject;
    }

    public CSR subject(String subject) {
        this.setSubject(subject);
        return this;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSans() {
        return this.sans;
    }

    public CSR sans(String sans) {
        this.setSans(sans);
        return this;
    }

    public void setSans(String sans) {
        this.sans = sans;
    }

    public Instant getRequestedOn() {
        return this.requestedOn;
    }

    public CSR requestedOn(Instant requestedOn) {
        this.setRequestedOn(requestedOn);
        return this;
    }

    public void setRequestedOn(Instant requestedOn) {
        this.requestedOn = requestedOn;
    }

    public String getRequestedBy() {
        return this.requestedBy;
    }

    public CSR requestedBy(String requestedBy) {
        this.setRequestedBy(requestedBy);
        return this;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public PipelineType getPipelineType() {
        return this.pipelineType;
    }

    public CSR pipelineType(PipelineType pipelineType) {
        this.setPipelineType(pipelineType);
        return this;
    }

    public void setPipelineType(PipelineType pipelineType) {
        this.pipelineType = pipelineType;
    }

    public CsrStatus getStatus() {
        return this.status;
    }

    public CSR status(CsrStatus status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(CsrStatus status) {
        this.status = status;
    }

    public String getAdministeredBy() {
        return this.administeredBy;
    }

    public CSR administeredBy(String administeredBy) {
        this.setAdministeredBy(administeredBy);
        return this;
    }

    public void setAdministeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
    }

    public Instant getApprovedOn() {
        return this.approvedOn;
    }

    public CSR approvedOn(Instant approvedOn) {
        this.setApprovedOn(approvedOn);
        return this;
    }

    public void setApprovedOn(Instant approvedOn) {
        this.approvedOn = approvedOn;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public Instant getRejectedOn() {
        return this.rejectedOn;
    }

    public CSR rejectedOn(Instant rejectedOn) {
        this.setRejectedOn(rejectedOn);
        return this;
    }

    public void setRejectedOn(Instant rejectedOn) {
        this.rejectedOn = rejectedOn;
    }

    public String getRejectionReason() {
        return this.rejectionReason;
    }

    public CSR rejectionReason(String rejectionReason) {
        this.setRejectionReason(rejectionReason);
        return this;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getProcessInstanceId() {
        return this.processInstanceId;
    }

    public CSR processInstanceId(String processInstanceId) {
        this.setProcessInstanceId(processInstanceId);
        return this;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getSigningAlgorithm() {
        return this.signingAlgorithm;
    }

    public CSR signingAlgorithm(String signingAlgorithm) {
        this.setSigningAlgorithm(signingAlgorithm);
        return this;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public Boolean getIsCSRValid() {
        return this.isCSRValid;
    }

    public CSR isCSRValid(Boolean isCSRValid) {
        this.setIsCSRValid(isCSRValid);
        return this;
    }

    public void setIsCSRValid(Boolean isCSRValid) {
        this.isCSRValid = isCSRValid;
    }

    public String getx509KeySpec() {
        return this.x509KeySpec;
    }

    public CSR x509KeySpec(String x509KeySpec) {
        this.setx509KeySpec(x509KeySpec);
        return this;
    }

    public void setx509KeySpec(String x509KeySpec) {
        this.x509KeySpec = x509KeySpec;
    }

    public String getPublicKeyAlgorithm() {
        return this.publicKeyAlgorithm;
    }

    public CSR publicKeyAlgorithm(String publicKeyAlgorithm) {
        this.setPublicKeyAlgorithm(publicKeyAlgorithm);
        return this;
    }

    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }

    public CSR keyAlgorithm(String keyAlgorithm) {
        this.setKeyAlgorithm(keyAlgorithm);
        return this;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeyLength() {
        return this.keyLength;
    }

    public CSR keyLength(Integer keyLength) {
        this.setKeyLength(keyLength);
        return this;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }

    public String getPublicKeyHash() {
        return this.publicKeyHash;
    }

    public CSR publicKeyHash(String publicKeyHash) {
        this.setPublicKeyHash(publicKeyHash);
        return this;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public Boolean isServersideKeyGeneration() {
        return serversideKeyGeneration;
    }

    public Boolean getServersideKeyGeneration() {
        return this.serversideKeyGeneration;
    }

    public CSR serversideKeyGeneration(Boolean serversideKeyGeneration) {
        this.setServersideKeyGeneration(serversideKeyGeneration);
        return this;
    }

    public void setServersideKeyGeneration(Boolean serversideKeyGeneration) {
        this.serversideKeyGeneration = serversideKeyGeneration;
    }

    public String getSubjectPublicKeyInfoBase64() {
        return this.subjectPublicKeyInfoBase64;
    }

    public CSR subjectPublicKeyInfoBase64(String subjectPublicKeyInfoBase64) {
        this.setSubjectPublicKeyInfoBase64(subjectPublicKeyInfoBase64);
        return this;
    }

    public void setSubjectPublicKeyInfoBase64(String subjectPublicKeyInfoBase64) {
        this.subjectPublicKeyInfoBase64 = subjectPublicKeyInfoBase64;
    }

    public String getRequestorComment() {
        return this.requestorComment;
    }

    public CSR requestorComment(String requestorComment) {
        this.setRequestorComment(requestorComment);
        return this;
    }

    public void setRequestorComment(String requestorComment) {
        this.requestorComment = requestorComment;
    }

    public String getAdministrationComment() {
        return this.administrationComment;
    }

    public CSR administrationComment(String administrationComment) {
        this.setAdministrationComment(administrationComment);
        return this;
    }

    public void setAdministrationComment(String administrationComment) {
        this.administrationComment = administrationComment;
    }

    public CSRComment getComment() {
        return this.comment;
    }

    public void setComment(CSRComment cSRComment) {
        this.comment = cSRComment;
    }

    public CSR comment(CSRComment cSRComment) {
        this.setComment(cSRComment);
        return this;
    }

    public Set<RDN> getRdns() {
        return this.rdns;
    }

    public void setRdns(Set<RDN> rDNS) {
        if (this.rdns != null) {
            this.rdns.forEach(i -> i.setCsr(null));
        }
        if (rDNS != null) {
            rDNS.forEach(i -> i.setCsr(this));
        }
        this.rdns = rDNS;
    }

    public CSR rdns(Set<RDN> rDNS) {
        this.setRdns(rDNS);
        return this;
    }

    public CSR addRdns(RDN rDN) {
        this.rdns.add(rDN);
        rDN.setCsr(this);
        return this;
    }

    public CSR removeRdns(RDN rDN) {
        this.rdns.remove(rDN);
        rDN.setCsr(null);
        return this;
    }

    public Set<RequestAttribute> getRas() {
        return this.ras;
    }

    public void setRas(Set<RequestAttribute> requestAttributes) {
        if (this.ras != null) {
            this.ras.forEach(i -> i.setCsr(null));
        }
        if (requestAttributes != null) {
            requestAttributes.forEach(i -> i.setCsr(this));
        }
        this.ras = requestAttributes;
    }

    public CSR ras(Set<RequestAttribute> requestAttributes) {
        this.setRas(requestAttributes);
        return this;
    }

    public CSR addRas(RequestAttribute requestAttribute) {
        this.ras.add(requestAttribute);
        requestAttribute.setCsr(this);
        return this;
    }

    public CSR removeRas(RequestAttribute requestAttribute) {
        this.ras.remove(requestAttribute);
        requestAttribute.setCsr(null);
        return this;
    }

    public Set<CsrAttribute> getCsrAttributes() {
        return this.csrAttributes;
    }

    public void setCsrAttributes(Set<CsrAttribute> csrAttributes) {
        if (this.csrAttributes != null) {
            this.csrAttributes.forEach(i -> i.setCsr(null));
        }
        if (csrAttributes != null) {
            csrAttributes.forEach(i -> i.setCsr(this));
        }
        this.csrAttributes = csrAttributes;
    }

    public CSR csrAttributes(Set<CsrAttribute> csrAttributes) {
        this.setCsrAttributes(csrAttributes);
        return this;
    }

    public CSR addCsrAttributes(CsrAttribute csrAttribute) {
        this.csrAttributes.add(csrAttribute);
        csrAttribute.setCsr(this);
        return this;
    }

    public CSR removeCsrAttributes(CsrAttribute csrAttribute) {
        this.csrAttributes.remove(csrAttribute);
        csrAttribute.setCsr(null);
        return this;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public CSR pipeline(Pipeline pipeline) {
        this.setPipeline(pipeline);
        return this;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        if (this.certificate != null) {
            this.certificate.setCsr(null);
        }
        if (certificate != null) {
            certificate.setCsr(this);
        }
        this.certificate = certificate;
    }

    public CSR certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    public Tenant getTenant() {
        return this.tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public CSR tenant(Tenant tenant) {
        this.setTenant(tenant);
        return this;
    }


    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CSR)) {
            return false;
        }
        return id != null && id.equals(((CSR) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CSR{" +
            "id=" + getId() +
            ", csrBase64='" + getCsrBase64() + "'" +
            ", subject='" + getSubject() + "'" +
            ", sans='" + getSans() + "'" +
            ", requestedOn='" + getRequestedOn() + "'" +
            ", requestedBy='" + getRequestedBy() + "'" +
            ", acceptedBy='" + getAcceptedBy() + "'" +
            ", pipelineType='" + getPipelineType() + "'" +
            ", status='" + getStatus() + "'" +
            ", administeredBy='" + getAdministeredBy() + "'" +
            ", approvedOn='" + getApprovedOn() + "'" +
            ", rejectedOn='" + getRejectedOn() + "'" +
            ", rejectionReason='" + getRejectionReason() + "'" +
            ", processInstanceId='" + getProcessInstanceId() + "'" +
            ", signingAlgorithm='" + getSigningAlgorithm() + "'" +
            ", isCSRValid='" + getIsCSRValid() + "'" +
            ", x509KeySpec='" + getx509KeySpec() + "'" +
            ", publicKeyAlgorithm='" + getPublicKeyAlgorithm() + "'" +
            ", keyAlgorithm='" + getKeyAlgorithm() + "'" +
            ", keyLength=" + getKeyLength() +
            ", publicKeyHash='" + getPublicKeyHash() + "'" +
            ", serversideKeyGeneration='" + getServersideKeyGeneration() + "'" +
            ", subjectPublicKeyInfoBase64='" + getSubjectPublicKeyInfoBase64() + "'" +
            ", requestorComment='" + getRequestorComment() + "'" +
            ", administrationComment='" + getAdministrationComment() + "'" +
            "}";
    }
}
