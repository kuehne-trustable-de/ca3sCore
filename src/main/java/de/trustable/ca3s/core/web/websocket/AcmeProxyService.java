package de.trustable.ca3s.core.web.websocket;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.trustable.ca3s.acmeproxy.dto.AcmeRequestContainer;
import de.trustable.ca3s.acmeproxy.dto.AcmeResponseContainer;



@Controller
public class AcmeProxyService implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger log = LoggerFactory.getLogger(AcmeProxyService.class);

    @MessageMapping("/acmeProxy")
    @SendTo("/topic/acmeProxied")
    public AcmeResponseContainer processACMERequest(@Payload AcmeRequestContainer arc, StompHeaderAccessor stompHeaderAccessor, Principal principal) {

        log.info("received proxied ACME call to '{}' (path '{}', realm '{}') with principal '{}'", stompHeaderAccessor.getDestination(), arc.getPath(), arc.getRealm(), principal);
   
    	AcmeResponseContainer acmeResp = new AcmeResponseContainer(303);
        log.info("returning ACME response '{}'", acmeResp.getStatus());

        return acmeResp;
    }

    @MessageExceptionHandler
    public String handleException(Throwable exception) {
        log.warn("received exception", exception);
        return exception.getMessage();
    }
    
    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        log.debug("application event received : {}", event);
    }
}
