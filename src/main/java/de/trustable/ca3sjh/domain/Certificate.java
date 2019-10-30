package de.trustable.ca3sjh.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * A Certificate.
 */
@Entity
@Table(name = "certificate",
indexes = {@Index(name = "index_serial",  columnList="serial", unique = false),
        @Index(name = "index_tbs_digest", columnList="tbs_digest", unique = false)})
@NamedQueries({
	@NamedQuery(name = "Certificate.findByTBSDigest",
	query = "SELECT c FROM Certificate c WHERE " +
			"c.tbsDigest = :tbsDigest"
 ),    
 @NamedQuery(name = "Certificate.findByIssuerSerial",
 query = "SELECT c FROM Certificate c WHERE " +
         "LOWER(c.issuer) = LOWER( :issuer ) AND " +
         " c.serial = :serial"
 ),
 @NamedQuery(name = "Certificate.findBySearchTermNamed",
 query = "SELECT c FROM Certificate c WHERE " +
     "LOWER(c.subject) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
     " c.serial = :serial"
 ),
 @NamedQuery(name = "Certificate.findByAttributeValue",
 query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
     " att1.name = :name and att1.value = :value " 
 ),
 @NamedQuery(name = "Certificate.findByAttributeValue_",
 query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
         "att1.name = :name and " +
         "att1.value LIKE LOWER(CONCAT( :value, '%'))"
 ),
 @NamedQuery(name = "Certificate.findBySearchTermNamed1",
 query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
     " att1.name = :name and att1.value like CONCAT( :value, '%')"
     ),
 @NamedQuery(name = "Certificate.findBySearchTermNamed2",
 query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 JOIN c.certificateAttributes att2  WHERE " +
     " att1.name = :name1 and att1.value like CONCAT( :value1, '%') AND" +
     " att2.name = :name2 and att2.value like CONCAT( :value2, '%') "
     ),
         
 @NamedQuery(name = "Certificate.findByTermNamed2",
 query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 JOIN c.certificateAttributes att2  WHERE " +
     " att1.name = :name1 and att1.value = :value1 AND" +
     " att2.name = :name2 and att2.value = :value2 "
     ),
     
     
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Certificate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "tbs_digest", nullable = false)
    private String tbsDigest;

    @NotNull
    @Column(name = "subject", nullable = false)
    private String subject;

    @NotNull
    @Column(name = "issuer", nullable = false)
    private String issuer;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "subject_key_identifier")
    private String subjectKeyIdentifier;

    @Column(name = "authority_key_identifier")
    private String authorityKeyIdentifier;

    @Column(name = "fingerprint")
    private String fingerprint;

    @NotNull
    @Column(name = "serial", nullable = false)
    private String serial;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private LocalDate validTo;

    @Column(name = "creation_execution_id")
    private String creationExecutionId;

    @Column(name = "content_added_at")
    private LocalDate contentAddedAt;

    @Column(name = "revoked_since")
    private LocalDate revokedSince;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "revoked")
    private Boolean revoked;

    @Column(name = "revocation_execution_id")
    private String revocationExecutionId;

    
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @OneToMany(mappedBy = "certificate")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<CertificateAttribute> certificateAttributes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTbsDigest() {
        return tbsDigest;
    }

    public Certificate tbsDigest(String tbsDigest) {
        this.tbsDigest = tbsDigest;
        return this;
    }

    public void setTbsDigest(String tbsDigest) {
        this.tbsDigest = tbsDigest;
    }

    public String getSubject() {
        return subject;
    }

    public Certificate subject(String subject) {
        this.subject = subject;
        return this;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getIssuer() {
        return issuer;
    }

    public Certificate issuer(String issuer) {
        this.issuer = issuer;
        return this;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getType() {
        return type;
    }

    public Certificate type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public Certificate description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubjectKeyIdentifier() {
        return subjectKeyIdentifier;
    }

    public Certificate subjectKeyIdentifier(String subjectKeyIdentifier) {
        this.subjectKeyIdentifier = subjectKeyIdentifier;
        return this;
    }

    public void setSubjectKeyIdentifier(String subjectKeyIdentifier) {
        this.subjectKeyIdentifier = subjectKeyIdentifier;
    }

    public String getAuthorityKeyIdentifier() {
        return authorityKeyIdentifier;
    }

    public Certificate authorityKeyIdentifier(String authorityKeyIdentifier) {
        this.authorityKeyIdentifier = authorityKeyIdentifier;
        return this;
    }

    public void setAuthorityKeyIdentifier(String authorityKeyIdentifier) {
        this.authorityKeyIdentifier = authorityKeyIdentifier;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public Certificate fingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
        return this;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getSerial() {
        return serial;
    }

    public Certificate serial(String serial) {
        this.serial = serial;
        return this;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public Certificate validFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
        return this;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public Certificate validTo(LocalDate validTo) {
        this.validTo = validTo;
        return this;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    public String getCreationExecutionId() {
        return creationExecutionId;
    }

    public Certificate creationExecutionId(String creationExecutionId) {
        this.creationExecutionId = creationExecutionId;
        return this;
    }

    public void setCreationExecutionId(String creationExecutionId) {
        this.creationExecutionId = creationExecutionId;
    }

    public LocalDate getContentAddedAt() {
        return contentAddedAt;
    }

    public Certificate contentAddedAt(LocalDate contentAddedAt) {
        this.contentAddedAt = contentAddedAt;
        return this;
    }

    public void setContentAddedAt(LocalDate contentAddedAt) {
        this.contentAddedAt = contentAddedAt;
    }

    public LocalDate getRevokedSince() {
        return revokedSince;
    }

    public Certificate revokedSince(LocalDate revokedSince) {
        this.revokedSince = revokedSince;
        return this;
    }

    public void setRevokedSince(LocalDate revokedSince) {
        this.revokedSince = revokedSince;
    }

    public String getRevocationReason() {
        return revocationReason;
    }

    public Certificate revocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
        return this;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public Boolean isRevoked() {
        return revoked;
    }

    public Certificate revoked(Boolean revoked) {
        this.revoked = revoked;
        return this;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public String getRevocationExecutionId() {
        return revocationExecutionId;
    }

    public Certificate revocationExecutionId(String revocationExecutionId) {
        this.revocationExecutionId = revocationExecutionId;
        return this;
    }

    public void setRevocationExecutionId(String revocationExecutionId) {
        this.revocationExecutionId = revocationExecutionId;
    }

    public String getContent() {
        return content;
    }

    public Certificate content(String content) {
        this.content = content;
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Set<CertificateAttribute> getCertificateAttributes() {
        return certificateAttributes;
    }

    public Certificate certificateAttributes(Set<CertificateAttribute> certificateAttributes) {
        this.certificateAttributes = certificateAttributes;
        return this;
    }

    public Certificate addCertificateAttributes(CertificateAttribute certificateAttribute) {
        this.certificateAttributes.add(certificateAttribute);
        certificateAttribute.setCertificate(this);
        return this;
    }

    public Certificate removeCertificateAttributes(CertificateAttribute certificateAttribute) {
        this.certificateAttributes.remove(certificateAttribute);
        certificateAttribute.setCertificate(null);
        return this;
    }

    public void setCertificateAttributes(Set<CertificateAttribute> certificateAttributes) {
        this.certificateAttributes = certificateAttributes;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Certificate)) {
            return false;
        }
        return id != null && id.equals(((Certificate) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Certificate{" +
            "id=" + getId() +
            ", tbsDigest='" + getTbsDigest() + "'" +
            ", subject='" + getSubject() + "'" +
            ", issuer='" + getIssuer() + "'" +
            ", type='" + getType() + "'" +
            ", description='" + getDescription() + "'" +
            ", subjectKeyIdentifier='" + getSubjectKeyIdentifier() + "'" +
            ", authorityKeyIdentifier='" + getAuthorityKeyIdentifier() + "'" +
            ", fingerprint='" + getFingerprint() + "'" +
            ", serial='" + getSerial() + "'" +
            ", validFrom='" + getValidFrom() + "'" +
            ", validTo='" + getValidTo() + "'" +
            ", creationExecutionId='" + getCreationExecutionId() + "'" +
            ", contentAddedAt='" + getContentAddedAt() + "'" +
            ", revokedSince='" + getRevokedSince() + "'" +
            ", revocationReason='" + getRevocationReason() + "'" +
            ", revoked='" + isRevoked() + "'" +
            ", revocationExecutionId='" + getRevocationExecutionId() + "'" +
            ", content='" + getContent() + "'" +
            "}";
    }
}
