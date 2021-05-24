package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;

public class BPMNUpload {

	String name;
    private BPMNProcessType type;
	String contentXML;

	public BPMNUpload() {

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

    public String getContentXML() {
        return contentXML;
    }

    public void setContentXML(String contentXML) {
        this.contentXML = contentXML;
    }
}
