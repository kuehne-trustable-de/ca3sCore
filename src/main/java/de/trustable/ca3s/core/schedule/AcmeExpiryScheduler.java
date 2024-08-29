package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.AcmeNonce;
import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.enumeration.AcmeOrderStatus;
import de.trustable.ca3s.core.repository.AcmeNonceRepository;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 *
 * @author kuehn
 *
 */
@Component
public class AcmeExpiryScheduler {

	transient Logger LOG = LoggerFactory.getLogger(AcmeExpiryScheduler.class);

    final private int maxRecordsPerTransaction;

    final private AcmeOrderRepository acmeOrderRepository;

    final private AcmeNonceRepository acmeNonceRepository;

    final private AuditService auditService;

    public AcmeExpiryScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction, AcmeOrderRepository acmeOrderRepository, AcmeNonceRepository acmeNonceRepository, AuditService auditService) {
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeNonceRepository = acmeNonceRepository;
        this.auditService = auditService;
        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
    }

    @Scheduled(fixedRateString="${ca3s.schedule.rate.acmeOrderExpiry:600000}")
    public void runOrderCleanup() {

        Instant now = Instant.now();

        Page<AcmeOrder> acmeOrderExpiredList = acmeOrderRepository.findByPendingExpiryBefore(
            PageRequest.of(0, maxRecordsPerTransaction),now);

        if( !acmeOrderExpiredList.isEmpty()) {
            LOG.info("setting #{} expired orders to status 'invalid'", acmeOrderExpiredList.getNumberOfElements());
            for( AcmeOrder acmeOrder: acmeOrderExpiredList){
                acmeOrder.setStatus(AcmeOrderStatus.INVALID);
                auditService.createAuditTraceACMEOrderExpired(acmeOrder);
                acmeOrderRepository.save(acmeOrder);
            }
        }
    }

    @Scheduled(fixedRateString="${ca3s.schedule.rate.acmeOrderExpiry:600000}")
    public void runNonceCleanup() {

        Page<AcmeNonce> acmeOrderExpiredList = acmeNonceRepository.findByNonceExpiryDate(
            PageRequest.of(0, maxRecordsPerTransaction), Instant.now());

        if( !acmeOrderExpiredList.isEmpty()) {
            LOG.info("deleting #{} expired nonces", acmeOrderExpiredList.getNumberOfElements());
            acmeNonceRepository.deleteAll(acmeOrderExpiredList);
        }
    }


}
