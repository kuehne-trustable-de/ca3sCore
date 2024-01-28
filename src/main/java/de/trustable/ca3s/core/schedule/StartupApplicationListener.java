package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.service.util.BPMNUtil;

@Component
public class StartupApplicationListener implements
  ApplicationListener<ContextRefreshedEvent> {

	Logger LOG = LoggerFactory.getLogger(StartupApplicationListener.class);

    private final BPMNUtil bpmnUtil;
    private final AuditService auditService;

    private final TenantRepository tenantRepository;

    @Autowired
    public StartupApplicationListener(BPMNUtil bpmnUtil, AuditService auditService, TenantRepository tenantRepository) {
        this.bpmnUtil = bpmnUtil;
        this.auditService = auditService;
        this.tenantRepository = tenantRepository;
    }

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {

//        LOG.info("\n\n\n\n\n##############################################\n" +
//            "Basic application startup finished. Starting ca3s startup tasks"+
//            "\n##############################################\n\n");

        auditService.saveAuditTrace(auditService.createAuditTraceStarted());

        tenantRepository.findAll();

        bpmnUtil.updateProcessDefinitions();
    }
}
