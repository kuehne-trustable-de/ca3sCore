package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A RequestAttribute.
 */
@Entity
@Table(name = "request_attribute")
public class RequestAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
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

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RequestAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeType() {
        return this.attributeType;
    }

    public RequestAttribute attributeType(String attributeType) {
        this.setAttributeType(attributeType);
        return this;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public Set<RequestAttributeValue> getRequestAttributeValues() {
        return this.requestAttributeValues;
    }

    public void setRequestAttributeValues(Set<RequestAttributeValue> requestAttributeValues) {
        if (this.requestAttributeValues != null) {
            this.requestAttributeValues.forEach(i -> i.setReqAttr(null));
        }
        if (requestAttributeValues != null) {
            requestAttributeValues.forEach(i -> i.setReqAttr(this));
        }
        this.requestAttributeValues = requestAttributeValues;
    }

    public RequestAttribute requestAttributeValues(Set<RequestAttributeValue> requestAttributeValues) {
        this.setRequestAttributeValues(requestAttributeValues);
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

    public RequestAttributeValue getHoldingRequestAttribute() {
        return this.holdingRequestAttribute;
    }

    public void setHoldingRequestAttribute(RequestAttributeValue requestAttributeValue) {
        this.holdingRequestAttribute = requestAttributeValue;
    }

    public RequestAttribute holdingRequestAttribute(RequestAttributeValue requestAttributeValue) {
        this.setHoldingRequestAttribute(requestAttributeValue);
        return this;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public RequestAttribute csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RequestAttribute{" +
            "id=" + getId() +
            ", attributeType='" + getAttributeType() + "'" +
            "}";
    }
}
