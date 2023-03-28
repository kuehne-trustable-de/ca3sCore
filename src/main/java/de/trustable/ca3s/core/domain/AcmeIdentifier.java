package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeIdentifier.
 */
@Entity
@Table(name = "acme_identifier")
public class AcmeIdentifier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "acme_identifier_id", nullable = false)
    private Long acmeIdentifierId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value_", nullable = false)
    private String value;

    @ManyToOne
    @JsonIgnoreProperties("acmeIdentifiers")
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeIdentifier id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAcmeIdentifierId() {
        return this.acmeIdentifierId;
    }

    public AcmeIdentifier acmeIdentifierId(Long acmeIdentifierId) {
        this.setAcmeIdentifierId(acmeIdentifierId);
        return this;
    }

    public void setAcmeIdentifierId(Long acmeIdentifierId) {
        this.acmeIdentifierId = acmeIdentifierId;
    }

    public String getType() {
        return this.type;
    }

    public AcmeIdentifier type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public AcmeIdentifier value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AcmeOrder getOrder() {
        return this.order;
    }

    public void setOrder(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
    }

    public AcmeIdentifier order(AcmeOrder acmeOrder) {
        this.setOrder(acmeOrder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeIdentifier)) {
            return false;
        }
        return id != null && id.equals(((AcmeIdentifier) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeIdentifier{" +
            "id=" + getId() +
            ", acmeIdentifierId=" + getAcmeIdentifierId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
