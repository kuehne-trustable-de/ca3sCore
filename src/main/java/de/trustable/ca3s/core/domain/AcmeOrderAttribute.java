package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A AcmeOrderAttribute.
 */
@Entity
@Table(name = "acme_order_attribute")
public class AcmeOrderAttribute implements Serializable {

    public static final String AUTHORIZATION = "AUTHORIZATION";
    public static final String CHALLENGE_TYPE = "CHALLENGE_TYPE";
    public static final String WILDCARD_REQUEST = "WILDCARD_REQUEST";

    public static final String REQUEST_PROXY_ID_USED = "REQUEST_PROXY_ID_USED";

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value_")
    private String value;

    @ManyToOne
    @JsonIgnoreProperties(
        value = { "acmeAuthorizations", "attributes", "acmeIdentifiers", "csr", "certificate", "pipeline", "account" },
        allowSetters = true
    )
    private AcmeOrder order;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AcmeOrderAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public AcmeOrderAttribute name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value;
    }

    public AcmeOrderAttribute value(String value) {
        this.setValue(value);
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

    public AcmeOrderAttribute order(AcmeOrder acmeOrder) {
        this.setOrder(acmeOrder);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AcmeOrderAttribute)) {
            return false;
        }
        return id != null && id.equals(((AcmeOrderAttribute) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AcmeOrderAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
