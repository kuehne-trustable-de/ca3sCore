package de.trustable.ca3s.core.web.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.service.UserPreferenceService;
import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import tech.jhipster.web.util.HeaderUtil;

/**
 * REST controller for reading {@link de.trustable.ca3s.core.domain.Certificate} using the convenient CertificateView object.
 * Just read-only access to this resource.
 *
 */
@RestController
@RequestMapping("/api")
public class PreferenceResource {

	private final Logger log = LoggerFactory.getLogger(PreferenceResource.class);


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "preference";

    private final PreferenceUtil preferenceUtil;
    private final UserPreferenceService userPreferenceService;

    final private AuditService auditService;

    public PreferenceResource(PreferenceUtil preferenceUtil, UserPreferenceService userPreferenceService, AuditService auditService) {
        this.preferenceUtil = preferenceUtil;
        this.userPreferenceService = userPreferenceService;
        this.auditService = auditService;
    }



    /**
     * {@code GET  /preference/:id} : get the "id" certificate.
     *
     * @param userId the id of the preference to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the certificate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/preference/{userId}")
    public ResponseEntity<Preferences> getPreference(@PathVariable Long userId) {

    	Preferences prefs = preferenceUtil.getPrefs(userId);

   		return new ResponseEntity<>(prefs, HttpStatus.OK);
    }



    /**
     * {@code PUT  /preference} : Update the preference.
     *
     * @param preferences the preference
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated preference,
     * or with status {@code 400 (Bad Request)} if the preference is not valid,
     * or with status {@code 500 (Internal Server Error)} if the preference couldn't be updated.
     */
    @PutMapping("/preference/{userId}")
    public ResponseEntity<Preferences> updatePreference(@Valid @RequestBody Preferences preferences, @PathVariable Long userId) {

        log.debug("REST request to update Preferences for user {} : {}", userId, preferences);

    	Preferences oldPrefs = preferenceUtil.getPrefs(userId);

        if(preferences.getAcmeHTTP01TimeoutMilliSec() < 100  || preferences.getAcmeHTTP01TimeoutMilliSec() > 60L * 1000L) {
	        log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", preferences.getAcmeHTTP01TimeoutMilliSec());
        	return ResponseEntity.badRequest().build();
        }


        int[] portsArr = preferences.getAcmeHTTP01CallbackPortArr();
        if( portsArr.length == 0 || portsArr.length > 10) {
	        log.warn("unexpected Preference number for ACME_HTTP01_CALLBACK_PORTS '{}'", portsArr.length);
        	return ResponseEntity.badRequest().body(oldPrefs);
        }

        String portsCommaSeparatedList = "";
        for( int nPort: portsArr){
            if(nPort == 0){
                // Just ignore a '0'
                continue;
            }
    		try {
    			if( nPort <= 0 || nPort > 65535) {
    		        log.warn("unexpected Preference value for port in ACME_HTTP01_CALLBACK_PORTS '{}'", nPort);
    	        	return ResponseEntity.badRequest().body(oldPrefs);
    			}
    		} catch(NumberFormatException nfe) {
    	        log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", nfe.getMessage());
            	return ResponseEntity.badRequest().body(oldPrefs);
    		}

    		if(!portsCommaSeparatedList.trim().isEmpty()){
                portsCommaSeparatedList += ",";
            }
            portsCommaSeparatedList += nPort;
        }

        List<UserPreference> upList = userPreferenceService.findAllForUserId(userId);

        Map<String, UserPreference> upMap = new HashMap<>();
        for(UserPreference up: upList) {
        	upMap.put(up.getName(), up);
        }

        List<AuditTrace> auditList = new ArrayList<>();

        updateValue(upMap, PreferenceUtil.CHECK_CRL, String.valueOf(preferences.isCheckCRL()), userId, auditList);
        updateValue(upMap, PreferenceUtil.NOTIFY_RA_ON_REQUEST, String.valueOf(preferences.isNotifyRAOnRequest()), userId, auditList);
        updateValue(upMap, PreferenceUtil.MAX_NEXT_UPDATE_PERIOD_CRL_SEC, String.valueOf(preferences.getMaxNextUpdatePeriodCRLHour() * 3600L), userId, auditList);
        updateValue(upMap, PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED, String.valueOf(preferences.isServerSideKeyCreationAllowed()), userId, auditList);

        updateValue(upMap, PreferenceUtil.SERVER_SIDE_KEY_DELETE_AFTER_DAYS, String.valueOf(preferences.getDeleteKeyAfterDays()), userId, auditList);
        updateValue(upMap, PreferenceUtil.SERVER_SIDE_KEY_DELETE_AFTER_USES, String.valueOf(preferences.getDeleteKeyAfterUses()), userId, auditList);

        updateValue(upMap, PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS, portsCommaSeparatedList, userId, auditList);
        updateValue(upMap, PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC, String.valueOf(preferences.getAcmeHTTP01TimeoutMilliSec()), userId, auditList);

        String[] selectedHashArr = preferences.getSelectedHashes();
        updateValue(upMap, PreferenceUtil.SELECTED_HASHES, String.join(",", selectedHashArr), userId, auditList);

        String[] selectedSigningAlgoArr = preferences.getSelectedSigningAlgos();
        updateValue(upMap, PreferenceUtil.SELECTED_SIGNING_ALGOS, String.join(",", selectedSigningAlgoArr), userId, auditList);

        updateValue(upMap, PreferenceUtil.AUTH_CLIENT_CERT, String.valueOf(preferences.isAuthClientCert()), userId, auditList);
        updateValue(upMap, PreferenceUtil.AUTH_TOTP, String.valueOf(preferences.isAuthTotp()), userId, auditList);
        updateValue(upMap, PreferenceUtil.AUTH_EMAIL, String.valueOf(preferences.isAuthEmail()), userId, auditList);
        updateValue(upMap, PreferenceUtil.AUTH_SMS, String.valueOf(preferences.isSms()), userId, auditList);

        updateValue(upMap, PreferenceUtil.INFO_MSG, preferences.getInfoMsg(), userId, auditList);

        auditService.saveAuditTrace(auditList);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userId.toString()))
            .body(preferences);
    }



	private void updateValue(Map<String, UserPreference> upMap,
                             String key, String value,
                             Long userId,
                             List<AuditTrace> auditList) {

        if( upMap.containsKey(key)) {
        	UserPreference up = upMap.get(key);
    		if( !value.equalsIgnoreCase(up.getContent().trim())) {
    	        log.debug("New preferences value '{}' != current value '{}'", value, up.getContent().trim());
                auditList.add( auditService.createAuditTraceSystemPreferenceUpdated(key,
                        up.getContent(), value));

                up.setContent(value);
    			userPreferenceService.save(up);
            }
        }else {
	        log.debug("Ceating new preferences for key '{}' and value '{}'", key, value);
            auditList.add( auditService.createAuditTraceSystemPreferenceCreated(key,value));
        	UserPreference up = new UserPreference();
        	up.setUserId(userId);
        	up.setName(key);
        	up.setContent(value);
    		userPreferenceService.save(up);
        }
	}

}
