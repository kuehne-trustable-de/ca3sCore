package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CsrAttribute.
 */
@Entity
@Table(name = "csr_attribute")
public class CsrAttribute implements Serializable {

    public static final String ATTRIBUTE_HASH_ALGO = "HASH_ALGO";
    public static final String ATTRIBUTE_SIGN_ALGO = "SIGN_ALGO";
    public static final String ATTRIBUTE_PADDING_ALGO = "PADDING_ALGO";
    public static final String ATTRIBUTE_MFG = "MFG";

    public static final int CURRENT_ATTRIBUTES_VERSION = 2;

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
    @JsonIgnoreProperties("csrAttributes")
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CsrAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CsrAttribute name(String name) {
        this.setName(name);
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
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public CsrAttribute csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CsrAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }


    public static final String ATTRIBUTE_REQUESTED_BY = "REQUESTOR_NAME";
	public static final String ATTRIBUTE_PROCESSING_CA = "PROCESSING_CA";
	public static final String ATTRIBUTE_CA_PROCESSING_ID = "CA_PROCESSING_ID";
	public static final String ATTRIBUTE_CA_PROCESSING_STARTED_TIMESTAMP = "CA_PROCESSING_STARTED_TIMESTAMP";
	public static final String ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP = "CA_PROCESSING_FINISHED_TIMESTAMP";

	public static final String ATTRIBUTE_SUBJECT = CertificateAttribute.ATTRIBUTE_SUBJECT;

	public static final String REQUESTOR_SYSTEM = "SYSTEM";

	public static final String REQUESTOR_SCEP = "REQUESTOR_SCEP";

	public static final String ATTRIBUTE_SAN = "SAN";
	public static final String ATTRIBUTE_TYPED_SAN = "TYPED_SAN";
	public static final String ATTRIBUTE_TYPED_VSAN = "TYPED_VSAN";

	public static final String ATTRIBUTE_FAILURE_INFO = "REJECTION_INFO";

    public static final String ARA_PREFIX = "_ARA_";

}
