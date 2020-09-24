package de.trustable.ca3s.core.web.rest;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

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
import io.github.jhipster.web.util.HeaderUtil;

/**
 * REST controller for reading {@link de.trustable.ca3s.core.domain.Certificate} using the convenient CertificateView object.
 * Just read-only access to this resource.
 * 
 */
@RestController
@RequestMapping("/api/admin")
public class PreferenceResource {

	private final Logger log = LoggerFactory.getLogger(PreferenceResource.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private static final String ENTITY_NAME = "preference";

    private final UserPreferenceService userPreferenceService;

    public PreferenceResource(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }


    
    /**
     * {@code GET  /preference/:id} : get the "id" certificate.
     *
     * @param userId the id of the preference to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the certificate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/preference/{userId}")
    public ResponseEntity<Preferences> getPreference(@PathVariable Long userId) {
    	
    	Preferences prefs = getPrefs(userId);
        
   		return new ResponseEntity<Preferences>(prefs, HttpStatus.OK);
    }



	private Preferences getPrefs(Long userId) {
		Preferences prefs = new Preferences();
    	
        log.debug("REST request to get Preference for user {}", userId);
        List<UserPreference> upList = userPreferenceService.findAllForUserId(userId);
        
        for(UserPreference up: upList) {
        	String name = up.getName();
        	if( PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED.equals(name)) {
        		prefs.setServerSideKeyCreationAllowed(Boolean.valueOf(up.getContent()));
        	} else if( PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS.equals(name)) {
        		prefs.setAcmeHTTP01CallbackPorts(up.getContent());
        	} else if( PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC.equals(name)) {
        		try {
        			prefs.setAcmeHTTP01TimeoutMilliSec(Long.parseLong(up.getContent()));
        		} catch(NumberFormatException nfe) {
        	        log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", nfe.getMessage());
        			prefs.setAcmeHTTP01TimeoutMilliSec(2000);
        		}
        	} else if( PreferenceUtil.CHECK_CRL.equals(name)) {
        		prefs.setCheckCRL(Boolean.valueOf(up.getContent()));
        	}
        }
		return prefs;
	}

    /**
     * {@code PUT  /preference} : Update the preference.
     *
     * @param preference the preference
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated preference,
     * or with status {@code 400 (Bad Request)} if the preference is not valid,
     * or with status {@code 500 (Internal Server Error)} if the preference couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/preference/{userId}")
    public ResponseEntity<Preferences> updatePreference(@Valid @RequestBody Preferences preferences, @PathVariable Long userId) throws URISyntaxException {
    	
        log.debug("REST request to update Preferences for user {} : {}", userId, preferences);

    	Preferences oldPrefs = getPrefs(userId);

        if(preferences.getAcmeHTTP01TimeoutMilliSec() < 100  || preferences.getAcmeHTTP01TimeoutMilliSec() > 60L * 1000L) {
	        log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", preferences.getAcmeHTTP01TimeoutMilliSec());
        	return ResponseEntity.badRequest().build();
        }
        
        String values = preferences.getAcmeHTTP01CallbackPorts();
        String[] portsArr = values.split(",");
        if( portsArr.length == 0 || portsArr.length > 10) {
	        log.warn("unexpected Preference value for ACME_HTTP01_CALLBACK_PORTS '{}'", values);
        	return ResponseEntity.badRequest().body(oldPrefs);
        }
        
        for( String port: portsArr){
    		try {
    			long nPort = Long.parseLong(port);
    			if( nPort <= 0 || nPort > 65535) {
    		        log.warn("unexpected Preference value for port in ACME_HTTP01_CALLBACK_PORTS '{}'", nPort);
    	        	return ResponseEntity.badRequest().body(oldPrefs);
    			}
    		} catch(NumberFormatException nfe) {
    	        log.warn("unexpected Preference value for ACME_HTTP01_TIMEOUT_MILLI_SEC '{}'", nfe.getMessage());
            	return ResponseEntity.badRequest().body(oldPrefs);
    		}
        }
        
        List<UserPreference> upList = userPreferenceService.findAllForUserId(userId);
        
        Map<String, UserPreference> upMap = new HashMap<String, UserPreference>();
        for(UserPreference up: upList) {
        	upMap.put(up.getName(), up);
        }
        
        updateValue(upMap, PreferenceUtil.CHECK_CRL, "" + preferences.isCheckCRL(), userId);
        updateValue(upMap, PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED, "" + preferences.isServerSideKeyCreationAllowed(), userId);
        updateValue(upMap, PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS, preferences.getAcmeHTTP01CallbackPorts().trim(), userId);
        updateValue(upMap, PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC, "" + preferences.getAcmeHTTP01TimeoutMilliSec(), userId);
        
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, userId.toString()))
            .body(preferences);
    }



	private void updateValue(Map<String, UserPreference> upMap, String key, String value, Long userId) {
		if( upMap.containsKey(key)) {
        	UserPreference up = upMap.get(key);
    		if( !value.equalsIgnoreCase(up.getContent().trim())) {
    	        log.debug("New preferences value '{}' != current value '{}'", value, up.getContent().trim());
    			up.setContent(value);
    			userPreferenceService.save(up);
    		}
        }else {
	        log.debug("Ceating new preferences for key '{}' and value '{}'", key, value);
        	UserPreference up = new UserPreference();
        	up.setUserId(userId);
        	up.setName(key);
        	up.setContent(value);
    		userPreferenceService.save(up);
        }
	}


}
