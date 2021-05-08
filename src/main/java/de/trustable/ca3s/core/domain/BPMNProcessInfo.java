package de.trustable.ca3s.core.domain;

import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import java.io.Serializable;
import java.time.Instant;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A BPMNProcessInfo.
 */
@Entity
@Table(name = "bpmn_process_info")
@NamedQueries({
	@NamedQuery(name = "BPMNProcessInfo.findByName",
	query = "SELECT bi FROM BPMNProcessInfo bi WHERE " +
			"bi.name = :name"
    ),

})
public class BPMNProcessInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "version", nullable = false)
    private String version;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private BPMNProcessType type;

    @NotNull
    @Column(name = "author", nullable = false)
    private String author;

    @NotNull
    @Column(name = "last_change", nullable = false)
    private Instant lastChange;

    @Lob
    @Column(name = "signature_base_64", nullable = false)
    private String signatureBase64;

    @NotNull
    @Column(name = "bpmn_hash_base_64", nullable = false)
    private String bpmnHashBase64;

    @Lob
    @Column(name = "process_id", nullable = false)
    private String processId;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BPMNProcessInfo id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public BPMNProcessInfo name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return this.version;
    }

    public BPMNProcessInfo version(String version) {
        this.version = version;
        return this;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public BPMNProcessType getType() {
        return this.type;
    }

    public BPMNProcessInfo type(BPMNProcessType type) {
        this.type = type;
        return this;
    }

    public void setType(BPMNProcessType type) {
        this.type = type;
    }

    public String getAuthor() {
        return this.author;
    }

    public BPMNProcessInfo author(String author) {
        this.author = author;
        return this;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getLastChange() {
        return this.lastChange;
    }

    public BPMNProcessInfo lastChange(Instant lastChange) {
        this.lastChange = lastChange;
        return this;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public String getSignatureBase64() {
        return this.signatureBase64;
    }

    public BPMNProcessInfo signatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
        return this;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }

    public String getBpmnHashBase64() {
        return this.bpmnHashBase64;
    }

    public BPMNProcessInfo bpmnHashBase64(String bpmnHashBase64) {
        this.bpmnHashBase64 = bpmnHashBase64;
        return this;
    }

    public void setBpmnHashBase64(String bpmnHashBase64) {
        this.bpmnHashBase64 = bpmnHashBase64;
    }

    public String getProcessId() {
        return this.processId;
    }

    public BPMNProcessInfo processId(String processId) {
        this.processId = processId;
        return this;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BPMNProcessInfo)) {
            return false;
        }
        return id != null && id.equals(((BPMNProcessInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BPMNProcessInfo{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", version='" + getVersion() + "'" +
            ", type='" + getType() + "'" +
            ", author='" + getAuthor() + "'" +
            ", lastChange='" + getLastChange() + "'" +
            ", signatureBase64='" + getSignatureBase64() + "'" +
            ", bpmnHashBase64='" + getBpmnHashBase64() + "'" +
            ", processId='" + getProcessId() + "'" +
            "}";
    }
}
