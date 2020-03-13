package de.trustable.ca3s.core.web.websocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import de.trustable.ca3s.acmeproxy.dto.AcmeRequestContainer;

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

    private Logger logger = LogManager.getLogger(RequestProxyStompSessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/acmeProxied", this);
        logger.info("Subscribed to /topic/acmeProxied");
        
//        session.send("/app/chat", getSampleMessage());
//        logger.info("Message sent to websocket server");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return AcmeRequestContainer.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
    	AcmeRequestContainer acr = (AcmeRequestContainer) payload;
        logger.info("Received : " + acr.getPath() );
    }

}