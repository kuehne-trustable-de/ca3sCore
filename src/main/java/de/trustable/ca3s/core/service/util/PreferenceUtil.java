package de.trustable.ca3s.core.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.service.dto.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;

@Service
public class PreferenceUtil {

    private static final Logger log = LoggerFactory.getLogger(PreferenceUtil.class);

    public static final Long SYSTEM_PREFERENCE_ID = 1L;

    public static final String CHECK_CRL = "CheckCRL";
    public static final String MAX_NEXT_UPDATE_PERIOD_CRL_SEC = "MaxNextUpdatePeriodCrlSec";
	public static final String ACME_HTTP01_TIMEOUT_MILLI_SEC = "AcmeHTTP01TimeoutMilliSec";
	public static final String ACME_HTTP01_CALLBACK_PORTS = "AcmeHTTP01CallbackPorts";
	public static final String SERVER_SIDE_KEY_CREATION_ALLOWED = "ServerSideKeyCreationAllowed";

	@Autowired
    private UserPreferenceService userPreferenceService;

    public boolean isCheckCrl() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(CHECK_CRL, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public long getMaxNextUpdatePeriodCRLSec() {
        Optional<UserPreference> optLong = userPreferenceService.findPreferenceForUserId(MAX_NEXT_UPDATE_PERIOD_CRL_SEC, SYSTEM_PREFERENCE_ID);
        return optLong.map(userPreference -> Long.parseLong(userPreference.getContent())).orElse(3600L * 24L);
    }

    public boolean isServerSideKeyCreationAllowed() {
    	Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(SERVER_SIDE_KEY_CREATION_ALLOWED, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public long getAcmeHTTP01TimeoutMilliSec() {
    	Optional<UserPreference> optLong = userPreferenceService.findPreferenceForUserId(ACME_HTTP01_TIMEOUT_MILLI_SEC, SYSTEM_PREFERENCE_ID);
        return optLong.map(userPreference -> Long.parseLong(userPreference.getContent())).orElse(2000L);
    }

    public String getAcmeHTTP01CallbackPorts() {
    	Optional<UserPreference> optString = userPreferenceService.findPreferenceForUserId(ACME_HTTP01_CALLBACK_PORTS, SYSTEM_PREFERENCE_ID);
    	if( optString.isPresent()) {
    		return optString.get().getContent();
    	}
    	return "5544";
    }


    public Preferences getSystemPrefs() {
        return getPrefs(SYSTEM_PREFERENCE_ID);
    }

    public Preferences getPrefs(Long userId) {
        Preferences prefs = new Preferences();

        log.debug("REST request to get Preference for user {}", userId);
        List<UserPreference> upList = userPreferenceService.findAllForUserId(userId);

        for(UserPreference up: upList) {
            String name = up.getName();
            if( PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED.equals(name)) {
                prefs.setServerSideKeyCreationAllowed(Boolean.parseBoolean(up.getContent()));
            } else if( PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS.equals(name)) {
                String[] portArr = up.getContent().split(",");
                ArrayList<Integer> portList = new ArrayList<>();
                for( String port: portArr){
                    if( "0".equals(port)){
                        continue;
                    }
                    try {
                        portList.add(Integer.parseInt(port));
                    } catch(NumberFormatException nfe){
                        log.info("unexpected value for ACME_HTTP01_CALLBACK_PORT '{}'", port);
                    }
                }
                int[] portIntArr = new int[portList.size()];
                for( int i =0; i < portList.size(); i++){
                    portIntArr[i] = portList.get(i);
                }
                prefs.setAcmeHTTP01CallbackPortArr(portIntArr);
            } else if( PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC.equals(name)) {
                try {
                    prefs.setAcmeHTTP01TimeoutMilliSec(Long.parseLong(up.getContent()));
                } catch(NumberFormatException nfe) {
                    log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", nfe.getMessage());
                    prefs.setAcmeHTTP01TimeoutMilliSec(2000);
                }
            } else if( PreferenceUtil.CHECK_CRL.equals(name)) {
                prefs.setCheckCRL(Boolean.parseBoolean(up.getContent()));
            } else if( PreferenceUtil.MAX_NEXT_UPDATE_PERIOD_CRL_SEC.equals(name)) {

                prefs.setMaxNextUpdatePeriodCRLHour((Long.parseLong(up.getContent()) + 1800L) / 3600L) ;
            }
        }
        return prefs;
    }

}
