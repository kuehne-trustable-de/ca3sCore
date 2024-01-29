package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Tenant.
 */
@Entity
@Table(name = "tenant")
@NamedQueries({
    @NamedQuery(name = "Tenant.findByName",
        query = "SELECT t FROM Tenant t WHERE " +
            "t.name = :name"
    )
})
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "longname", nullable = false)
    private String longname;

    @Column(name = "active")
    private Boolean active;

    @ManyToMany(mappedBy = "tenants")
    @JsonIgnoreProperties(
        value = {
            "pipelineAttributes",
            "caConnector",
            "processInfoCreate",
            "processInfoRevoke",
            "processInfoNotify",
            "algorithms",
            "requestProxies",
            "tenants",
        },
        allowSetters = true
    )
    private Set<Pipeline> pipelines = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Tenant id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Tenant name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLongname() {
        return this.longname;
    }

    public Tenant longname(String longname) {
        this.setLongname(longname);
        return this;
    }

    public void setLongname(String longname) {
        this.longname = longname;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Tenant active(Boolean active) {
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
            this.pipelines.forEach(i -> i.removeTenants(this));
        }
        if (pipelines != null) {
            pipelines.forEach(i -> i.addTenants(this));
        }
        this.pipelines = pipelines;
    }

    public Tenant pipelines(Set<Pipeline> pipelines) {
        this.setPipelines(pipelines);
        return this;
    }

    public Tenant addPipelines(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        pipeline.getTenants().add(this);
        return this;
    }

    public Tenant removePipelines(Pipeline pipeline) {
        this.pipelines.remove(pipeline);
        pipeline.getTenants().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tenant)) {
            return false;
        }
        return id != null && id.equals(((Tenant) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Tenant{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", longname='" + getLongname() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
