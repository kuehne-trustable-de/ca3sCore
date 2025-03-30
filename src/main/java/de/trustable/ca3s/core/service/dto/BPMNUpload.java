package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.BPMNProcessAttribute;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;

public class BPMNUpload {

    Long id;

    private String name;
    private BPMNProcessType type;

    private String version;
    private String contentXML;

    private BPMNProcessAttribute[] bpmnProcessAttributes;

    public BPMNUpload() {

	}

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

    public BPMNProcessType getType() {
        return type;
    }

    public void setType(BPMNProcessType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getContentXML() {
        return contentXML;
    }

    public void setContentXML(String contentXML) {
        this.contentXML = contentXML;
    }

    public BPMNProcessAttribute[] getBpmnProcessAttributes() {
        return bpmnProcessAttributes;
    }

    public void setBpmnProcessAttributes(BPMNProcessAttribute[] bpmnProcessAttributes) {
        this.bpmnProcessAttributes = bpmnProcessAttributes;
    }
}
