package de.trustable.ca3s.core.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * A CsrAttribute.
 */
@Entity
@Table(name = "csr_attribute")
public class CsrAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @ManyToOne
    @JsonIgnoreProperties("csrAttributes")
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public CsrAttribute name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public CsrAttribute value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CSR getCsr() {
        return csr;
    }

    public CsrAttribute csr(CSR cSR) {
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
        if (!(o instanceof CsrAttribute)) {
            return false;
        }
        return id != null && id.equals(((CsrAttribute) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CsrAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
    
	public static final String ATTRIBUTE_REQUSTOR_NAME = "CA3S:REQUESTOR_NAME";
	public static final String ATTRIBUTE_PROCESSING_CA = "CA3S:PROCESSING_CA";
	public static final String ATTRIBUTE_CA_PROCESSING_ID = "CA3S:CA_PROCESSING_ID";
	public static final String ATTRIBUTE_CA_PROCESSING_STARTED_TIMESTAMP = "CA3S:CA_PROCESSING_STARTED_TIMESTAMP";
	public static final String ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP = "CA3S:CA_PROCESSING_FINISHED_TIMESTAMP";
	

}
