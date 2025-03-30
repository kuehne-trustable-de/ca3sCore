package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.BPMNProcessAttribute;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.time.Instant;

public class BPMNProcessInfoView implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Logger LOG = LoggerFactory.getLogger(CertificateView.class);

    private Long id;
    private String name;
    private String version;
    private BPMNProcessType type;
    private String author;
    private Instant lastChange;
    private String signatureBase64;
    private String bpmnHashBase64;
    private String processId;

    @JsonIgnoreProperties("bpmnProcessInfo")
    private BPMNProcessAttribute[] bpmnProcessAttributes;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public BPMNProcessType getType() {
        return type;
    }

    public void setType(BPMNProcessType type) {
        this.type = type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getLastChange() {
        return lastChange;
    }

    public void setLastChange(Instant lastChange) {
        this.lastChange = lastChange;
    }

    public String getSignatureBase64() {
        return signatureBase64;
    }

    public void setSignatureBase64(String signatureBase64) {
        this.signatureBase64 = signatureBase64;
    }

    public String getBpmnHashBase64() {
        return bpmnHashBase64;
    }

    public void setBpmnHashBase64(String bpmnHashBase64) {
        this.bpmnHashBase64 = bpmnHashBase64;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public BPMNProcessAttribute[] getBpmnProcessAttributes() {
        return bpmnProcessAttributes;
    }

    public void setBpmnProcessAttributes(BPMNProcessAttribute[] bpmnProcessAttributes) {
        this.bpmnProcessAttributes = bpmnProcessAttributes;
    }
}
