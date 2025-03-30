package de.trustable.ca3s.core.service.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.config.ClientAuthConfig;
import de.trustable.ca3s.core.config.PreferenceDefaults;
import de.trustable.ca3s.core.service.dto.Preferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;

@Service
public class PreferenceUtil {

    private static final Logger log = LoggerFactory.getLogger(PreferenceUtil.class);

    public static final Long SYSTEM_PREFERENCE_ID = 1L;

    public static final String LIST_DELIMITER = ",";

    public static final String CHECK_CRL = "CheckCRL";
    public static final String NOTIFY_RA_ON_REQUEST = "NotifyRAOnRequest";
    public static final String MAX_NEXT_UPDATE_PERIOD_CRL_SEC = "MaxNextUpdatePeriodCrlSec";
	public static final String ACME_HTTP01_TIMEOUT_MILLI_SEC = "AcmeHTTP01TimeoutMilliSec";
	public static final String ACME_HTTP01_CALLBACK_PORTS = "AcmeHTTP01CallbackPorts";
    public static final String SERVER_SIDE_KEY_CREATION_ALLOWED = "ServerSideKeyCreationAllowed";
    public static final String SERVER_SIDE_KEY_DELETE_AFTER_DAYS = "ServerSideKeyDeleteAfterDays";
    public static final String SERVER_SIDE_KEY_DELETE_AFTER_USES = "ServerSideKeyDeleteAfterUses";
    public static final String SELECTED_HASHES = "SelectedHashes";
    public static final String SELECTED_SIGNING_ALGOS = "SelectedSigningAlgos";
    public static final String AUTH_CLIENT_CERT = "AuthClientCert";
    public static final String AUTH_TOTP = "AuthTotp";
    public static final String AUTH_EMAIL = "AuthEmail";
    public static final String AUTH_SMS = "AuthSms";

    private final UserPreferenceService userPreferenceService;

    private final PreferenceDefaults preferenceDefaults;

    private final ClientAuthConfig clientAuthConfig;

    public PreferenceUtil(UserPreferenceService userPreferenceService, PreferenceDefaults preferenceDefaults, ClientAuthConfig clientAuthConfig) {
        this.userPreferenceService = userPreferenceService;
        this.preferenceDefaults = preferenceDefaults;
        this.clientAuthConfig = clientAuthConfig;
    }


