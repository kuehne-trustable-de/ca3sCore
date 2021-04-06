package de.trustable.ca3s.core.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.trustable.ca3s.core.domain.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.time.Instant;

public class AuditTraceView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String actorName;

    private String actorRole;

    private String plainContent;

    private String contentTemplate;

    private Instant createdOn;

    private Long csrId;

    private Long certificateId;

    private Long pipelineId;

    private Long caConnectorId;

    private Long processInfoId;

    public AuditTraceView(){}

    public AuditTraceView(final AuditTrace auditTrace){

        this.id = auditTrace.getId();
        this.actorName = auditTrace.getActorName();
        this.actorRole = auditTrace.getActorRole();
        this.contentTemplate = auditTrace.getContentTemplate();
        this.plainContent = auditTrace.getPlainContent();
        this.createdOn = auditTrace.getCreatedOn();
        if(auditTrace.getCsr() != null){
            this.csrId = auditTrace.getCsr().getId();
        }
        if(auditTrace.getCertificate() != null){
            this.certificateId = auditTrace.getCertificate().getId();
        }
        if(auditTrace.getCaConnector() != null){
            this.caConnectorId = auditTrace.getCaConnector().getId();
        }
        if(auditTrace.getPipeline() != null){
            this.pipelineId = auditTrace.getPipeline().getId();
        }
        if(auditTrace.getProcessInfo() != null){
            this.processInfoId = auditTrace.getProcessInfo().getId();
        }
        if(auditTrace.getCsr() != null){
            this.csrId = auditTrace.getCsr().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public void setActorRole(String actorRole) {
        this.actorRole = actorRole;
    }

    public String getPlainContent() {
        return plainContent;
    }

    public void setPlainContent(String plainContent) {
        this.plainContent = plainContent;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Instant createdOn) {
        this.createdOn = createdOn;
    }

    public Long getCsrId() {
        return csrId;
    }

    public void setCsrId(Long csrId) {
        this.csrId = csrId;
    }

    public Long getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(Long certificateId) {
        this.certificateId = certificateId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public Long getCaConnectorId() {
        return caConnectorId;
    }

    public void setCaConnectorId(Long caConnectorId) {
        this.caConnectorId = caConnectorId;
    }

    public Long getProcessInfoId() {
        return processInfoId;
    }

    public void setProcessInfoId(Long processInfoId) {
        this.processInfoId = processInfoId;
    }

    @Override
    public String toString() {
        return "AuditTrace{" +
            "id=" + getId() +
            ", actorName='" + getActorName() + "'" +
            ", actorRole='" + getActorRole() + "'" +
            ", plainContent='" + getPlainContent() + "'" +
            ", contentTemplate='" + getContentTemplate() + "'" +
            ", createdOn='" + getCreatedOn() + "'" +
            "}";
    }
}
