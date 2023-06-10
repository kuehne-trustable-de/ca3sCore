package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;

import java.io.Serializable;
import java.time.Instant;

public class CaConnectorConfigView implements Serializable {

    private Long id;

    private String name;

    private CAConnectorType caConnectorType;

    private String caUrl;

    private Integer pollingOffset;

    private Boolean defaultCA;

    private Boolean active;

    private Boolean trustSelfsignedCertificates;

    private String selector;

    private Interval interval;

    private Boolean messageProtectionPassphrase;

    private String plainSecret;

    private Instant secretValidTo;

    private Long tlsAuthenticationId;

    private Long messageProtectionId;

    private String issuerName = null;
    private NamedValue[] aTaVArr = new NamedValue[0];
    private boolean multipleMessages = true;
    private boolean implicitConfirm = true;

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

    public CAConnectorType getCaConnectorType() {
        return caConnectorType;
    }

    public void setCaConnectorType(CAConnectorType caConnectorType) {
        this.caConnectorType = caConnectorType;
    }

    public String getCaUrl() {
        return caUrl;
    }

    public void setCaUrl(String caUrl) {
        this.caUrl = caUrl;
    }

    public Integer getPollingOffset() {
        return pollingOffset;
    }

    public void setPollingOffset(Integer pollingOffset) {
        this.pollingOffset = pollingOffset;
    }

    public Boolean getDefaultCA() {
        return defaultCA;
    }

    public void setDefaultCA(Boolean defaultCA) {
        this.defaultCA = defaultCA;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getTrustSelfsignedCertificates() {
        return trustSelfsignedCertificates;
    }

    public void setTrustSelfsignedCertificates(Boolean trustSelfsignedCertificates) {
        this.trustSelfsignedCertificates = trustSelfsignedCertificates;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Boolean getMessageProtectionPassphrase() {
        return messageProtectionPassphrase;
    }

    public void setMessageProtectionPassphrase(Boolean messageProtectionPassphrase) {
        this.messageProtectionPassphrase = messageProtectionPassphrase;
    }

    public String getPlainSecret() {
        return plainSecret;
    }

    public void setPlainSecret(String plainSecret) {
        this.plainSecret = plainSecret;
    }

    public Instant getSecretValidTo() {
        return secretValidTo;
    }

    public void setSecretValidTo(Instant secretValidTo) {
        this.secretValidTo = secretValidTo;
    }

    public String getIssuerName() {
        return issuerName;
    }

    public void setIssuerName(String issuerName) {
        this.issuerName = issuerName;
    }

    public NamedValue[] getaTaVArr() {
        return aTaVArr;
    }

    public void setaTaVArr(NamedValue[] aTaVArr) {
        this.aTaVArr = aTaVArr;
    }

    public boolean isMultipleMessages() {
        return multipleMessages;
    }

    public void setMultipleMessages(boolean multipleMessages) {
        this.multipleMessages = multipleMessages;
    }

    public boolean isImplicitConfirm() {
        return implicitConfirm;
    }

    public void setImplicitConfirm(boolean implicitConfirm) {
        this.implicitConfirm = implicitConfirm;
    }

    public Long getTlsAuthenticationId() {
        return tlsAuthenticationId;
    }

    public void setTlsAuthenticationId(Long tlsAuthenticationId) {
        this.tlsAuthenticationId = tlsAuthenticationId;
    }

    public Long getMessageProtectionId() {
        return messageProtectionId;
    }

    public void setMessageProtectionId(Long messageProtectionId) {
        this.messageProtectionId = messageProtectionId;
    }
}
