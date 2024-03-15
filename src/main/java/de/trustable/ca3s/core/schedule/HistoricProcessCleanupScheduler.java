package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class HistoricProcessCleanupScheduler {

    transient Logger LOG = LoggerFactory.getLogger(HistoricProcessCleanupScheduler.class);

    private final BPMNUtil bpmnUtil;

    private final int historicProcessRetentionPeriodDays;

    public HistoricProcessCleanupScheduler(BPMNUtil bpmnUtil,
                                  @Value("${ca3s.historicProcess.retention.days:180}") int historicProcessRetentionPeriodDays) {
        this.bpmnUtil = bpmnUtil;
        this.historicProcessRetentionPeriodDays = historicProcessRetentionPeriodDays;
    }

    @Scheduled(cron="${ca3s.schedule.cron.dropHistoricProcessesCron: 0 22 22 * * ?}")
    public void retrieveUnrelatedUsers() {
        bpmnUtil.deleteHistoricProcesses( historicProcessRetentionPeriodDays);
    }

}
