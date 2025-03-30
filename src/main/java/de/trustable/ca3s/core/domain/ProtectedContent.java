package de.trustable.ca3s.core.domain;

import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.Cache;

/**
 * A ProtectedContent.
 */
@Entity
@Table(name = "protected_content")
@NamedQueries({
	@NamedQuery(name = "ProtectedContent.findByCertificateId",
	query = "SELECT pc FROM ProtectedContent pc WHERE " +
			"pc.relationType = 'CERTIFICATE' and " +
			"pc.relatedId    = :certId"
    ),
	@NamedQuery(name = "ProtectedContent.findByCSRId",
	query = "SELECT pc FROM ProtectedContent pc WHERE " +
			"pc.relationType = 'CSR' and " +
			"pc.relatedId    = :csrId"
    ),
    @NamedQuery(name = "ProtectedContent.findByTypeRelationId",
        query = "SELECT pc FROM ProtectedContent pc WHERE " +
            "pc.type = :type and " +
            "pc.relationType = :relationType and " +
            "pc.relatedId    = :id"
    ),
    @NamedQuery(name = "ProtectedContent.findByRelationTypesRelationId",
        query = "SELECT pc FROM ProtectedContent pc WHERE " +
            "pc.relationType in :relationTypes and " +
            "pc.relatedId    = :id"
    ),
    @NamedQuery(name = "ProtectedContent.findByTypeRelationContentB64",
        query = "SELECT pc FROM ProtectedContent pc WHERE " +
            "pc.type = :type and " +
            "pc.relationType  = :relationType and " +
            "pc.contentBase64 = :contentB64"
    ),
    @NamedQuery(name = "ProtectedContent.findByValidToPassed",
        query = "SELECT pc FROM ProtectedContent pc WHERE " +
            "pc.validTo    < :validTo"
    ),
    @NamedQuery(name = "ProtectedContent.findByDeleteAfterPassed",
        query = "SELECT pc FROM ProtectedContent pc WHERE " +
            "pc.deleteAfter    < :deleteAfter"
    )

})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ProtectedContent implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final ContentRelationType[] USER_CONTENT_RELATION_TYPES = new ContentRelationType[]{
        ContentRelationType.OTP_SECRET,
        ContentRelationType.SMS_PHONE,
        ContentRelationType.ACCOUNT_TOKEN};

    public static final List<ContentRelationType> USER_CONTENT_RELATION_TYPE_LIST = Arrays.asList(USER_CONTENT_RELATION_TYPES);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "content_base_64", nullable = false)
    private String contentBase64;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProtectedContentType type;

    @Column(name = "left_usages")
    private Integer leftUsages;

    @Column(name = "created_on")
    private Instant createdOn;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "delete_after")
    private Instant deleteAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private ContentRelationType relationType;

    @Column(name = "related_id")
    private Long relatedId;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ProtectedContent id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentBase64() {
        return this.contentBase64;
    }

    public ProtectedContent contentBase64(String contentBase64) {
        this.setContentBase64(contentBase64);
        return this;
    }

    public void setContentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
    }

    public ProtectedContentType getType() {
        return this.type;
    }

    public ProtectedContent type(ProtectedContentType type) {
        this.setType(type);
        return this;
    }

    public void setType(ProtectedContentType type) {
        this.type = type;
    }

    public Integer getLeftUsages() {
        return this.leftUsages;
    }

    public ProtectedContent leftUsages(Integer leftUsages) {
        this.setLeftUsages(leftUsages);
        return this;
    }

    public void setLeftUsages(Integer leftUsages) {
        this.leftUsages = leftUsages;
    }

    public Instant getCreatedOn() {
        return this.createdOn;
    }

    public ProtectedContent createdOn(Instant createdOn) {
        this.setCreatedOn(createdOn);
        return this;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Instant getValidTo() {
        return this.validTo;
    }

    public ProtectedContent validTo(Instant validTo) {
        this.setValidTo(validTo);
        return this;
    }

    public void setValidTo(Instant validTo) {
        this.validTo = validTo;
    }

    public Instant getDeleteAfter() {
        return this.deleteAfter;
    }

    public ProtectedContent deleteAfter(Instant deleteAfter) {
        this.setDeleteAfter(deleteAfter);
        return this;
    }

    public void setDeleteAfter(Instant deleteAfter) {
        this.deleteAfter = deleteAfter;
    }

    public ContentRelationType getRelationType() {
        return this.relationType;
    }

    public ProtectedContent relationType(ContentRelationType relationType) {
        this.setRelationType(relationType);
        return this;
    }

    public void setRelationType(ContentRelationType relationType) {
        this.relationType = relationType;
    }

    public Long getRelatedId() {
        return this.relatedId;
    }

    public ProtectedContent relatedId(Long relatedId) {
        this.setRelatedId(relatedId);
        return this;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProtectedContent)) {
            return false;
        }
        return id != null && id.equals(((ProtectedContent) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ProtectedContent{" +
            "id=" + getId() +
            ", contentBase64='" + getContentBase64() + "'" +
            ", type='" + getType() + "'" +
            ", leftUsages=" + getLeftUsages() +
            ", createdOn='" + getCreatedOn() + "'" +
            ", validTo='" + getValidTo() + "'" +
            ", deleteAfter='" + getDeleteAfter() + "'" +
            ", relationType='" + getRelationType() + "'" +
            ", relatedId=" + getRelatedId() +
            "}";
    }
}
