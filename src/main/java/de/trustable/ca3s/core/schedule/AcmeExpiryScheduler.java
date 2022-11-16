package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 *
 * @author kuehn
 *
 */
@Component
public class AcmeExpiryScheduler {

	transient Logger LOG = LoggerFactory.getLogger(AcmeExpiryScheduler.class);

    final private AcmeOrderRepository acmeOrderRepository;
    final private AuditService auditService;

    public AcmeExpiryScheduler(AcmeOrderRepository acmeOrderRepository, AuditService auditService) {
        this.acmeOrderRepository = acmeOrderRepository;
        this.auditService = auditService;
    }

    @Scheduled(fixedRateString="${ca3s.schedule.rate.acmeOrderExpiry:600000}")
	public void runMinute() {

        Instant now = Instant.now();

        List<AcmeOrder> acmeOrderExpiredList =  acmeOrderRepository.findByPendingExpiryBefore(now);
        if( !acmeOrderExpiredList.isEmpty()) {
            LOG.info("setting #{} expired orders to status 'invalid'", acmeOrderExpiredList.size());
            for( AcmeOrder acmeOrder: acmeOrderExpiredList){
                acmeOrder.setStatus(AcmeOrderStatus.INVALID);
                auditService.createAuditTraceACMEOrderExpired(acmeOrder);
                acmeOrderRepository.save(acmeOrder);
            }
        }
	}

}
