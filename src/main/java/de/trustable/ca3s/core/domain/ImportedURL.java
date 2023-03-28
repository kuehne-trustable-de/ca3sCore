package de.trustable.ca3s.core.domain;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A ImportedURL.
 */
@Entity
@Table(name = "imported_url")
@NamedQueries({
	@NamedQuery(name = "ImportedURL.findByURL",
	query = "SELECT impURL FROM ImportedURL impURL WHERE " +
			"impURL.name = :url"
    )
})
public class ImportedURL implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "import_date", nullable = false)
    private Instant importDate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ImportedURL id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public ImportedURL name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getImportDate() {
        return this.importDate;
    }

    public ImportedURL importDate(Instant importDate) {
        this.setImportDate(importDate);
        return this;
    }

    public void setImportDate(Instant importDate) {
        this.importDate = importDate;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImportedURL)) {
            return false;
        }
        return id != null && id.equals(((ImportedURL) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ImportedURL{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", importDate='" + getImportDate() + "'" +
            "}";
    }
}
