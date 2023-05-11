package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A RequestProxyConfig.
 */
@Entity
@Table(name = "request_proxy_config")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RequestProxyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "request_proxy_url", nullable = false)
    private String requestProxyUrl;

    @Column(name = "active")
    private Boolean active;

    @ManyToMany(mappedBy = "requestProxies")
    @JsonIgnoreProperties(
        value = { "pipelineAttributes", "caConnector", "processInfo", "algorithms", "requestProxies" },
        allowSetters = true
    )
    private Set<Pipeline> pipelines = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RequestProxyConfig id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public RequestProxyConfig name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestProxyUrl() {
        return this.requestProxyUrl;
    }

    public RequestProxyConfig requestProxyUrl(String requestProxyUrl) {
        this.setRequestProxyUrl(requestProxyUrl);
        return this;
    }

    public void setRequestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
    }

    public Boolean isActive() {
        return this.active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public RequestProxyConfig active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<Pipeline> getPipelines() {
        return this.pipelines;
    }

    public void setPipelines(Set<Pipeline> pipelines) {
        if (this.pipelines != null) {
            this.pipelines.forEach(i -> i.removeRequestProxy(this));
        }
        if (pipelines != null) {
            pipelines.forEach(i -> i.addRequestProxy(this));
        }
        this.pipelines = pipelines;
    }

    public RequestProxyConfig pipelines(Set<Pipeline> pipelines) {
        this.setPipelines(pipelines);
        return this;
    }

    public RequestProxyConfig addPipelines(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        pipeline.getRequestProxies().add(this);
        return this;
    }

    public RequestProxyConfig removePipelines(Pipeline pipeline) {
        this.pipelines.remove(pipeline);
        pipeline.getRequestProxies().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequestProxyConfig)) {
            return false;
        }
        return id != null && id.equals(((RequestProxyConfig) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RequestProxyConfig{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", requestProxyUrl='" + getRequestProxyUrl() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
