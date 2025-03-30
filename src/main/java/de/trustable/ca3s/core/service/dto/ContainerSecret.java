package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class ContainerSecret implements Serializable {
    private String secret;

    public ContainerSecret(){}

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
