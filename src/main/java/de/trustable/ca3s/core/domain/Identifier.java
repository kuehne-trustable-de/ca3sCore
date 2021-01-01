package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A Identifier.
 */
@Entity
@Table(name = "identifier")
public class Identifier implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "identifier_id", nullable = false)
    private Long identifierId;

    @NotNull
    @Column(name = "type", nullable = false)
    private String type;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne
    @JsonIgnoreProperties("identifiers")
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdentifierId() {
        return identifierId;
    }

    public Identifier identifierId(Long identifierId) {
        this.identifierId = identifierId;
        return this;
    }

    public void setIdentifierId(Long identifierId) {
        this.identifierId = identifierId;
    }

    public String getType() {
        return type;
    }

    public Identifier type(String type) {
        this.type = type;
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public Identifier value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AcmeOrder getOrder() {
        return order;
    }

    public Identifier order(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
        return this;
    }

    public void setOrder(AcmeOrder acmeOrder) {
        this.order = acmeOrder;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Identifier)) {
            return false;
        }
        return id != null && id.equals(((Identifier) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Identifier{" +
            "id=" + getId() +
            ", identifierId=" + getIdentifierId() +
            ", type='" + getType() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
