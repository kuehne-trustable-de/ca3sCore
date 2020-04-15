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
    @NamedQuery(name = "CSR.countAll",
    query = "SELECT count(c) FROM CSR c "
    ),
    @NamedQuery(name = "CSR.findPendingGroupedByDay",
    query = "SELECT concat(YEAR(c.requestedOn), '.', MONTH(c.requestedOn), '.', DAY(c.requestedOn)), count(c) FROM CSR c WHERE " +
        "c.requestedOn >= :after and " +
        " c.requestedOn <= :before and " +
        " c.status = 'PENDING' " +
        " group by YEAR(c.requestedOn), MONTH(c.requestedOn), DAY(c.requestedOn)"
    )
})
public class CSR implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @OneToMany(mappedBy = "csr")
    private Set<RDN> rdns = new HashSet<>();

    @OneToMany(mappedBy = "csr")
    private Set<RequestAttribute> ras = new HashSet<>();

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "csr", cascade = {CascadeType.ALL})
    @JsonIgnoreProperties({"csr"})
    private Set<CsrAttribute> csrAttributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("cSRS")
    private Pipeline pipeline;

    @OneToOne(mappedBy = "csr")
    @JsonIgnore
    private Certificate certificate;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCsrBase64() {
        return csrBase64;
    }

    public CSR csrBase64(String csrBase64) {
        this.csrBase64 = csrBase64;
        return this;
    }

    public void setCsrBase64(String csrBase64) {
        this.csrBase64 = csrBase64;
    }

    public String getSubject() {
        return subject;
    }

    public CSR subject(String subject) {
        this.subject = subject;
        return this;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSans() {
        return sans;
    }

    public CSR sans(String sans) {
        this.sans = sans;
        return this;
    }

    public void setSans(String sans) {
        this.sans = sans;
    }

    public Instant getRequestedOn() {
        return requestedOn;
    }

    public CSR requestedOn(Instant requestedOn) {
        this.requestedOn = requestedOn;
        return this;
    }

    public void setRequestedOn(Instant requestedOn) {
        this.requestedOn = requestedOn;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public CSR requestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
        return this;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public PipelineType getPipelineType() {
        return pipelineType;
    }

    public CSR pipelineType(PipelineType pipelineType) {
        this.pipelineType = pipelineType;
        return this;
    }

    public void setPipelineType(PipelineType pipelineType) {
        this.pipelineType = pipelineType;
    }

    public CsrStatus getStatus() {
        return status;
    }

    public CSR status(CsrStatus status) {
        this.status = status;
        return this;
    }

    public void setStatus(CsrStatus status) {
        this.status = status;
    }

    public String getAdministeredBy() {
        return administeredBy;
    }

    public CSR administeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
        return this;
    }

    public void setAdministeredBy(String administeredBy) {
        this.administeredBy = administeredBy;
    }

    public Instant getApprovedOn() {
        return approvedOn;
    }

    public CSR approvedOn(Instant approvedOn) {
        this.approvedOn = approvedOn;
        return this;
    }

    public void setApprovedOn(Instant approvedOn) {
        this.approvedOn = approvedOn;
    }

    public Instant getRejectedOn() {
        return rejectedOn;
    }

    public CSR rejectedOn(Instant rejectedOn) {
        this.rejectedOn = rejectedOn;
        return this;
    }

    public void setRejectedOn(Instant rejectedOn) {
        this.rejectedOn = rejectedOn;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public CSR rejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
        return this;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public CSR processInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
        return this;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getSigningAlgorithm() {
        return signingAlgorithm;
    }

    public CSR signingAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
        return this;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public Boolean isIsCSRValid() {
        return isCSRValid;
    }

    public CSR isCSRValid(Boolean isCSRValid) {
        this.isCSRValid = isCSRValid;
        return this;
    }

    public void setIsCSRValid(Boolean isCSRValid) {
        this.isCSRValid = isCSRValid;
    }

    public String getx509KeySpec() {
        return x509KeySpec;
    }

    public CSR x509KeySpec(String x509KeySpec) {
        this.x509KeySpec = x509KeySpec;
        return this;
    }

    public void setx509KeySpec(String x509KeySpec) {
        this.x509KeySpec = x509KeySpec;
    }

    public String getPublicKeyAlgorithm() {
        return publicKeyAlgorithm;
    }

    public CSR publicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
        return this;
    }

    public void setPublicKeyAlgorithm(String publicKeyAlgorithm) {
        this.publicKeyAlgorithm = publicKeyAlgorithm;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public CSR keyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
        return this;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeyLength() {
        return keyLength;
    }

    public CSR keyLength(Integer keyLength) {
        this.keyLength = keyLength;
        return this;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }

    public String getPublicKeyHash() {
        return publicKeyHash;
    }

    public CSR publicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
        return this;
    }

    public void setPublicKeyHash(String publicKeyHash) {
        this.publicKeyHash = publicKeyHash;
    }

    public Boolean isServersideKeyGeneration() {
        return serversideKeyGeneration;
    }

    public CSR serversideKeyGeneration(Boolean serversideKeyGeneration) {
        this.serversideKeyGeneration = serversideKeyGeneration;
        return this;
    }

    public void setServersideKeyGeneration(Boolean serversideKeyGeneration) {
        this.serversideKeyGeneration = serversideKeyGeneration;
    }

    public String getSubjectPublicKeyInfoBase64() {
        return subjectPublicKeyInfoBase64;
    }

    public CSR subjectPublicKeyInfoBase64(String subjectPublicKeyInfoBase64) {
        this.subjectPublicKeyInfoBase64 = subjectPublicKeyInfoBase64;
        return this;
    }

    public void setSubjectPublicKeyInfoBase64(String subjectPublicKeyInfoBase64) {
        this.subjectPublicKeyInfoBase64 = subjectPublicKeyInfoBase64;
    }

    public String getRequestorComment() {
        return requestorComment;
    }

    public CSR requestorComment(String requestorComment) {
        this.requestorComment = requestorComment;
        return this;
    }

    public void setRequestorComment(String requestorComment) {
        this.requestorComment = requestorComment;
    }

    public String getAdministrationComment() {
        return administrationComment;
    }

    public CSR administrationComment(String administrationComment) {
        this.administrationComment = administrationComment;
        return this;
    }

    public void setAdministrationComment(String administrationComment) {
        this.administrationComment = administrationComment;
    }

    public Set<RDN> getRdns() {
        return rdns;
    }

    public CSR rdns(Set<RDN> rDNS) {
        this.rdns = rDNS;
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

    public void setRdns(Set<RDN> rDNS) {
        this.rdns = rDNS;
    }

    public Set<RequestAttribute> getRas() {
        return ras;
    }

    public CSR ras(Set<RequestAttribute> requestAttributes) {
        this.ras = requestAttributes;
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

    public void setRas(Set<RequestAttribute> requestAttributes) {
        this.ras = requestAttributes;
    }

    public Set<CsrAttribute> getCsrAttributes() {
        return csrAttributes;
    }

    public CSR csrAttributes(Set<CsrAttribute> csrAttributes) {
        this.csrAttributes = csrAttributes;
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

    public void setCsrAttributes(Set<CsrAttribute> csrAttributes) {
        this.csrAttributes = csrAttributes;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public CSR pipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
        return this;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public CSR certificate(Certificate certificate) {
        this.certificate = certificate;
        return this;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        return 31;
    }

    @Override
    public String toString() {
        return "CSR{" +
            "id=" + getId() +
            ", csrBase64='" + getCsrBase64() + "'" +
            ", subject='" + getSubject() + "'" +
            ", sans='" + getSans() + "'" +
            ", requestedOn='" + getRequestedOn() + "'" +
            ", requestedBy='" + getRequestedBy() + "'" +
            ", pipelineType='" + getPipelineType() + "'" +
            ", status='" + getStatus() + "'" +
            ", administeredBy='" + getAdministeredBy() + "'" +
            ", approvedOn='" + getApprovedOn() + "'" +
            ", rejectedOn='" + getRejectedOn() + "'" +
            ", rejectionReason='" + getRejectionReason() + "'" +
            ", processInstanceId='" + getProcessInstanceId() + "'" +
            ", signingAlgorithm='" + getSigningAlgorithm() + "'" +
            ", isCSRValid='" + isIsCSRValid() + "'" +
            ", x509KeySpec='" + getx509KeySpec() + "'" +
            ", publicKeyAlgorithm='" + getPublicKeyAlgorithm() + "'" +
            ", keyAlgorithm='" + getKeyAlgorithm() + "'" +
            ", keyLength=" + getKeyLength() +
            ", publicKeyHash='" + getPublicKeyHash() + "'" +
            ", serversideKeyGeneration='" + isServersideKeyGeneration() + "'" +
            ", subjectPublicKeyInfoBase64='" + getSubjectPublicKeyInfoBase64() + "'" +
            ", requestorComment='" + getRequestorComment() + "'" +
            ", administrationComment='" + getAdministrationComment() + "'" +
            "}";
    }
}
