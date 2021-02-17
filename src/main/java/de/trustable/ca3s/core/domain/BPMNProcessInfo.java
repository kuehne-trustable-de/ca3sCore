package de.trustable.ca3s.core.domain;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.Instant;

import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;

/**
 * A BPNMProcessInfo.
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

    public BPMNProcessInfo name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public BPMNProcessInfo version(String version) {
        this.version = version;
        return this;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public BPMNProcessType getType() {
        return type;
    }

    public BPMNProcessInfo type(BPMNProcessType type) {
        this.type = type;
        return this;
    }

    public void setType(BPMNProcessType type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public BPMNProcessInfo author(String author) {
        this.author = author;
        return this;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    public BPMNProcessInfo lastChange(Instant lastChange) {
        this.lastChange = lastChange;
        return this;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public String getSignatureBase64() {
        return signatureBase64;
    }

    public BPMNProcessInfo signatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
        return this;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        return 31;
    }

    @Override
    public String toString() {
        return "BPNMProcessInfo{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", version='" + getVersion() + "'" +
            ", type='" + getType() + "'" +
            ", author='" + getAuthor() + "'" +
            ", lastChange='" + getLastChange() + "'" +
            ", signatureBase64='" + getSignatureBase64() + "'" +
            "}";
    }
}
