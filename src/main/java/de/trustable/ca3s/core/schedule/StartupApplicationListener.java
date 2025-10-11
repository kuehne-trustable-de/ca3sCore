package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements
  ApplicationListener<ContextRefreshedEvent> {

	Logger LOG = LoggerFactory.getLogger(StartupApplicationListener.class);

    private final SchemaUpdateScheduler schemaUpdateScheduler;
    private final AuditService auditService;

    @Autowired
    public StartupApplicationListener(SchemaUpdateScheduler schemaUpdateScheduler, AuditService auditService) {
        this.schemaUpdateScheduler = schemaUpdateScheduler;
        this.auditService = auditService;
    }

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {

//        LOG.info("\n\n\n\n\n##############################################\n" +
//            "Basic application startup finished. Starting ca3s startup tasks"+
//            "\n##############################################\n\n");

        auditService.saveAuditTrace(auditService.createAuditTraceStarted());

        schemaUpdateScheduler.updatePipeline();
    }
}
