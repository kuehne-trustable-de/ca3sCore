package de.trustable.ca3s.core.domain;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import de.trustable.ca3s.core.domain.enumeration.PipelineType;

/**
 * A Pipeline.
 */
@Entity
@Table(name = "pipeline")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Pipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PipelineType type;

    @NotNull
    @Column(name = "url_part", nullable = false)
    private String urlPart;

    @OneToMany(mappedBy = "pipeline")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<PipelineAttribute> pipelineAttributes = new HashSet<>();

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

    public Pipeline name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PipelineType getType() {
        return type;
    }

    public Pipeline type(PipelineType type) {
        this.type = type;
        return this;
    }

    public void setType(PipelineType type) {
        this.type = type;
    }

    public String getUrlPart() {
        return urlPart;
    }

    public Pipeline urlPart(String urlPart) {
        this.urlPart = urlPart;
        return this;
    }

    public void setUrlPart(String urlPart) {
        this.urlPart = urlPart;
    }

    public Set<PipelineAttribute> getPipelineAttributes() {
        return pipelineAttributes;
    }

    public Pipeline pipelineAttributes(Set<PipelineAttribute> pipelineAttributes) {
        this.pipelineAttributes = pipelineAttributes;
        return this;
    }

    public Pipeline addPipelineAttributes(PipelineAttribute pipelineAttribute) {
        this.pipelineAttributes.add(pipelineAttribute);
        pipelineAttribute.setPipeline(this);
        return this;
    }

    public Pipeline removePipelineAttributes(PipelineAttribute pipelineAttribute) {
        this.pipelineAttributes.remove(pipelineAttribute);
        pipelineAttribute.setPipeline(null);
        return this;
    }

    public void setPipelineAttributes(Set<PipelineAttribute> pipelineAttributes) {
        this.pipelineAttributes = pipelineAttributes;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Pipeline)) {
            return false;
        }
        return id != null && id.equals(((Pipeline) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Pipeline{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", urlPart='" + getUrlPart() + "'" +
            "}";
    }
}
