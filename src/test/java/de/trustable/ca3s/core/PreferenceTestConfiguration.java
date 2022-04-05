package de.trustable.ca3s.core;

import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.service.dto.Preferences;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.util.SocketUtils;

import java.util.Optional;

import static de.trustable.ca3s.core.service.util.PreferenceUtil.SYSTEM_PREFERENCE_ID;

@Service
public class PreferenceTestConfiguration {

	public static final Logger LOGGER = LogManager.getLogger(PreferenceTestConfiguration.class);

	@Autowired
    UserPreferenceRepository upRepo;

    @Autowired
    PreferenceUtil prefUtil;

    int freePort = SocketUtils.findAvailableTcpPort(45000);
    int dnsPort = SocketUtils.findAvailableTcpPort(45000);

	@Bean
    public Preferences getTestUserPreference() {

        return getTestUserPreference(freePort);
    }

    public Preferences getTestUserPreference(final int primaryPort) {

        initTestUserPreference(PreferenceUtil.ACME_HTTP01_CALLBACK_PORTS, primaryPort + ",5544,8080", SYSTEM_PREFERENCE_ID);
        initTestUserPreference(PreferenceUtil.ACME_HTTP01_TIMEOUT_MILLI_SEC, "1000", SYSTEM_PREFERENCE_ID);
        initTestUserPreference(PreferenceUtil.CHECK_CRL, "false", SYSTEM_PREFERENCE_ID);
        initTestUserPreference(PreferenceUtil.NOTIFY_RA_ON_REQUEST, "false", SYSTEM_PREFERENCE_ID);
        initTestUserPreference(PreferenceUtil.SERVER_SIDE_KEY_CREATION_ALLOWED, "true", SYSTEM_PREFERENCE_ID);

        initTestUserPreference(PreferenceUtil.SELECTED_HASHES, "sha-1,sha-256,sha-512", SYSTEM_PREFERENCE_ID);
        initTestUserPreference(PreferenceUtil.SELECTED_SIGNING_ALGOS, "rsa-2048,rsa-3072,rsa-4096", SYSTEM_PREFERENCE_ID);

        return prefUtil.getPrefs(SYSTEM_PREFERENCE_ID);

    }

    public int getFreePort(){
        return freePort;
    }

    public int getDNSPort(){
        return dnsPort;
    }

    void initTestUserPreference(final String topicName, final String value, final Long userId) {

        UserPreference newUP;
        Optional<UserPreference> existingUCOpt = upRepo.findByNameforUser(topicName, userId);
        if( existingUCOpt.isPresent() ) {
            LOGGER.debug("UserPreference '{}' for user '{}' already present", topicName, userId);
            newUP = existingUCOpt.get();
        }else {
            newUP = new UserPreference();
        }

        newUP.setUserId(userId);
        newUP.setName(topicName);
        newUP.setContent(value);

        upRepo.save(newUP);
        LOGGER.debug("UserPreference '{}' with '{}' for user '{}' created", topicName, value, userId);
    }
}
