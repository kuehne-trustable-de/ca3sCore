package de.trustable.ca3s.core.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A RequestProxyConfig.
 */
@Entity
@Table(name = "request_proxy_config")
public class RequestProxyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "request_proxy_url", nullable = false)
    private String requestProxyUrl;

    @Column(name = "active")
    private Boolean active;

    @OneToOne
    @JoinColumn(unique = true)
    private ProtectedContent secret;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RequestProxyConfig id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public RequestProxyConfig name(String name) {
        this.setName(name);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestProxyUrl() {
        return this.requestProxyUrl;
    }

    public RequestProxyConfig requestProxyUrl(String requestProxyUrl) {
        this.setRequestProxyUrl(requestProxyUrl);
        return this;
    }

    public void setRequestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
    }

    public Boolean isActive() {
        return active;
    }

    public Boolean getActive() {
        return this.active;
    }

    public RequestProxyConfig active(Boolean active) {
        this.setActive(active);
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProtectedContent getSecret() {
        return this.secret;
    }

    public void setSecret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
    }

    public RequestProxyConfig secret(ProtectedContent protectedContent) {
        this.setSecret(protectedContent);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RequestProxyConfig)) {
            return false;
        }
        return id != null && id.equals(((RequestProxyConfig) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RequestProxyConfig{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", requestProxyUrl='" + getRequestProxyUrl() + "'" +
            ", active='" + getActive() + "'" +
            "}";
    }
}
