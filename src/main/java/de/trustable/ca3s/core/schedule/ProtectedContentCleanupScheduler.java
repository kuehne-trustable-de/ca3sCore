package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.repository.RequestProxyConfigRepository;
import de.trustable.ca3s.core.web.websocket.WSConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 *
 * @author kuehn
 *
 */
@Component
public class ProtectedContentCleanupScheduler {

	transient Logger LOG = LoggerFactory.getLogger(ProtectedContentCleanupScheduler.class);

	@Autowired
    ProtectedContentRepository protectedContentRepository;

	@Scheduled(fixedDelay = 60000)
	public void runMinute() {

        List<ProtectedContent> expiredList = protectedContentRepository.findByDeleteAfterPassed(Instant.now());
        if( !expiredList.isEmpty()) {
            LOG.info("deleting #{} expired ProtectedContent objects", expiredList.size());
            protectedContentRepository.deleteAll(expiredList);
        }
	}

}
