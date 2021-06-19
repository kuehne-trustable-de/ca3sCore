package de.trustable.ca3s.core.domain;


import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;

/**
 * A CAConnectorConfig.
 */
@Entity
@Table(name = "ca_connector_config")
@NamedQueries({
	@NamedQuery(name = "CAConnectorConfig.findAllCertGenerators",
	query = "SELECT ccc FROM CAConnectorConfig ccc WHERE " +
			"ccc.caConnectorType in ( 'ADCS', 'CMP', 'INTERNAL' )"
    ),
	@NamedQuery(name = "CAConnectorConfig.findbyName",
	query = "SELECT ccc FROM CAConnectorConfig ccc WHERE " +
			"ccc.name = :name"
    )
})
public class CAConnectorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "ca_connector_type", nullable = false)
    private CAConnectorType caConnectorType;

    @Column(name = "ca_url")
    private String caUrl;

    @Column(name = "polling_offset")
    private Integer pollingOffset;

    @Column(name = "default_ca")
    private Boolean defaultCA;

    @Column(name = "trust_selfsigned_certificates")
    private Boolean trustSelfsignedCertificates;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "selector")
    private String selector;

    @Enumerated(EnumType.STRING)
    @Column(name = "jhi_interval")
    private Interval interval;

    @Column(name = "plain_secret")
    private String plainSecret;

    @OneToOne
    @JsonIgnore
    @JoinColumn(unique = true)
    private ProtectedContent secret;

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

    public CAConnectorConfig name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CAConnectorType getCaConnectorType() {
        return caConnectorType;
    }

    public CAConnectorConfig caConnectorType(CAConnectorType caConnectorType) {
        this.caConnectorType = caConnectorType;
        return this;
    }

    public void setCaConnectorType(CAConnectorType caConnectorType) {
        this.caConnectorType = caConnectorType;
    }

    public String getCaUrl() {
        return caUrl;
    }

    public CAConnectorConfig caUrl(String caUrl) {
        this.caUrl = caUrl;
        return this;
    }

    public void setCaUrl(String caUrl) {
        this.caUrl = caUrl;
    }

    public Integer getPollingOffset() {
        return pollingOffset;
    }

    public CAConnectorConfig pollingOffset(Integer pollingOffset) {
        this.pollingOffset = pollingOffset;
        return this;
    }

    public void setPollingOffset(Integer pollingOffset) {
        this.pollingOffset = pollingOffset;
    }

    public Boolean isDefaultCA() {
        return defaultCA;
    }

    public CAConnectorConfig defaultCA(Boolean defaultCA) {
        this.defaultCA = defaultCA;
        return this;
    }

    public void setDefaultCA(Boolean defaultCA) {
        this.defaultCA = defaultCA;
    }

    public Boolean isActive() {
        return active;
    }

    public CAConnectorConfig active(Boolean active) {
        this.active = active;
        return this;
    }

    public Boolean getTrustSelfsignedCertificates() {
        return trustSelfsignedCertificates;
    }

    public void setTrustSelfsignedCertificates(Boolean trustSelfsignedCertificates) {
        this.trustSelfsignedCertificates = trustSelfsignedCertificates;
    }

    public CAConnectorConfig trustSelfsignedCertificates(Boolean trustSelfsignedCertificates) {
        this.trustSelfsignedCertificates = trustSelfsignedCertificates;
        return this;
    }


    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getSelector() {
        return selector;
    }

    public CAConnectorConfig selector(String selector) {
        this.selector = selector;
        return this;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Interval getInterval() {
        return interval;
    }

    public CAConnectorConfig interval(Interval interval) {
        this.interval = interval;
        return this;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public String getPlainSecret() {
        return plainSecret;
    }

    public CAConnectorConfig plainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
        return this;
    }

    public void setPlainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
    }

    public ProtectedContent getSecret() {
        return secret;
    }

    public CAConnectorConfig secret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
        return this;
    }

    public void setSecret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CAConnectorConfig)) {
            return false;
        }
        return id != null && id.equals(((CAConnectorConfig) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "CAConnectorConfig{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", caConnectorType='" + getCaConnectorType() + "'" +
            ", caUrl='" + getCaUrl() + "'" +
            ", pollingOffset=" + getPollingOffset() +
            ", defaultCA='" + isDefaultCA() + "'" +
            ", active='" + isActive() + "'" +
            ", selector='" + getSelector() + "'" +
            ", interval='" + getInterval() + "'" +
            ", plainSecret='" + getPlainSecret() + "'" +
            "}";
    }
}
