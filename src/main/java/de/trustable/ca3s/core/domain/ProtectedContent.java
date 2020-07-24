package de.trustable.ca3s.core.domain;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;

import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;

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
    )
})
public class ProtectedContent implements Serializable {

	
	
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Lob
    @Column(name = "content_base_64", nullable = false)
    private String contentBase64;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProtectedContentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type")
    private ContentRelationType relationType;

    @Column(name = "related_id")
    private Long relatedId;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentBase64() {
        return contentBase64;
    }

    public ProtectedContent contentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
        return this;
    }

    public void setContentBase64(String contentBase64) {
        this.contentBase64 = contentBase64;
    }

    public ProtectedContentType getType() {
        return type;
    }

    public ProtectedContent type(ProtectedContentType type) {
        this.type = type;
        return this;
    }

    public void setType(ProtectedContentType type) {
        this.type = type;
    }

    public ContentRelationType getRelationType() {
        return relationType;
    }

    public ProtectedContent relationType(ContentRelationType relationType) {
        this.relationType = relationType;
        return this;
    }

    public void setRelationType(ContentRelationType relationType) {
        this.relationType = relationType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public ProtectedContent relatedId(Long relatedId) {
        this.relatedId = relatedId;
        return this;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        return 31;
    }

    @Override
    public String toString() {
        return "ProtectedContent{" +
            "id=" + getId() +
            ", contentBase64='" + getContentBase64() + "'" +
            ", type='" + getType() + "'" +
            ", relationType='" + getRelationType() + "'" +
            ", relatedId=" + getRelatedId() +
            "}";
    }
}
