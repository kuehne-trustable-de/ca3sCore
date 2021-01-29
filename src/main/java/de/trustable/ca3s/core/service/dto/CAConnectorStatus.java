package de.trustable.ca3s.core.service.dto;

import java.io.Serializable;

public class CAConnectorStatus implements Serializable {

    private Long connectorId;

    private String name;

    private CAStatus status;


    public CAConnectorStatus(Long connectorId, String name, CAStatus status) {
        this.connectorId = connectorId;
        this.name = name;
        this.status = status;
    }

    public Long getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(Long connectorId) {
        this.connectorId = connectorId;
    }

    public CAStatus getStatus() {
        return status;
    }

    public void setStatus(CAStatus status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
