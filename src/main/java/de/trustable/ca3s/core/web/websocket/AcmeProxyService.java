package de.trustable.ca3s.core.web.websocket;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import de.trustable.ca3s.acmeproxy.dto.AcmeRequestContainer;
import de.trustable.ca3s.acmeproxy.dto.AcmeResponseContainer;

@Controller
public class AcmeProxyService implements ApplicationListener<SessionDisconnectEvent> {

    private static final Logger log = LoggerFactory.getLogger(AcmeProxyService.class);

    private final SimpMessageSendingOperations messagingTemplate;

    public AcmeProxyService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/topic/acmeProxied")
    @SendTo("/topic/acmeProxy")
    public AcmeResponseContainer sendActivity(@Payload AcmeRequestContainer arc, StompHeaderAccessor stompHeaderAccessor, Principal principal) {

        log.debug("received proxied call to '{}' for realm '{}'", arc.getPath(), arc.getRealm());

    	AcmeResponseContainer acmeResp = new AcmeResponseContainer(303);

        return acmeResp;
    }

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        log.debug("application event received : {}", event);
    }
}
