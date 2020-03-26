package de.trustable.ca3s.core.web.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import de.trustable.ca3s.acmeproxy.dto.AcmeRequestContainer;
import de.trustable.ca3s.acmeproxy.dto.AcmeResponseContainer;
import de.trustable.ca3s.acmeproxy.dto.ConnectInfo;

import java.lang.reflect.Type;

/**
 * This class is an implementation for <code>StompSessionHandlerAdapter</code>.
 * Once a connection is established, We subscribe to /topic/messages and 
 * send a sample message to server.
 * 
 * @author Kalyan
 *
 */
public class RequestProxyStompSessionHandler extends StompSessionHandlerAdapter {

    private static final String TOPIC_CONNECTED = "/topic/connected";
	private static final String TOPIC_ACME_PROXIED = "/topic/acmeProxied";
	private Logger logger = LogManager.getLogger(RequestProxyStompSessionHandler.class);

	@Override
	public void afterConnected(StompSession session, StompHeaders connectedHeaders) {

		logger.info("New session established : " + session.getSessionId());

		session.subscribe(TOPIC_ACME_PROXIED, this);
		logger.info("Subscribed to /topic/acmeProxied");

		session.subscribe(TOPIC_CONNECTED, this);
		logger.info("Subscribed to /topic/connected");

		session.send("/requestProxy/connect", "hello");
		logger.info("Connect message sent to request proxy");
	}

	@Override
	public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload,
			Throwable exception) {
		logger.error("Got an exception", exception);
	}

	@Override
	public Type getPayloadType(StompHeaders headers) {
		if( TOPIC_ACME_PROXIED.equals(headers.getDestination())) {
			return AcmeResponseContainer.class;
		}else if( TOPIC_CONNECTED.equals(headers.getDestination())) {
			return ConnectInfo.class;
		}
		
		return String.class;
	}

	@Override
	public void handleFrame(StompHeaders headers, Object payload) {

		logger.info("Received message for {} ", headers.getDestination());
		if (payload instanceof AcmeResponseContainer) {
			AcmeResponseContainer msg = (AcmeResponseContainer) payload;
			logger.info("Received : " + msg.getStatus() + " for : " + msg.getContent());
		} else if (payload instanceof ConnectInfo) {
			ConnectInfo msg = (ConnectInfo) payload;
			logger.info("Received connected response for : " + msg.getIp());
		} else {
			logger.info("Received payload class {}", payload.getClass().getName());
			logger.info("Received payload {}", payload.toString());
		}
	}

	/**
	 * A sample message instance.
	 * 
	 * @return instance of <code>Message</code>
	 */
	private AcmeRequestContainer getSampleMessage() {
		AcmeRequestContainer msg = new AcmeRequestContainer("path", "realm");
		return msg;
	}
}