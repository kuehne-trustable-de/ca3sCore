package de.trustable.ca3s.core.service.dto;

import java.time.Instant;

public class CrlUrlStatusView {

    private final String crlUrl;
    private final CrlEndpointStatus crlEndpointStatus;
    private final Instant nextUpdate;
    private final Instant thisUpdate;
    private final String issuerName;

    public CrlUrlStatusView(CrlUrlStatus crlUrlStatus){
        this.crlUrl = crlUrlStatus.getCrlUrl();
        this.crlEndpointStatus = crlUrlStatus.getCrlEndpointStatus();

        if(crlUrlStatus.getCrl() != null) {
            this.nextUpdate = crlUrlStatus.getCrl().getNextUpdate().toInstant();
            this.thisUpdate = crlUrlStatus.getCrl().getThisUpdate().toInstant();
            this.issuerName = crlUrlStatus.getCrl().getIssuerX500Principal().getName();
        }else{
            this.nextUpdate = null;
            this.thisUpdate = null;
            this.issuerName = null;
        }
    }

    public String getCrlUrl() {
        return crlUrl;
    }

    public CrlEndpointStatus getCrlEndpointStatus() {
        return crlEndpointStatus;
    }

    public Instant getNextUpdate() {
        return nextUpdate;
    }

    public Instant getThisUpdate() {
        return thisUpdate;
    }

    public String getIssuerName() {
        return issuerName;
    }
}
