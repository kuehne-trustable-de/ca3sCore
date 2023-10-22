package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Certificate.
 */
@Entity
@Table(name = "certificate")
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
    @NamedQuery(name = "Certificate.findCACertByIssuer",
    query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
    	"c.subject = :issuer AND " +
        " att1.name = 'CA3S:CA' and LOWER(att1.value) = 'true' "
    ),
    @NamedQuery(name = "Certificate.findBySearchTermNamed",
    query = "SELECT c FROM Certificate c WHERE " +
        "LOWER(c.subject) LIKE LOWER(CONCAT('%', :subject, '%')) OR " +
        " c.serial = :serial"
    ),

    @NamedQuery(name = "Certificate.findActiveByAttributeValue",
        query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
            " att1.name = :name and att1.value = :value AND " +
            " c.active = TRUE "
    ),
    @NamedQuery(name = "Certificate.findByAttributeValue",
        query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
            " att1.name = :name and att1.value = :value "
    ),
    @NamedQuery(name = "Certificate.findByAttributeValueLowerThan",
        query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
            " att1.name = :name and att1.value < :value "
    ),
    @NamedQuery(name = "Certificate.findByAttributeValue_",
    query = "SELECT distinct c FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
            "att1.name = :name and " +
            "att1.value LIKE LOWER(CONCAT( :value, '%')" +
        " )"
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
    @NamedQuery(name = "Certificate.findByValidTo",
    query = "SELECT c FROM Certificate c WHERE " +
        " c.validTo >= :after and " +
        " c.validTo <= :before and " +
        " c.revoked = FALSE " +
        " order by c.validTo asc"
    ),
    @NamedQuery(name = "Certificate.findByValidToGroupedByDay",
        query = "SELECT concat(YEAR(c.validTo), '.', MONTH(c.validTo), '.', DAY(c.validTo)), count(c) FROM Certificate c WHERE " +
            "c.validTo >= :after and " +
            " c.validTo <= :before " +
            " group by YEAR(c.validTo), MONTH(c.validTo), DAY(c.validTo)"
    ),
    @NamedQuery(name = "Certificate.findByTypeAndValidTo",
        query = "SELECT c FROM Certificate c WHERE " +
            " c.validTo >= :after and " +
            " c.validTo <= :before and " +
            " c.endEntity = :isEndEntity and " +
            " c.revoked = FALSE " +
            " order by c.validTo asc"
    ),
    @NamedQuery(name = "Certificate.countAll",
        query = "SELECT count(c) FROM Certificate c "
    ),
    @NamedQuery(name = "Certificate.findActiveCertificatesByHashAlgo",
    query = "SELECT c.hashingAlgorithm, count(c) as total FROM Certificate c WHERE " +
        "c.validTo >= :now and " +
        " c.validFrom <= :now and " +
        " c.revoked = FALSE " +
        " GROUP BY c.hashingAlgorithm ORDER BY c.hashingAlgorithm ASC"
    ),

    @NamedQuery(name = "Certificate.findActiveCertificatesByKeyAlgo",
    query = "SELECT c.keyAlgorithm, count(c) as total FROM Certificate c WHERE " +
        "c.validTo >= :now and " +
        " c.validFrom <= :now and " +
        " c.revoked = FALSE " +
        " GROUP BY c.keyAlgorithm ORDER BY c.keyAlgorithm ASC"
    ),

    @NamedQuery(name = "Certificate.findActiveCertificatesByKeyLength",
    query = "SELECT c.keyLength, count(c) as total FROM Certificate c WHERE " +
        "c.validTo >= :now and " +
        " c.validFrom <= :now and " +
        " c.revoked = FALSE " +
        " GROUP BY c.keyLength ORDER BY c.keyLength ASC"
    ),

    @NamedQuery(name = "Certificate.findActiveCertificatesBySANs",
    query = "SELECT c as total FROM Certificate c " +
    	" JOIN c.certificateAttributes certAtt " +
    	" WHERE " +
        " c.validTo >= :now and " +
        " c.validFrom <= :now and " +
        " c.revoked = FALSE and " +
        " ( certAtt.name = 'TYPED_SAN' or certAtt.name = 'TYPED_VSAN') and " +
        " certAtt.value in :sans " +
        " group by c " +
        " order by count(certAtt) desc"
    ),

    @NamedQuery(name = "Certificate.findInactiveCertificatesByValidFrom",
    query = "SELECT c FROM Certificate c WHERE " +
        "c.validFrom >= :now and " +
        "c.validTo < :now and " +
        "c.revoked = FALSE and " +
        "c.active = FALSE "
    ),
    @NamedQuery(name = "Certificate.findActiveCertificatesByValidTo",
        query = "SELECT c FROM Certificate c WHERE " +
            "c.validTo <= :now and " +
            "c.active = TRUE "
    ),
    @NamedQuery(name = "Certificate.findActiveTLSCertificate",
        query = "SELECT distinct c, certAtt.value FROM Certificate c JOIN c.certificateAttributes certAtt WHERE " +
            " certAtt.name = 'TLS_KEY' and " +
            " c.active = TRUE " +
            " order by c.validTo desc"
    ),
    @NamedQuery(name = "Certificate.findActiveCertificateOrderedByCrlURL",
        query = "SELECT distinct c, certAtt.value FROM Certificate c JOIN c.certificateAttributes certAtt WHERE " +
            " certAtt.name = 'CRL_URL' and " +
            " c.active = TRUE and" +
            " certAtt.value not like 'ldap%'" +
            " order by certAtt.value "
    ),
    @NamedQuery(name = "Certificate.findActiveCertificateBySerial",
        query = "SELECT distinct c FROM Certificate c WHERE " +
            " c.serial = :serial and " +
            " c.active = TRUE "
    ),
    @NamedQuery(name = "Certificate.findCrlURLForActiveCertificates",
        query = "SELECT distinct certAtt.value FROM Certificate c JOIN c.certificateAttributes certAtt WHERE " +
            " certAtt.name = 'CRL_URL' and " +
            " c.active = TRUE " +
            " order by certAtt.value "
    ),
    @NamedQuery(name = "Certificate.findTimestampNotExistForCA",
    query = "SELECT c  FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
        " att1.name = '" +CertificateAttribute.ATTRIBUTE_PROCESSING_CA+"' and att1.value = :caName AND" +
        " NOT EXISTS (select att2 from CertificateAttribute att2 WHERE att2.certificate = c AND " +
        " att2.name = :timestamp )"
    ),
    @NamedQuery(name = "Certificate.findMaxTimestampForCA",
        query = "SELECT max(att2.value)  FROM Certificate c JOIN c.certificateAttributes att1 JOIN c.certificateAttributes att2  WHERE " +
            " att1.name = '" +CertificateAttribute.ATTRIBUTE_PROCESSING_CA+"' and att1.value = :caName AND" +
            " att2.name = :timestamp"
    ),
    @NamedQuery(name = "Certificate.findByRequestor",
        query = "SELECT c  FROM Certificate c JOIN c.certificateAttributes att1 WHERE " +
            " att1.name = '" +CertificateAttribute.ATTRIBUTE_REQUESTED_BY+"' and att1.value = :requestor"
    ),

})
public class Certificate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tbs_digest", nullable = false)
    private String tbsDigest;

    @NotNull
    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "sans")
    private String sans;

    @NotNull
    @Column(name = "issuer", nullable = false)
    private String issuer;

    @Column(name = "root")
    private String root;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "fingerprint")
    private String fingerprint;

    @NotNull
    @Column(name = "serial", nullable = false)
    private String serial;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @Column(name = "key_algorithm")
    private String keyAlgorithm;

    @Column(name = "key_length")
    private Integer keyLength;

    @Column(name = "curve_name")
    private String curveName;

    @Column(name = "hashing_algorithm")
    private String hashingAlgorithm;

    @Column(name = "padding_algorithm")
    private String paddingAlgorithm;

    @Column(name = "signing_algorithm")
    private String signingAlgorithm;

    @Column(name = "creation_execution_id")
    private String creationExecutionId;

    @Column(name = "content_added_at")
    private Instant contentAddedAt;

    @Column(name = "revoked_since")
    private Instant revokedSince;

    @Column(name = "revocation_reason")
    private String revocationReason;

    @Column(name = "revoked")
    private Boolean revoked;

    @Column(name = "revocation_execution_id")
    private String revocationExecutionId;

    @Lob
    @Column(name = "administration_comment")
    private String administrationComment;

    @Column(name = "end_entity")
    private Boolean endEntity;

    @Column(name = "selfsigned")
    private Boolean selfsigned;

    @Column(name = "trusted")
    private Boolean trusted;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @JsonIgnoreProperties(value = { "comment", "rdns", "ras", "csrAttributes", "pipeline", "certificate" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private CSR csr;

    @JsonIgnoreProperties(value = { "certificate" }, allowSetters = true)
    @OneToOne
    @JoinColumn(unique = true)
    private CertificateComment comment;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "certificate", cascade = {CascadeType.ALL})
    private Set<CertificateAttribute> certificateAttributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"rootCertificate", "issuingCertificate", "certificateAttributes"})
    private Certificate issuingCertificate;

    @ManyToOne
    @JsonIgnoreProperties({"rootCertificate", "issuingCertificate", "certificateAttributes"})
    private Certificate rootCertificate;

    @ManyToOne
    @JsonIgnoreProperties(value = { "secret", "caConnectorAttributes", "tlsAuthentication", "messageProtection" }, allowSetters = true)
    private CAConnectorConfig revocationCA;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Certificate id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTbsDigest() {
        return this.tbsDigest;
    }

    public Certificate tbsDigest(String tbsDigest) {
        this.setTbsDigest(tbsDigest);
        return this;
    }

    public void setTbsDigest(String tbsDigest) {
        this.tbsDigest = tbsDigest;
    }

    public String getSubject() {
        return this.subject;
    }

    public Certificate subject(String subject) {
        this.setSubject(subject);
        return this;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSans() {
        return this.sans;
    }

    public Certificate sans(String sans) {
        this.setSans(sans);
        return this;
    }

    public void setSans(String sans) {
        this.sans = sans;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public Certificate issuer(String issuer) {
        this.setIssuer(issuer);
        return this;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getRoot() {
        return this.root;
    }

    public Certificate root(String root) {
        this.setRoot(root);
        return this;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getType() {
        return this.type;
    }

    public Certificate type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return this.description;
    }

    public Certificate description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFingerprint() {
        return this.fingerprint;
    }

    public Certificate fingerprint(String fingerprint) {
        this.setFingerprint(fingerprint);
        return this;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getSerial() {
        return this.serial;
    }

    public Certificate serial(String serial) {
        this.setSerial(serial);
        return this;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public Instant getValidFrom() {
        return this.validFrom;
    }

    public Certificate validFrom(Instant validFrom) {
        this.setValidFrom(validFrom);
        return this;
    }

    public void setValidFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    public Instant getValidTo() {
        return this.validTo;
    }

    public Certificate validTo(Instant validTo) {
        this.setValidTo(validTo);
        return this;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public String getKeyAlgorithm() {
        return this.keyAlgorithm;
    }

    public Certificate keyAlgorithm(String keyAlgorithm) {
        this.setKeyAlgorithm(keyAlgorithm);
        return this;
    }

    public void setKeyAlgorithm(String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public Integer getKeyLength() {
        return this.keyLength;
    }

    public Certificate keyLength(Integer keyLength) {
        this.setKeyLength(keyLength);
        return this;
    }

    public void setKeyLength(Integer keyLength) {
        this.keyLength = keyLength;
    }

    public String getCurveName() {
        return this.curveName;
    }

    public Certificate curveName(String curveName) {
        this.setCurveName(curveName);
        return this;
    }

    public void setCurveName(String curveName) {
        this.curveName = curveName;
    }

    public String getHashingAlgorithm() {
        return this.hashingAlgorithm;
    }

    public Certificate hashingAlgorithm(String hashingAlgorithm) {
        this.setHashingAlgorithm(hashingAlgorithm);
        return this;
    }

    public void setHashingAlgorithm(String hashingAlgorithm) {
        this.hashingAlgorithm = hashingAlgorithm;
    }

    public String getPaddingAlgorithm() {
        return this.paddingAlgorithm;
    }

    public Certificate paddingAlgorithm(String paddingAlgorithm) {
        this.setPaddingAlgorithm(paddingAlgorithm);
        return this;
    }

    public void setPaddingAlgorithm(String paddingAlgorithm) {
        this.paddingAlgorithm = paddingAlgorithm;
    }

    public String getSigningAlgorithm() {
        return this.signingAlgorithm;
    }

    public Certificate signingAlgorithm(String signingAlgorithm) {
        this.setSigningAlgorithm(signingAlgorithm);
        return this;
    }

    public void setSigningAlgorithm(String signingAlgorithm) {
        this.signingAlgorithm = signingAlgorithm;
    }

    public String getCreationExecutionId() {
        return this.creationExecutionId;
    }

    public Certificate creationExecutionId(String creationExecutionId) {
        this.setCreationExecutionId(creationExecutionId);
        return this;
    }

    public void setCreationExecutionId(String creationExecutionId) {
        this.creationExecutionId = creationExecutionId;
    }

    public Instant getContentAddedAt() {
        return this.contentAddedAt;
    }

    public Certificate contentAddedAt(Instant contentAddedAt) {
        this.setContentAddedAt(contentAddedAt);
        return this;
    }

    public void setContentAddedAt(Instant contentAddedAt) {
        this.contentAddedAt = contentAddedAt;
    }

    public Instant getRevokedSince() {
        return this.revokedSince;
    }

    public Certificate revokedSince(Instant revokedSince) {
        this.setRevokedSince(revokedSince);
        return this;
    }

    public void setRevokedSince(Instant revokedSince) {
        this.revokedSince = revokedSince;
    }

    public String getRevocationReason() {
        return this.revocationReason;
    }

    public Certificate revocationReason(String revocationReason) {
        this.setRevocationReason(revocationReason);
        return this;
    }

    public void setRevocationReason(String revocationReason) {
        this.revocationReason = revocationReason;
    }

    public boolean isRevoked() {
        return (revoked == null)?false:revoked;
    }

    public Boolean getRevoked() {
        return this.revoked;
    }

    public Certificate revoked(Boolean revoked) {
        this.setRevoked(revoked);
        return this;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }

    public String getRevocationExecutionId() {
        return this.revocationExecutionId;
    }

    public Certificate revocationExecutionId(String revocationExecutionId) {
        this.setRevocationExecutionId(revocationExecutionId);
        return this;
    }

    public void setRevocationExecutionId(String revocationExecutionId) {
        this.revocationExecutionId = revocationExecutionId;
    }

    public String getAdministrationComment() {
        return this.administrationComment;
    }

    public Certificate administrationComment(String administrationComment) {
        this.setAdministrationComment(administrationComment);
        return this;
    }

    public void setAdministrationComment(String administrationComment) {
        this.administrationComment = administrationComment;
    }

    public boolean isEndEntity() {
        return (endEntity == null)?false:endEntity;
    }

    public Boolean getEndEntity() {
        return this.endEntity;
    }

    public Certificate endEntity(Boolean endEntity) {
        this.setEndEntity(endEntity);
        return this;
    }

    public void setEndEntity(Boolean endEntity) {
        this.endEntity = endEntity;
    }

    public boolean isSelfsigned() {
        return (selfsigned == null)?false:selfsigned;
    }

    public Boolean getSelfsigned() {
        return this.selfsigned;
    }

    public Certificate selfsigned(Boolean selfsigned) {
        this.setSelfsigned(selfsigned);
        return this;
    }

    public void setSelfsigned(Boolean selfsigned) {
        this.selfsigned = selfsigned;
    }

    public boolean isTrusted() {
        return (trusted == null)?false:trusted;
    }

    public Boolean getTrusted() {
        return this.trusted;
    }

    public Certificate trusted(Boolean trusted) {
        this.setTrusted(trusted);
        return this;
    }

    public void setTrusted(Boolean trusted) {
        this.trusted = trusted;
    }

    public boolean isActive() {
        return (active == null)?false:active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Certificate active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getContent() {
        return this.content;
    }

    public Certificate content(String content) {
        this.setContent(content);
        return this;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public Certificate csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    public CertificateComment getComment() {
        return this.comment;
    }

    public void setComment(CertificateComment certificateComment) {
        this.comment = certificateComment;
    }

    public Certificate comment(CertificateComment certificateComment) {
        this.setComment(certificateComment);
        return this;
    }

    public Set<CertificateAttribute> getCertificateAttributes() {
        return this.certificateAttributes;
    }

    public void setCertificateAttributes(Set<CertificateAttribute> certificateAttributes) {
        if (this.certificateAttributes != null) {
            this.certificateAttributes.forEach(i -> i.setCertificate(null));
        }
        if (certificateAttributes != null) {
            certificateAttributes.forEach(i -> i.setCertificate(this));
        }
        this.certificateAttributes = certificateAttributes;
    }

    public Certificate certificateAttributes(Set<CertificateAttribute> certificateAttributes) {
        this.setCertificateAttributes(certificateAttributes);
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

    public Certificate getIssuingCertificate() {
        return this.issuingCertificate;
    }

    public void setIssuingCertificate(Certificate certificate) {
        this.issuingCertificate = certificate;
    }

    public Certificate issuingCertificate(Certificate certificate) {
        this.setIssuingCertificate(certificate);
        return this;
    }

    public Certificate getRootCertificate() {
        return this.rootCertificate;
    }

    public void setRootCertificate(Certificate certificate) {
        this.rootCertificate = certificate;
    }

    public Certificate rootCertificate(Certificate certificate) {
        this.setRootCertificate(certificate);
        return this;
    }

    public CAConnectorConfig getRevocationCA() {
        return this.revocationCA;
    }

    public void setRevocationCA(CAConnectorConfig cAConnectorConfig) {
        this.revocationCA = cAConnectorConfig;
    }

    public Certificate revocationCA(CAConnectorConfig cAConnectorConfig) {
        this.setRevocationCA(cAConnectorConfig);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Certificate{" +
            "id=" + getId() +
            ", tbsDigest='" + getTbsDigest() + "'" +
            ", subject='" + getSubject() + "'" +
            ", sans='" + getSans() + "'" +
            ", issuer='" + getIssuer() + "'" +
            ", root='" + getRoot() + "'" +
            ", type='" + getType() + "'" +
            ", description='" + getDescription() + "'" +
            ", fingerprint='" + getFingerprint() + "'" +
            ", serial='" + getSerial() + "'" +
            ", validFrom='" + getValidFrom() + "'" +
            ", validTo='" + getValidTo() + "'" +
            ", keyAlgorithm='" + getKeyAlgorithm() + "'" +
            ", keyLength=" + getKeyLength() +
            ", curveName='" + getCurveName() + "'" +
            ", hashingAlgorithm='" + getHashingAlgorithm() + "'" +
            ", paddingAlgorithm='" + getPaddingAlgorithm() + "'" +
            ", signingAlgorithm='" + getSigningAlgorithm() + "'" +
            ", creationExecutionId='" + getCreationExecutionId() + "'" +
            ", contentAddedAt='" + getContentAddedAt() + "'" +
            ", revokedSince='" + getRevokedSince() + "'" +
            ", revocationReason='" + getRevocationReason() + "'" +
            ", revoked='" + getRevoked() + "'" +
            ", revocationExecutionId='" + getRevocationExecutionId() + "'" +
            ", administrationComment='" + getAdministrationComment() + "'" +
            ", endEntity='" + getEndEntity() + "'" +
            ", selfsigned='" + getSelfsigned() + "'" +
            ", trusted='" + getTrusted() + "'" +
            ", active='" + getActive() + "'" +
            ", content='" + getContent() + "'" +
            "}";
    }
}
