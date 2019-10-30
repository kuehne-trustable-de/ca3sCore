package de.trustable.ca3sjh.domain;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * A RDN.
 */
@Entity
@Table(name = "rdn")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class RDN implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "rdn")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<RDNAttribute> rdnAttributes = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties("rdns")
    private CSR csr;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<RDNAttribute> getRdnAttributes() {
        return rdnAttributes;
    }

    public RDN rdnAttributes(Set<RDNAttribute> rDNAttributes) {
        this.rdnAttributes = rDNAttributes;
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

    public void setRdnAttributes(Set<RDNAttribute> rDNAttributes) {
        this.rdnAttributes = rDNAttributes;
    }

    public CSR getCsr() {
        return csr;
    }

    public RDN csr(CSR cSR) {
        this.csr = cSR;
        return this;
    }

    public void setCsr(CSR cSR) {
        this.csr = cSR;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        return 31;
    }

    @Override
    public String toString() {
        return "RDN{" +
            "id=" + getId() +
            "}";
    }
}
