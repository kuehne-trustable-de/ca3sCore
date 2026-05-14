package de.trustable.ca3s.core.service.dto;

import java.security.cert.X509CRL;

public class CrlUrlStatus {

    private String crlUrl;
    private CrlEndpointStatus crlEndpointStatus;
    private X509CRL crl;

    public String getCrlUrl() {
        return crlUrl;
    }

    public void setCrlUrl(String crlUrl) {
        this.crlUrl = crlUrl;
    }

    public CrlEndpointStatus getCrlEndpointStatus() {
        return crlEndpointStatus;
    }

    public void setCrlEndpointStatus(CrlEndpointStatus crlEndpointStatus) {
        this.crlEndpointStatus = crlEndpointStatus;
    }

    public X509CRL getCrl() {
        return crl;
    }

    public void setCrl(X509CRL crl) {
        this.crl = crl;
    }
}
