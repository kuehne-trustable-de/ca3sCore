package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A RequestAttributeValue.
 */
@Entity
@Table(name = "request_attribute_value")
public class RequestAttributeValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @ManyToOne
    @JsonIgnoreProperties(value = "requestAttributeValues", allowSetters = true)
    private RequestAttribute reqAttr;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public RequestAttributeValue attributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
        return this;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public RequestAttribute getReqAttr() {
        return reqAttr;
    }

    public RequestAttributeValue reqAttr(RequestAttribute requestAttribute) {
        this.reqAttr = requestAttribute;
        return this;
    }

    public void setReqAttr(RequestAttribute requestAttribute) {
        this.reqAttr = requestAttribute;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequestAttributeValue)) {
            return false;
        }
        return id != null && id.equals(((RequestAttributeValue) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RequestAttributeValue{" +
            "id=" + getId() +
            ", attributeValue='" + getAttributeValue() + "'" +
            "}";
    }
}
