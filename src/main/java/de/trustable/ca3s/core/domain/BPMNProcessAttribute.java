package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A BPMNProcessInfoAttribute.
 */
@Entity
@Table(name = "bpmn_process_info_attribute")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BPMNProcessAttribute implements Serializable {

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

    @Column(name = "protected_content")
    private Boolean protectedContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("bpmnProcessInfoAttributes")
    private BPMNProcessInfo bpmnProcessInfo;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public BPMNProcessAttribute id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public BPMNProcessAttribute name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return this.value0;
    }

    public BPMNProcessAttribute value(String value0) {
        this.setValue(value0);
        return this;
    }

    public void setValue(String value) {
        this.value0 = value;
    }

    public Boolean getProtectedContent() {
        return this.protectedContent;
    }

    public BPMNProcessAttribute protectedContent(Boolean protectedContent) {
        this.setProtectedContent(protectedContent);
        return this;
    }

    public void setProtectedContent(Boolean protectedContent) {
        this.protectedContent = protectedContent;
    }

    public BPMNProcessInfo getBpmnProcessInfo() {
        return bpmnProcessInfo;
    }

    public void setBpmnProcessInfo(BPMNProcessInfo bpmnProcessInfo) {
        this.bpmnProcessInfo = bpmnProcessInfo;
    }

// jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BPMNProcessAttribute)) {
            return false;
        }
        return id != null && id.equals(((BPMNProcessAttribute) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BPMNProcessAttribute{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", value='" + getValue() + "'" +
            "}";
    }
}
