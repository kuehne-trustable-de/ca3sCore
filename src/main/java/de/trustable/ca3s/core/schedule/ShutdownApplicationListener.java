package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

@Component
public class ShutdownApplicationListener implements
  ApplicationListener<ContextStoppedEvent> {

	Logger LOG = LoggerFactory.getLogger(ShutdownApplicationListener.class);

    private final AuditService auditService;

    @Autowired
    public ShutdownApplicationListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override public void onApplicationEvent(ContextStoppedEvent event) {

        auditService.createAuditTraceStopped();

    }
}
