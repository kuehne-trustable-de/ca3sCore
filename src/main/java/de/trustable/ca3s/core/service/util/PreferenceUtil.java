package de.trustable.ca3s.core.service.util;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;

@Service
public class PreferenceUtil {


    public static final String CHECK_CRL = "CheckCRL";
	public static final String ACME_HTTP01_TIMEOUT_MILLI_SEC = "AcmeHTTP01TimeoutMilliSec";
	public static final String ACME_HTTP01_CALLBACK_PORTS = "AcmeHTTP01CallbackPorts";
	public static final String SERVER_SIDE_KEY_CREATION_ALLOWED = "ServerSideKeyCreationAllowed";

	@Autowired
    private UserPreferenceService userPreferenceService;

    public boolean isCheckCrl() {
    	Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(CHECK_CRL, 1L);
    	if( optBoolean.isPresent()) {
    		return Boolean.parseBoolean(optBoolean.get().getContent());
    	}
    	return false;
    }
    
    public boolean isServerSideKeyCreationAllowed() {
    	Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(SERVER_SIDE_KEY_CREATION_ALLOWED, 1L);
    	if( optBoolean.isPresent()) {
    		return Boolean.parseBoolean(optBoolean.get().getContent());
    	}
    	return false;
    }
    
    public long getAcmeHTTP01TimeoutMilliSec() {
    	Optional<UserPreference> optLong = userPreferenceService.findPreferenceForUserId(ACME_HTTP01_TIMEOUT_MILLI_SEC, 1L);
    	if( optLong.isPresent()) {
    		return Long.parseLong(optLong.get().getContent());
    	}
    	return 2000L;
    }
    
    public String getAcmeHTTP01CallbackPorts() {
    	Optional<UserPreference> optString = userPreferenceService.findPreferenceForUserId(ACME_HTTP01_CALLBACK_PORTS, 1L);
    	if( optString.isPresent()) {
    		return optString.get().getContent();
    	}
    	return "5544";
    }
    
}
