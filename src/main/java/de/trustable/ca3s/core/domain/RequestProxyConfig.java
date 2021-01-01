package de.trustable.ca3s.core.domain;


import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * A RequestProxyConfig.
 */
@Entity
@Table(name = "request_proxy_config")
public class RequestProxyConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    public RequestProxyConfig name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestProxyUrl() {
        return requestProxyUrl;
    }

    public RequestProxyConfig requestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
        return this;
    }

    public void setRequestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
    }

    public Boolean isActive() {
        return active;
    }

    public RequestProxyConfig active(Boolean active) {
        this.active = active;
        return this;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProtectedContent getSecret() {
        return secret;
    }

    public RequestProxyConfig secret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
        return this;
    }

    public void setSecret(ProtectedContent protectedContent) {
        this.secret = protectedContent;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

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
        return 31;
    }

    @Override
    public String toString() {
        return "RequestProxyConfig{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", requestProxyUrl='" + getRequestProxyUrl() + "'" +
            ", active='" + isActive() + "'" +
            "}";
    }
}
