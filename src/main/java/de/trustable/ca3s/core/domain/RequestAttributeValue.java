package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A RequestAttributeValue.
 */
@Entity
@Table(name = "request_attribute_value")
public class RequestAttributeValue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "attribute_value", nullable = false)
    private String attributeValue;

    @ManyToOne
    @JsonIgnoreProperties("requestAttributeValues")
    private RequestAttribute reqAttr;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RequestAttributeValue id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAttributeValue() {
        return this.attributeValue;
    }

    public RequestAttributeValue attributeValue(String attributeValue) {
        this.setAttributeValue(attributeValue);
        return this;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }

    public RequestAttribute getReqAttr() {
        return this.reqAttr;
    }

    public void setReqAttr(RequestAttribute requestAttribute) {
        this.reqAttr = requestAttribute;
    }

    public RequestAttributeValue reqAttr(RequestAttribute requestAttribute) {
        this.setReqAttr(requestAttribute);
        return this;
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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
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
