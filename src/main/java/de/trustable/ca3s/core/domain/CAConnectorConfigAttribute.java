package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A CAConnectorConfigAttribute.
 */
@Entity
@Table(name = "ca_connector_config_attribute")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CAConnectorConfigAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "value_0", nullable = false)
    private String value0;

    @ManyToOne
    @JsonIgnoreProperties(value = { "secret", "caConnectorAttributes", "tlsAuthentication", "messageProtection" }, allowSetters = true)
    private CAConnectorConfig caConnector;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public CAConnectorConfigAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public CAConnectorConfigAttribute name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value0;
    }

    public CAConnectorConfigAttribute value(String value0) {
        this.setValue(value0);
        return this;
    }

    public void setValue(String value) {
        this.value0 = value;
    }

    public CAConnectorConfig getCaConnector() {
        return this.caConnector;
    }

    public void setCaConnector(CAConnectorConfig cAConnectorConfig) {
        this.caConnector = cAConnectorConfig;
    }

    public CAConnectorConfigAttribute caConnector(CAConnectorConfig cAConnectorConfig) {
        this.setCaConnector(cAConnectorConfig);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CAConnectorConfigAttribute)) {
            return false;
        }
        return id != null && id.equals(((CAConnectorConfigAttribute) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CAConnectorConfigAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
