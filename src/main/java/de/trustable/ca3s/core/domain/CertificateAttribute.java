package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CertificateAttribute.
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "CertificateAttribute.findDistinctValues",
        query = "SELECT distinct value FROM CertificateAttribute att WHERE " +
            " att.name = :attName " +
            " group by value " +
            " order by value "
    ),
})
@Table(name = "certificate_attribute")
public class CertificateAttribute implements Serializable {

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
    @JsonIgnoreProperties("certificateAttributes")
    private Certificate certificate;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CertificateAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CertificateAttribute name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public CertificateAttribute value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Certificate getCertificate() {
        return this.certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public CertificateAttribute certificate(Certificate certificate) {
        this.setCertificate(certificate);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CertificateAttribute)) {
            return false;
        }
        return id != null && id.equals(((CertificateAttribute) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CertificateAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }

    public static final String ATTRIBUTE_ATTRIBUTES_VERSION = "ATTRIBUTES_VERSION";

    public static final String ATTRIBUTE_ACME_ACCOUNT_ID = "ACME:ACCOUNT_ID";
	public static final String ATTRIBUTE_ACME_ORDER_ID = "ACME:ORDER_ID";
	public static final String ATTRIBUTE_SCEP_RECIPIENT = "CA3S:SCEP_RECIPIENT";
    public static final String ATTRIBUTE_SCEP_TRANS_ID = "CA3S:SCEP_TRANS_ID";
    public static final String ATTRIBUTE_SCEP_LAST_AUTHORIZATION_INSTANT_SEC = "CA3S:SCEP_LAST_AUTHORIZATION_INSTANT_SEC";
	public static final String ATTRIBUTE_SELFSIGNED = "CA3S:SELFSIGNED";
	public static final String ATTRIBUTE_CA = "CA3S:CA";
	public static final String ATTRIBUTE_CA3S_ROOT = "CA3S:ROOT";
	public static final String ATTRIBUTE_CA3S_INTERMEDIATE = "CA3S:INTERMEDIATE";
	public static final String ATTRIBUTE_END_ENTITY = "CA3S:END_ENTITY";
	public static final String ATTRIBUTE_CHAIN_LENGTH = "CA3S:CHAIN_LENGTH";

    public static final String ATTRIBUTE_SUBJECT = "SUBJECT";
    public static final String ATTRIBUTE_RDN_PREFIX = "RDN_";
    public static final String ATTRIBUTE_RDN_C = "RDN_COUNTRYNAME";
    public static final String ATTRIBUTE_RDN_CN = "RDN_COMMONNAME";
    public static final String ATTRIBUTE_RDN_O = "RDN_ORGANIZATIONNAME";
    public static final String ATTRIBUTE_RDN_OU = "RDN_ORGANIZATIONALUNITNAME";
    public static final String ATTRIBUTE_RDN_S = "RDN_STATEORPROVINCENAME";
    public static final String ATTRIBUTE_RDN_L = "RDN_LOCALITYNAME";

	public static final String ATTRIBUTE_SAN = "SAN";
    public static final String ATTRIBUTE_ALT_ALGO = "ALT_ALGO";
	public static final String ATTRIBUTE_SUBJECT_RDN_PART = "SUBJECT_RDN_PART";
	public static final String ATTRIBUTE_ISSUER = "ISSUER";
	public static final String ATTRIBUTE_ROOT = "ROOT";
	public static final String ATTRIBUTE_TYPE = "TYPE";
	public static final String ATTRIBUTE_USAGE = "USAGE";
	public static final String ATTRIBUTE_EXTENDED_USAGE = "EXTENDED_USAGE";
	public static final String ATTRIBUTE_EXTENDED_USAGE_OID = "EXTENDED_USAGE_OID";

	public static final String ATTRIBUTE_SKI = "SKI";
	public static final String ATTRIBUTE_AKI = "AKI";
	public static final String ATTRIBUTE_SERIAL = "SERIAL";
	public static final String ATTRIBUTE_SERIAL_PADDED = "SERIAL_PADDED";
	public static final String ATTRIBUTE_VALID_FROM = "VALID_FROM";
	public static final String ATTRIBUTE_VALID_FROM_TIMESTAMP = "VALID_FROM_TIMESTAMP";
	public static final String ATTRIBUTE_VALID_TO = "VALID_TO";
	public static final String ATTRIBUTE_VALID_TO_TIMESTAMP = "VALID_TO_TIMESTAMP";
	public static final String ATTRIBUTE_VALIDITY_PERIOD = "VALIDITY_PERIOD";

    public static final String ATTRIBUTE_FINGERPRINT_SHA1 = "FINGERPRINT_SHA1";
    public static final String ATTRIBUTE_FINGERPRINT_SHA256 = "FINGERPRINT_SHA256";

    public static final String ATTRIBUTE_SUBJECT_RFC_2253 = "SUBJECT_RFC_2253";

    public static final String ATTRIBUTE_CA_CONNECTOR_ID = "CA_CONNECTOR_ID";
	public static final String ATTRIBUTE_CA_RESOLVED_TIMESTAMP = "CA_RESOLVED_TIMESTAMP";
	public static final String ATTRIBUTE_CA_REVOKED_TIMESTAMP = "CA_REVOKED_TIMESTAMP";

	public static final String ATTRIBUTE_CA_PROCESSING_ID = CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID;
	public static final String ATTRIBUTE_PROCESSING_CA = CsrAttribute.ATTRIBUTE_PROCESSING_CA;

    public static final String ATTRIBUTE_REQUESTED_BY = "REQUESTED_BY";
    public static final String ATTRIBUTE_REPLACED_BY = "REPLACED_BY";

    public static final String ATTRIBUTE_REPLACES_NUMBER_OF_CERTS = "REPLACES_NUMBER_OF_CERTS";

    public static final String ATTRIBUTE_SOURCE = "SOURCE";

	public static final String ATTRIBUTE_UPLOADED_BY = "UPLOADED_BY";

	public static final String ATTRIBUTE_REVOKED_BY = "REVOKED_BY";

	public static final String ATTRIBUTE_CRL_URL = "CRL_URL";

	public static final String ATTRIBUTE_CRL_NEXT_UPDATE = "CRL_NEXT_UPDATE";

	public static final String ATTRIBUTE_OCSP_URL = "OCSP_URL";

	public static final String ATTRIBUTE_POLICY_ID = "POLICY_ID";

    public static final String ATTRIBUTE_COMMENT = "COMMENT";

    public static final String ATTRIBUTE_SCEP_TRUSTED_ISSUER = "SCEP_TRUSTED_ISSUER";

    public static final String ATTRIBUTE_TLS_CERTIFICATE = "TLS_CERTIFICATE";
    public static final String ATTRIBUTE_TLS_KEY = "TLS_KEY";

    public static final String ATTRIBUTE_USER_CLIENT_CERT = "USER_CLIENT_CERT";

    public static final String ATTRIBUTE_CERTIFICATE_NOTIFICATION = "CERTIFICATE_NOTIFICATION_PROCESS";
    public static final String ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER = "CERTIFICATE_NOTIFICATION_COUNTER";

}
