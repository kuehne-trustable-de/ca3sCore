package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3s.core.domain.enumeration.CsrStatus;

/**
 * A CSR.
 */
@Entity
@Table(name = "csr")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(name = "CSR.findByPublicKeyHash",
	query = "SELECT c FROM CSR c WHERE " +
			"c.publicKeyHash = :hash"
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
    @Column(name = "requested_on", nullable = false)
    private LocalDate requestedOn;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CsrStatus status;

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

    @Column(name = "public_key_hash")
    private String publicKeyHash;

    
    @Lob
    @Column(name = "subject_public_key_info_base_64", nullable = false)
    private String subjectPublicKeyInfoBase64;

    @OneToMany(mappedBy = "csr")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<RDN> rdns = new HashSet<>();

    @OneToMany(mappedBy = "csr")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<RequestAttribute> ras = new HashSet<>();

    @OneToMany(mappedBy = "csr")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

    public LocalDate getRequestedOn() {
        return requestedOn;
    }

    public CSR requestedOn(LocalDate requestedOn) {
        this.requestedOn = requestedOn;
        return this;
    }

    public void setRequestedOn(LocalDate requestedOn) {
        this.requestedOn = requestedOn;
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
            ", requestedOn='" + getRequestedOn() + "'" +
            ", status='" + getStatus() + "'" +
            ", processInstanceId='" + getProcessInstanceId() + "'" +
            ", signingAlgorithm='" + getSigningAlgorithm() + "'" +
            ", isCSRValid='" + isIsCSRValid() + "'" +
            ", x509KeySpec='" + getx509KeySpec() + "'" +
            ", publicKeyAlgorithm='" + getPublicKeyAlgorithm() + "'" +
            ", publicKeyHash='" + getPublicKeyHash() + "'" +
            ", subjectPublicKeyInfoBase64='" + getSubjectPublicKeyInfoBase64() + "'" +
            "}";
    }
}
