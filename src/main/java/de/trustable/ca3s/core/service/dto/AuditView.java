package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.util.CryptoUtil;

import javax.persistence.Column;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

public class AuditView implements Serializable {

    private Long id;

    private String actorName;

    private String actorRole;

    private String plainContent;

    private String[] contentParts;

    private String contentTemplate;

    private Instant createdOn;

    public AuditView(){}

    public AuditView(final AuditTrace auditTrace){

        this.id = auditTrace.getId();

        this.actorName = auditTrace.getActorName();
        this.actorRole = auditTrace.getActorRole();
        this.plainContent = auditTrace.getPlainContent();
        this.contentTemplate = auditTrace.getContentTemplate();
        this.createdOn = auditTrace.getCreatedOn();

        this.contentParts = this.plainContent.split(",");
        for( int i = 0; i < this.contentParts.length; i++ ){
            this.contentParts[i] = this.contentParts[i].replace("%2C", ",").replace("%25", "%");
        }

    }

    public Long getId() {
        return id;
    }

    public String getActorName() {
        return actorName;
    }

    public String getActorRole() {
        return actorRole;
    }

    public String getPlainContent() {
        return plainContent;
    }

    public String[] getContentParts() {
        return contentParts;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public Instant getCreatedOn() {
        return createdOn;
    }
}
