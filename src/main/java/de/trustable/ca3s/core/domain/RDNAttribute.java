package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A RDNAttribute.
 */
@Entity
@Table(name = "rdn_attribute")
public class RDNAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "attribute_type", nullable = false)
    private String attributeType;

    @NotNull
    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @ManyToOne
    @JsonIgnoreProperties(value = "rdnAttributes", allowSetters = true)
    private RDN rdn;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public RDNAttribute attributeType(String attributeType) {
        this.attributeType = attributeType;
        return this;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public RDNAttribute attributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
        return this;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public RDN getRdn() {
        return rdn;
    }

    public RDNAttribute rdn(RDN rDN) {
        this.rdn = rDN;
        return this;
    }

    public void setRdn(RDN rDN) {
        this.rdn = rDN;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RDNAttribute)) {
            return false;
        }
        return id != null && id.equals(((RDNAttribute) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RDNAttribute{" +
            "id=" + getId() +
            ", attributeType='" + getAttributeType() + "'" +
            ", attributeValue='" + getAttributeValue() + "'" +
            "}";
    }
}
