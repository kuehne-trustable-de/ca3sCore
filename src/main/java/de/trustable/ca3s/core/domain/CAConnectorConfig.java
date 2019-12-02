package de.trustable.ca3s.core.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;

import javax.persistence.*;

import java.io.Serializable;

/**
 * A CAConnectorConfig.
 */
@Entity
@Table(name = "ca_connector_config")
@NamedQueries({
	@NamedQuery(name = "CAConnectorConfig.findDefaultCA",
	query = "SELECT a FROM CAConnectorConfig a WHERE " +
			"a.defaultCA = TRUE"
    ),
})
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CAConnectorConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "ca_connector_type")
    private CAConnectorType caConnectorType;

    @Column(name = "ca_url")
    private String caUrl;

    @Column(name = "secret")
    private String secret;

    @Column(name = "polling_offset")
    private Integer pollingOffset;

    @Column(name = "default_ca")
    private Boolean defaultCA;

    @Column(name = "active")
    private Boolean active;

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

    public String getSecret() {
        return secret;
    }

    public CAConnectorConfig secret(String secret) {
        this.secret = secret;
        return this;
    }

    public void setSecret(String secret) {
        this.secret = secret;
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

    public void setActive(Boolean active) {
        this.active = active;
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
            ", secret='" + getSecret() + "'" +
            ", pollingOffset=" + getPollingOffset() +
            ", defaultCA='" + isDefaultCA() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }
}
