package de.trustable.ca3s.core.web.websocket;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import de.trustable.ca3s.core.domain.RequestProxyConfig;

@Service
public class WSConnectionPool {

    private static final Logger log = LoggerFactory.getLogger(WSConnectionPool.class);

	HashMap<Long, StompSession> connectionMap = new HashMap<Long, StompSession>();
	
	public boolean hasActiveConnection(RequestProxyConfig rpConfig) {

		if( connectionMap.containsKey(rpConfig.getId())){

			StompSession connection = connectionMap.get(rpConfig.getId());
			
			log.debug("websocket connection: isConnected {}", connection.isConnected() );

			if( connection.isConnected()) {
				log.debug("websocket connection to {} active", rpConfig.getRequestProxyUrl() );
				return true;
			}

			log.debug("websocket connection to {} inactive", rpConfig.getRequestProxyUrl() );

		}
		return false;
	}

	public void putConnection(RequestProxyConfig rpConfig, StompSession wsConnectionStatus) {
		
		if( hasActiveConnection(rpConfig)) {
			StompSession connection = connectionMap.get(rpConfig.getId());
			connection.disconnect();
			log.debug("cancelling existing websocket connection to {}", rpConfig.getRequestProxyUrl() );
		}
		connectionMap.put(rpConfig.getId(), wsConnectionStatus);
		log.debug("registered new websocket connection to {}", rpConfig.getRequestProxyUrl() );
		
	}

	public void ensureWSConnected(RequestProxyConfig rpConfig) {
		
		if( hasActiveConnection(rpConfig) ) {
			log.debug("stompClient already connected ...");
		} else {
			WebSocketClient client = new StandardWebSocketClient();
			 
			WebSocketStompClient stompClient = new WebSocketStompClient(client);
			stompClient.setMessageConverter(new MappingJackson2MessageConverter());
			 
			StompSessionHandler sessionHandler = new RequestProxyStompSessionHandler();
			ListenableFuture<StompSession> wsConnectionStatus = stompClient.connect(rpConfig.getRequestProxyUrl(), sessionHandler);
			
			try {
				putConnection(rpConfig, wsConnectionStatus.get());
				log.debug("stompClient frehly connected ...");
			} catch (InterruptedException | ExecutionException e) {
				log.warn("problem connecting stompClient", e);
			}
		}

	}

	public void ensureWSConnectionClosed(RequestProxyConfig rpConfig) {
		if( hasActiveConnection(rpConfig) ) {
			log.debug("stompClient connected, disconnecting ...");
			StompSession connection = connectionMap.get(rpConfig.getId());
			connection.disconnect();
			log.debug("cancelling existing websocket connection to {}", rpConfig.getRequestProxyUrl() );
			connectionMap.remove(rpConfig.getId());
		}
	}
}
