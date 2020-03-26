package de.trustable.ca3s.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.repository.RequestProxyConfigRepository;
import de.trustable.ca3s.core.web.websocket.WSConnectionPool;

/**
 * 
 * @author kuehn
 *
 */
@Component
public class RequestProxyScheduler {

	transient Logger LOG = LoggerFactory.getLogger(RequestProxyScheduler.class);

	@Autowired
	RequestProxyConfigRepository requestProxyConfigRepo;

	@Autowired
	WSConnectionPool wsConnectionPool;
	
	@Value("${request.proxy.active:true}")
	private String requestProxyActive;

	@Scheduled(fixedDelay = 30000)
	public void runMinute() {

		if ("true".equalsIgnoreCase(requestProxyActive) ) {
			
			for (RequestProxyConfig rpConfig : requestProxyConfigRepo.findAll()) {
				LOG.debug("########## requestProxy check for " + rpConfig);
				if( rpConfig.isActive() ) {
					wsConnectionPool.ensureWSConnected(rpConfig);
				}else {
					wsConnectionPool.ensureWSConnectionClosed(rpConfig);
				}
			}
			LOG.debug("requestProxy connect 'Minute' finished");
		} else {
			LOG.debug("requestProxy connectdisabled");
		}
	}

}
