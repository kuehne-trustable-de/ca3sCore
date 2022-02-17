package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.ScepOrderStatus;

import java.io.Serializable;
import java.time.Instant;

public class ScepOrderView implements Serializable {

    private Long id;
    private String transId;
    private ScepOrderStatus status;
    private String realm;
    private String pipelineName;
    private String subject;
    private String sans;
    private String[] sanArr;

    private Instant requestedOn;
    private String requestedBy;

    private Boolean asyncProcessing;
    private Boolean passwordAuthentication;

    private Long csrId;
    private Long certificateId;

    public ScepOrderView() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public ScepOrderStatus getStatus() {
        return status;
    }

    public void setStatus(ScepOrderStatus status) {
        this.status = status;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }


    public Instant getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(Instant requestedOn) {
        this.requestedOn = requestedOn;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Boolean getAsyncProcessing() {
        return asyncProcessing;
    }

    public void setAsyncProcessing(Boolean asyncProcessing) {
        this.asyncProcessing = asyncProcessing;
    }

    public Boolean getPasswordAuthentication() {
        return passwordAuthentication;
    }

    public void setPasswordAuthentication(Boolean passwordAuthentication) {
        this.passwordAuthentication = passwordAuthentication;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSans() {
        return sans;
    }

    public void setSans(String sans) {
        this.sans = sans;
    }

    public String[] getSanArr() {
        return sanArr;
    }

    public void setSanArr(String[] sanArr) {
        this.sanArr = sanArr;
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
}
