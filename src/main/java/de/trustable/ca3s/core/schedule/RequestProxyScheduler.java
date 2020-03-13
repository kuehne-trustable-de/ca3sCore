package de.trustable.ca3s.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.repository.RequestProxyConfigRepository;
import de.trustable.ca3s.core.web.websocket.RequestProxyStompSessionHandler;

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


	@Value("${request.proxy.active:true}")
	private String requestProxyActive;

	@Scheduled(fixedDelay = 30000)
	public void runMinute() {

		if ("true".equalsIgnoreCase(requestProxyActive) ) {
			
			for (RequestProxyConfig rpConfig : requestProxyConfigRepo.findAll()) {
				LOG.debug("########## requestProxy check for " + rpConfig);
				if( rpConfig.isActive() ) {
					connect(rpConfig);
				}
			}
			LOG.debug("requestProxy connect 'Minute' finished");
		} else {
			LOG.debug("requestProxy connectdisabled");
		}
	}

	private void connect(RequestProxyConfig rpConfig) {
		WebSocketClient client = new StandardWebSocketClient();
		 
		WebSocketStompClient stompClient = new WebSocketStompClient(client);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
		 
		StompSessionHandler sessionHandler = new RequestProxyStompSessionHandler();
		stompClient.connect(rpConfig.getRequestProxyUrl(), sessionHandler);
		LOG.debug("stompClient.connect ...");
	}
}
