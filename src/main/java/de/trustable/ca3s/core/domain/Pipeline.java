package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A Pipeline.
 */
@Entity
@Table(name = "pipeline")
@NamedQueries({
    @NamedQuery(name = "Pipeline.findByTypeUrl",
        query = "SELECT p FROM Pipeline p WHERE " +
            "p.type = :type and " +
            "p.urlPart = :urlPart"
    ),
    @NamedQuery(name = "Pipeline.findActiveByTypeUrl",
        query = "SELECT p FROM Pipeline p WHERE " +
            "p.active = true and " +
            "p.type = :type and " +
            "p.urlPart = :urlPart"
    ),
    @NamedQuery(name = "Pipeline.findByType",
        query = "SELECT p FROM Pipeline p WHERE " +
            "p.type = :type "
    ),
    @NamedQuery(name = "Pipeline.findActiveByType",
        query = "SELECT p FROM Pipeline p WHERE " +
            "p.active = true and " +
            "p.type = :type "
    ),
    @NamedQuery(name = "Pipeline.findByName",
        query = "SELECT p FROM Pipeline p WHERE " +
            "p.name = :name "
    )

})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Pipeline implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private PipelineType type;

    @Column(name = "url_part")
    private String urlPart;

    @Column(name = "active")
    private Boolean active;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "approval_required")
    private Boolean approvalRequired;

    @OneToMany(mappedBy = "pipeline")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "pipeline" }, allowSetters = true)
    private Set<PipelineAttribute> pipelineAttributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties({"pipelines", "secret"})
    private CAConnectorConfig caConnector;

    @ManyToOne
    @JsonIgnoreProperties({"pipelines", "secret"})
    private BPMNProcessInfo processInfo;

    @ManyToMany
    @JoinTable(
        name = "rel_pipeline__algorithms",
        joinColumns = @JoinColumn(name = "pipeline_id"),
        inverseJoinColumns = @JoinColumn(name = "algorithms_id")
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "pipelines" }, allowSetters = true)
    private Set<AlgorithmRestriction> algorithms = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Pipeline id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public Pipeline name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PipelineType getType() {
        return this.type;
    }

    public Pipeline type(PipelineType type) {
        this.setType(type);
        return this;
    }

    public void setType(PipelineType type) {
        this.type = type;
    }

    public String getUrlPart() {
        return this.urlPart;
    }

    public Pipeline urlPart(String urlPart) {
        this.setUrlPart(urlPart);
        return this;
    }

    public void setUrlPart(String urlPart) {
        this.urlPart = urlPart;
    }


    public Boolean isActive() {
        return active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Pipeline active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return this.description;
    }

    public Pipeline description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isApprovalRequired() {
        return approvalRequired;
    }

    public Boolean getApprovalRequired() {
        return this.approvalRequired;
    }

    public Pipeline approvalRequired(Boolean approvalRequired) {
        this.setApprovalRequired(approvalRequired);
        return this;
    }

    public void setApprovalRequired(Boolean approvalRequired) {
        this.approvalRequired = approvalRequired;
    }

    public Set<PipelineAttribute> getPipelineAttributes() {
        return this.pipelineAttributes;
    }

    public void setPipelineAttributes(Set<PipelineAttribute> pipelineAttributes) {
        if (this.pipelineAttributes != null) {
            this.pipelineAttributes.forEach(i -> i.setPipeline(null));
        }
        if (pipelineAttributes != null) {
            pipelineAttributes.forEach(i -> i.setPipeline(this));
        }
        this.pipelineAttributes = pipelineAttributes;
    }

    public Pipeline pipelineAttributes(Set<PipelineAttribute> pipelineAttributes) {
        this.setPipelineAttributes(pipelineAttributes);
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

    public CAConnectorConfig getCaConnector() {
        return this.caConnector;
    }

    public void setCaConnector(CAConnectorConfig cAConnectorConfig) {
        this.caConnector = cAConnectorConfig;
    }

    public Pipeline caConnector(CAConnectorConfig cAConnectorConfig) {
        this.setCaConnector(cAConnectorConfig);
        return this;
    }

    public BPMNProcessInfo getProcessInfo() {
        return this.processInfo;
    }

    public void setProcessInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.processInfo = bPMNProcessInfo;
    }

    public Pipeline processInfo(BPMNProcessInfo bPMNProcessInfo) {
        this.setProcessInfo(bPMNProcessInfo);
        return this;
    }

    public Set<AlgorithmRestriction> getAlgorithms() {
        return this.algorithms;
    }

    public void setAlgorithms(Set<AlgorithmRestriction> algorithmRestrictions) {
        this.algorithms = algorithmRestrictions;
    }

    public Pipeline algorithms(Set<AlgorithmRestriction> algorithmRestrictions) {
        this.setAlgorithms(algorithmRestrictions);
        return this;
    }

    public Pipeline addAlgorithms(AlgorithmRestriction algorithmRestriction) {
        this.algorithms.add(algorithmRestriction);
        algorithmRestriction.getPipelines().add(this);
        return this;
    }

    public Pipeline removeAlgorithms(AlgorithmRestriction algorithmRestriction) {
        this.algorithms.remove(algorithmRestriction);
        algorithmRestriction.getPipelines().remove(this);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

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
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Pipeline{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", type='" + getType() + "'" +
            ", urlPart='" + getUrlPart() + "'" +
            ", active='" + getActive() + "'" +
            ", description='" + getDescription() + "'" +
            ", approvalRequired='" + getApprovalRequired() + "'" +
            "}";
    }
}
