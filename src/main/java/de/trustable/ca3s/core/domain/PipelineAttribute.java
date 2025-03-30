package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A PipelineAttribute.
 */
@Entity
@Table(name = "pipeline_attribute")
@NamedQueries({
    @NamedQuery(name = "PipelineAttribute.findDistinctByName",
        query = "SELECT distinct pa.value FROM PipelineAttribute pa WHERE " +
            "pa.name like :name"
    ),
    @NamedQuery(name = "PipelineAttribute.findDistinctPipelineByNameAndValue",
        query = "SELECT distinct pa.pipeline.id FROM PipelineAttribute pa WHERE " +
            "pa.name like :name and " +
            "pa.value like :value"
    )
})
public class
PipelineAttribute implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "value_", nullable = false)
    private String value;

    @ManyToOne
    @JsonIgnoreProperties("pipelineAttributes")
    private Pipeline pipeline;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public PipelineAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public PipelineAttribute name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public PipelineAttribute value(String value) {
        this.value = value;
        return this;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Pipeline getPipeline() {
        return this.pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }

    public PipelineAttribute pipeline(Pipeline pipeline) {
        this.setPipeline(pipeline);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PipelineAttribute)) {
            return false;
        }
        return id != null && id.equals(((PipelineAttribute) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PipelineAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
