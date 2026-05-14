package de.trustable.ca3s.core.service.dto;

import java.time.Instant;
import java.util.List;

public class CrlStatusSet {

    private List<CrlUrlStatus> crlUrlStatusList;
    private Instant lastUpdate;

    public CrlStatusSet(){}

    public CrlStatusSet(List<CrlUrlStatus> crlUrlStatusList, Instant lastUpdate){
        this.crlUrlStatusList = crlUrlStatusList;
        this.lastUpdate = lastUpdate;
    }

    public List<CrlUrlStatus> getCrlUrlStatusList() {
        return crlUrlStatusList;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }
}
