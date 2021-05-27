package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CSRComment.
 */
@Entity
@Table(name = "csr_comment")
public class CSRComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(name = "comment", nullable = false)
    private String comment;

    @JsonIgnoreProperties(value = { "comment", "rdns", "ras", "csrAttributes", "pipeline", "certificate" }, allowSetters = true)
    @OneToOne(mappedBy = "comment")
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CSRComment id(Long id) {
        this.id = id;
        return this;
    }

    public String getComment() {
        return this.comment;
    }

    public CSRComment comment(String comment) {
        this.comment = comment;
        return this;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public CSRComment csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    public void setCsr(CSR cSR) {
        if (this.csr != null) {
            this.csr.setComment(null);
        }
        if (csr != null) {
            csr.setComment(this);
        }
        this.csr = cSR;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CSRComment)) {
            return false;
        }
        return id != null && id.equals(((CSRComment) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CSRComment{" +
            "id=" + getId() +
            ", comment='" + getComment() + "'" +
            "}";
    }
}
