package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

/**
 * A RequestAttribute.
 */
@Entity
@Table(name = "request_attribute")
public class RequestAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "attribute_type", nullable = false)
    private String attributeType;

    @OneToMany(mappedBy = "reqAttr")
    private Set<RequestAttributeValue> requestAttributeValues = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("requestAttributes")
    private RequestAttributeValue holdingRequestAttribute;

    @ManyToOne
    @JsonIgnoreProperties("ras")
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public RequestAttribute attributeType(String attributeType) {
        this.attributeType = attributeType;
        return this;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public Set<RequestAttributeValue> getRequestAttributeValues() {
        return requestAttributeValues;
    }

    public RequestAttribute requestAttributeValues(Set<RequestAttributeValue> requestAttributeValues) {
        this.requestAttributeValues = requestAttributeValues;
        return this;
    }

    public RequestAttribute addRequestAttributeValues(RequestAttributeValue requestAttributeValue) {
        this.requestAttributeValues.add(requestAttributeValue);
        requestAttributeValue.setReqAttr(this);
        return this;
    }

    public RequestAttribute removeRequestAttributeValues(RequestAttributeValue requestAttributeValue) {
        this.requestAttributeValues.remove(requestAttributeValue);
        requestAttributeValue.setReqAttr(null);
        return this;
    }

    public void setRequestAttributeValues(Set<RequestAttributeValue> requestAttributeValues) {
        this.requestAttributeValues = requestAttributeValues;
    }

    public RequestAttributeValue getHoldingRequestAttribute() {
        return holdingRequestAttribute;
    }

    public RequestAttribute holdingRequestAttribute(RequestAttributeValue requestAttributeValue) {
        this.holdingRequestAttribute = requestAttributeValue;
        return this;
    }

    public void setHoldingRequestAttribute(RequestAttributeValue requestAttributeValue) {
        this.holdingRequestAttribute = requestAttributeValue;
    }

    public CSR getCsr() {
        return csr;
    }

    public RequestAttribute csr(CSR cSR) {
        this.csr = cSR;
        return this;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequestAttribute)) {
            return false;
        }
        return id != null && id.equals(((RequestAttribute) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "RequestAttribute{" +
            "id=" + getId() +
            ", attributeType='" + getAttributeType() + "'" +
            "}";
    }
}
