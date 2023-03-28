package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CertificateComment.
 */
@Entity
@Table(name = "certificate_comment")
public class CertificateComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "comment", nullable = false)
    private String comment;

    @JsonIgnoreProperties(
        value = { "csr", "comment", "certificateAttributes", "issuingCertificate", "rootCertificate", "revocationCA" },
        allowSetters = true
    )
    @OneToOne(mappedBy = "comment")
    private Certificate certificate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CertificateComment id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return this.comment;
    }

    public CertificateComment comment(String comment) {
        this.setComment(comment);
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        if (this.certificate != null) {
            this.certificate.setComment(null);
        }
        if (certificate != null) {
            certificate.setComment(this);
        }
        this.certificate = certificate;
    }

    public CertificateComment certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificateComment)) {
            return false;
        }
        return id != null && id.equals(((CertificateComment) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CertificateComment{" +
            "id=" + getId() +
            ", comment='" + getComment() + "'" +
            "}";
    }
}
