package de.trustable.ca3s.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A RDN.
 */
@Entity
@Table(name = "rdn")
public class RDN implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToMany(mappedBy = "rdn")
    @JsonIgnoreProperties(value = { "rdn" }, allowSetters = true)
    private Set<RDNAttribute> rdnAttributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "comment", "rdns", "ras", "csrAttributes", "pipeline", "certificate" }, allowSetters = true)
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RDN id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<RDNAttribute> getRdnAttributes() {
        return this.rdnAttributes;
    }

    public void setRdnAttributes(Set<RDNAttribute> rDNAttributes) {
        if (this.rdnAttributes != null) {
            this.rdnAttributes.forEach(i -> i.setRdn(null));
        }
        if (rDNAttributes != null) {
            rDNAttributes.forEach(i -> i.setRdn(this));
        }
        this.rdnAttributes = rDNAttributes;
    }

    public RDN rdnAttributes(Set<RDNAttribute> rDNAttributes) {
        this.setRdnAttributes(rDNAttributes);
        return this;
    }

    public RDN addRdnAttributes(RDNAttribute rDNAttribute) {
        this.rdnAttributes.add(rDNAttribute);
        rDNAttribute.setRdn(this);
        return this;
    }

    public RDN removeRdnAttributes(RDNAttribute rDNAttribute) {
        this.rdnAttributes.remove(rDNAttribute);
        rDNAttribute.setRdn(null);
        return this;
    }

    public CSR getCsr() {
        return this.csr;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }

    public RDN csr(CSR cSR) {
        this.setCsr(cSR);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RDN)) {
            return false;
        }
        return id != null && id.equals(((RDN) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RDN{" +
            "id=" + getId() +
            "}";
    }
}
