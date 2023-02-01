package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class CRLExpirationNotificationView implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String crlUrl;

    private Duration notifyBefore;

    private Instant notifyUntil;

    private Boolean isExpired;

    private Long certificateCount;


    public CRLExpirationNotificationView() {}

    public CRLExpirationNotificationView(CRLExpirationNotification crlExpirationNotification) {
        this.id = crlExpirationNotification.getId();
        this.crlUrl = crlExpirationNotification.getCrlUrl();
        this.notifyBefore = crlExpirationNotification.getNotifyBefore();
        this.notifyUntil = crlExpirationNotification.getNotifyUntil();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCrlUrl() {
        return crlUrl;
    }

    public void setCrlUrl(String crlUrl) {
        this.crlUrl = crlUrl;
    }

    public Duration getNotifyBefore() {
        return notifyBefore;
    }

    public void setNotifyBefore(Duration notifyBefore) {
        this.notifyBefore = notifyBefore;
    }

    public Instant getNotifyUntil() {
        return notifyUntil;
    }

    public void setNotifyUntil(Instant notifyUntil) {
        this.notifyUntil = notifyUntil;
    }

    public Boolean getExpired() {
        return isExpired;
    }

    public void setExpired(Boolean expired) {
        isExpired = expired;
    }

    public Long getCertificateCount() {
        return certificateCount;
    }

    public void setCertificateCount(Long certificateCount) {
        this.certificateCount = certificateCount;
    }
}
