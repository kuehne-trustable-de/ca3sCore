package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.service.dto.CrlStatusSet;

public interface CRLStatusService {

    public void updateStatus();

    public CrlStatusSet getCrlStatusSet();
}
