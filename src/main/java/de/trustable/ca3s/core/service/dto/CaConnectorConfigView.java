package de.trustable.ca3s.core.service.dto;

import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;

import javax.persistence.Column;
import java.io.Serializable;
import java.time.Instant;

public class CaConnectorConfigView implements Serializable {

    private Long id;

    private String name;

    private CAConnectorType caConnectorType;

    private String caUrl;
    private String sni;
    private boolean disableHostNameVerifier;

    private String msgContentType;

    private Integer pollingOffset;

    private Instant lastUpdate;

    private Boolean defaultCA;

    private Boolean active;

    private Boolean trustSelfsignedCertificates;

    private String selector;

    private Boolean isDatabase;

    private String certificateTable;

    private String certificateColumn;

    private String sequenceColumn;

    private String lastUpdateColumn;

    private String role;

    private Interval interval;

    private boolean messageProtectionPassphrase;

    private Long tlsAuthenticationId;

    private Long messageProtectionId;

    private boolean ignoreResponseMessageVerification = false;

    private String issuerName = null;
    private NamedValue[] aTaVArr = new NamedValue[0];
    private boolean multipleMessages = true;
    private boolean implicitConfirm = true;
    private boolean fillEmptySubjectWithSAN = true;
    private Instant expiryDate;

    private boolean allowCrlOnHold = false;

    private AuthenticationParameter authenticationParameter;

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

    public String getSni() {
        return sni;
    }

    public void setSni(String sni) {
        this.sni = sni;
    }


    public String getMsgContentType() {
        return msgContentType;
    }

    public void setMsgContentType(String msgContentType) {
        this.msgContentType = msgContentType;
    }

    public Integer getPollingOffset() {
        return pollingOffset;
    }

    public void setPollingOffset(Integer pollingOffset) {
        this.pollingOffset = pollingOffset;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public Boolean getDatabase() {
        return isDatabase;
    }

    public void setDatabase(Boolean database) {
        isDatabase = database;
    }

    public String getCertificateTable() {
        return certificateTable;
    }

    public void setCertificateTable(String certificateTable) {
        this.certificateTable = certificateTable;
    }

    public String getCertificateColumn() {
        return certificateColumn;
    }

    public void setCertificateColumn(String certificateColumn) {
        this.certificateColumn = certificateColumn;
    }

    public String getSequenceColumn() {
        return sequenceColumn;
    }

    public void setSequenceColumn(String sequenceColumn) {
        this.sequenceColumn = sequenceColumn;
    }

    public String getLastUpdateColumn() {
        return lastUpdateColumn;
    }

    public void setLastUpdateColumn(String lastUpdateColumn) {
        this.lastUpdateColumn = lastUpdateColumn;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
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

    public boolean isDisableHostNameVerifier() {
        return disableHostNameVerifier;
    }

    public void setDisableHostNameVerifier(boolean disableHostNameVerifier) {
        this.disableHostNameVerifier = disableHostNameVerifier;
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

    public boolean isMessageProtectionPassphrase() {
        return messageProtectionPassphrase;
    }

    public void setMessageProtectionPassphrase(boolean messageProtectionPassphrase) {
        this.messageProtectionPassphrase = messageProtectionPassphrase;
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

    public boolean isIgnoreResponseMessageVerification() {
        return ignoreResponseMessageVerification;
    }

    public void setIgnoreResponseMessageVerification(boolean ignoreResponseMessageVerification) {
        this.ignoreResponseMessageVerification = ignoreResponseMessageVerification;
    }

    public boolean isFillEmptySubjectWithSAN() {
        return fillEmptySubjectWithSAN;
    }

    public void setFillEmptySubjectWithSAN(boolean fillEmptySubjectWithSAN) {
        this.fillEmptySubjectWithSAN = fillEmptySubjectWithSAN;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }
    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public AuthenticationParameter getAuthenticationParameter() {
        return authenticationParameter;
    }

    public void setAuthenticationParameter(AuthenticationParameter authenticationParameter) {
        this.authenticationParameter = authenticationParameter;
    }

    public boolean isAllowCrlOnHold() {
        return allowCrlOnHold;
    }

    public void setAllowCrlOnHold(boolean allowCrlOnHold) {
        this.allowCrlOnHold = allowCrlOnHold;
    }
}