    public boolean isCheckCrl() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(CHECK_CRL, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public boolean isNotifyRAOnRequest() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(NOTIFY_RA_ON_REQUEST, SYSTEM_PREFERENCE_ID);
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

    public int getServerSideKeyDeleteAfterDays() {
        Optional<UserPreference> optInteger = userPreferenceService.findPreferenceForUserId(SERVER_SIDE_KEY_DELETE_AFTER_DAYS, SYSTEM_PREFERENCE_ID);
        return optInteger.map(userPreference -> Integer.parseInt(userPreference.getContent())).orElse(5);
    }

    public int getServerSideKeyDeleteAfterUses() {
        Optional<UserPreference> optInteger = userPreferenceService.findPreferenceForUserId(SERVER_SIDE_KEY_DELETE_AFTER_USES, SYSTEM_PREFERENCE_ID);
        return optInteger.map(userPreference -> Integer.parseInt(userPreference.getContent())).orElse(10);
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
    public boolean isAuthClientCert() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(AUTH_CLIENT_CERT, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public boolean isAuthOtp() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(AUTH_TOTP, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public boolean isAuthEmail() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(AUTH_EMAIL, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }

    public boolean isAuthSms() {
        Optional<UserPreference> optBoolean = userPreferenceService.findPreferenceForUserId(AUTH_SMS, SYSTEM_PREFERENCE_ID);
        return optBoolean.filter(userPreference -> Boolean.parseBoolean(userPreference.getContent())).isPresent();
    }


    public Preferences getSystemPrefs() {
        return getPrefs(SYSTEM_PREFERENCE_ID);
    }

    public Preferences getPrefs(Long userId) {
        Preferences prefs = new Preferences();

        prefs.setAuthClientCertEnabled(clientAuthConfig.isTlsClientAuthActive());
        prefs.setSmsEnabled(false);

        prefs.setAvailableHashes(preferenceDefaults.getAvailableHashes());
        prefs.setAvailableSigningAlgos(preferenceDefaults.getAvailableSigningAlgos());

        // initialize the algorithm sets
        // this values have no effect once properties are set
        prefs.setSelectedHashes(preferenceDefaults.getAvailableHashes());
        prefs.setSelectedSigningAlgos(preferenceDefaults.getAvailableSigningAlgos());

        log.debug("REST request to get Preference for user {}", userId);
        List<UserPreference> upList = userPreferenceService.findAllForUserId(userId);

        for(UserPreference up: upList) {
            String name = up.getName();
            if (PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED.equals(name)) {
                prefs.setServerSideKeyCreationAllowed(Boolean.parseBoolean(up.getContent()));

            } else if (PreferenceUtil.SERVER_SIDE_KEY_DELETE_AFTER_DAYS.equals(name)) {
                prefs.setDeleteKeyAfterDays(Integer.parseInt(up.getContent()));
            } else if (PreferenceUtil.SERVER_SIDE_KEY_DELETE_AFTER_USES.equals(name)) {
                prefs.setDeleteKeyAfterUses(Integer.parseInt(up.getContent()));
            } else if (PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS.equals(name)) {
                String[] portArr = up.getContent().split(LIST_DELIMITER);
                ArrayList<Integer> portList = new ArrayList<>();
                for (String port : portArr) {
                    if ("0".equals(port)) {
                        continue;
                    }
                    try {
                        portList.add(Integer.parseInt(port));
                    } catch (NumberFormatException nfe) {
                        log.info("unexpected value for ACME_HTTP01_CALLBACK_PORT '{}'", port);
                    }
                }
                int[] portIntArr = new int[portList.size()];
                for (int i = 0; i < portList.size(); i++) {
                    portIntArr[i] = portList.get(i);
                }
                prefs.setAcmeHTTP01CallbackPortArr(portIntArr);
            } else if (PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC.equals(name)) {
                try {
                    prefs.setAcmeHTTP01TimeoutMilliSec(Long.parseLong(up.getContent()));
                } catch (NumberFormatException nfe) {
                    log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", nfe.getMessage());
                    prefs.setAcmeHTTP01TimeoutMilliSec(2000);
                }
            } else if (PreferenceUtil.NOTIFY_RA_ON_REQUEST.equals(name)) {
                prefs.setNotifyRAOnRequest(Boolean.parseBoolean(up.getContent()));
            } else if (PreferenceUtil.CHECK_CRL.equals(name)) {
                prefs.setCheckCRL(Boolean.parseBoolean(up.getContent()));
            } else if (PreferenceUtil.MAX_NEXT_UPDATE_PERIOD_CRL_SEC.equals(name)) {
                prefs.setMaxNextUpdatePeriodCRLHour((Long.parseLong(up.getContent()) + 1800L) / 3600L);
            } else if (PreferenceUtil.SELECTED_HASHES.equals(name)) {
                String[] valArr = up.getContent().split(LIST_DELIMITER);
                if (valArr.length == 0) {
                    log.error("Configuration problem: No valid hash algorithm defined");
                    continue;
                } else if (valArr.length > 10) {
                    log.warn("Configuration problem: Too many hash algorithms ({}) defined", valArr.length);
                }
                prefs.setSelectedHashes(valArr);
            } else if (PreferenceUtil.SELECTED_SIGNING_ALGOS.equals(name)) {
                String[] valArr = up.getContent().split(LIST_DELIMITER);
                if (valArr.length == 0) {
                    log.error("Configuration problem: No valid signing algorithm defined");
                    continue;
                } else if (valArr.length > 32) {
                    log.warn("Configuration problem: Too many signing algorithms ({}) defined", valArr.length);
                }
                prefs.setSelectedSigningAlgos(valArr);
            } else if (PreferenceUtil.AUTH_CLIENT_CERT.equals(name)) {
                prefs.setAuthClientCert(Boolean.parseBoolean(up.getContent()));
            } else if (PreferenceUtil.AUTH_TOTP.equals(name)) {
                prefs.setAuthTotp(Boolean.parseBoolean(up.getContent()));
            } else if (PreferenceUtil.AUTH_EMAIL.equals(name)) {
                prefs.setAuthEmail(Boolean.parseBoolean(up.getContent()));
            } else if (PreferenceUtil.AUTH_SMS.equals(name)) {
                prefs.setSms(Boolean.parseBoolean(up.getContent()));

            }
        }
        return prefs;
    }

}
