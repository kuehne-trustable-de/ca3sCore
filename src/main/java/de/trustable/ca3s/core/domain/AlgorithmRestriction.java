package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.AlgorithmType;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;

/**
 * A AlgorithmRestriction.
 */
@Entity
@Table(name = "algorithm_restriction")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlgorithmRestriction implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AlgorithmType type;

    @Column(name = "not_after")
    private Instant notAfter;

    @Column(name = "identifier")
    private String identifier;

    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "acceptable", nullable = false)
    private Boolean acceptable;

    @ManyToMany(mappedBy = "algorithms")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "pipelineAttributes", "caConnector", "processInfo", "algorithms" }, allowSetters = true)
    private Set<Pipeline> pipelines = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public AlgorithmRestriction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AlgorithmType getType() {
        return this.type;
    }

    public AlgorithmRestriction type(AlgorithmType type) {
        this.setType(type);
        return this;
    }

    public void setType(AlgorithmType type) {
        this.type = type;
    }

    public Instant getNotAfter() {
        return this.notAfter;
    }

    public AlgorithmRestriction notAfter(Instant notAfter) {
        this.setNotAfter(notAfter);
        return this;
    }

    public void setNotAfter(Instant notAfter) {
        this.notAfter = notAfter;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public AlgorithmRestriction identifier(String identifier) {
        this.setIdentifier(identifier);
        return this;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return this.name;
    }

    public AlgorithmRestriction name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getAcceptable() {
        return this.acceptable;
    }

    public AlgorithmRestriction acceptable(Boolean acceptable) {
        this.setAcceptable(acceptable);
        return this;
    }

    public void setAcceptable(Boolean acceptable) {
        this.acceptable = acceptable;
    }

    public Set<Pipeline> getPipelines() {
        return this.pipelines;
    }

    public void setPipelines(Set<Pipeline> pipelines) {
        if (this.pipelines != null) {
            this.pipelines.forEach(i -> i.removeAlgorithms(this));
        }
        if (pipelines != null) {
            pipelines.forEach(i -> i.addAlgorithms(this));
        }
        this.pipelines = pipelines;
    }

    public AlgorithmRestriction pipelines(Set<Pipeline> pipelines) {
        this.setPipelines(pipelines);
        return this;
    }

    public AlgorithmRestriction addPipelines(Pipeline pipeline) {
        this.pipelines.add(pipeline);
        pipeline.getAlgorithms().add(this);
        return this;
    }

    public AlgorithmRestriction removePipelines(Pipeline pipeline) {
        this.pipelines.remove(pipeline);
        pipeline.getAlgorithms().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlgorithmRestriction)) {
            return false;
        }
        return id != null && id.equals(((AlgorithmRestriction) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlgorithmRestriction{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", notAfter='" + getNotAfter() + "'" +
            ", identifier='" + getIdentifier() + "'" +
            ", name='" + getName() + "'" +
            ", acceptable='" + getAcceptable() + "'" +
            "}";
    }
}
