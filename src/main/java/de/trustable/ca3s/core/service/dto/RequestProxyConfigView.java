package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class RequestProxyConfigView implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String requestProxyUrl;

    private Boolean active;

    private String plainSecret;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestProxyUrl() {
        return requestProxyUrl;
    }

    public void setRequestProxyUrl(String requestProxyUrl) {
        this.requestProxyUrl = requestProxyUrl;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getPlainSecret() {
        return plainSecret;
    }

    public void setPlainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
    }

    @Override
    public String toString() {
        return "RequestProxyConfigView{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", requestProxyUrl='" + requestProxyUrl + '\'' +
            ", active=" + active +
            ", plainSecret='" + plainSecret + '\'' +
            '}';
    }
}
