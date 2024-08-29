package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CSRUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author kuehn
 *
 */
@Component
public class ProtectedContentCleanupScheduler {

	transient Logger LOG = LoggerFactory.getLogger(ProtectedContentCleanupScheduler.class);

    private final int maxRecordsPerTransaction;

    final private ProtectedContentRepository protectedContentRepository;
    final private CSRRepository csrRepository;
    final private CSRUtil csrUtil;
    final private AuditService auditService;

    public ProtectedContentCleanupScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
        ProtectedContentRepository protectedContentRepository, CSRRepository csrRepository, CSRUtil csrUtil, AuditService auditService) {
        this.protectedContentRepository = protectedContentRepository;
        this.csrRepository = csrRepository;
        this.csrUtil = csrUtil;
        this.auditService = auditService;
        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
    }

    @Scheduled(fixedRateString="${ca3s.schedule.rate.protectedContentCleanup:600000}")
	public void runMinute() {

        Instant now = Instant.now();
        Page<ProtectedContent> invalidList = protectedContentRepository.findByValidToPassed( PageRequest.of(0, maxRecordsPerTransaction),now);
        for (ProtectedContent pc : invalidList){
            if(ContentRelationType.CSR.equals(pc.getRelationType())) {
                Optional<CSR> optCsr = csrRepository.findById(pc.getRelatedId());
                if (optCsr.isPresent()) {
                    CSR csr = optCsr.get();
                    if (CsrStatus.PENDING.equals(csr.getStatus())) {
                        // Invalidate request
                        csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, "created key for csr expired");
                        auditService.createAuditTraceCsrRejected(csr, "created key for csr expired");
                    }
                }
            }
        }

        Page<ProtectedContent> expiredList = protectedContentRepository.findByDeleteAfterPassed(PageRequest.of(0, maxRecordsPerTransaction),now);
        if( !expiredList.isEmpty()) {
            LOG.info("deleting #{} expired ProtectedContent objects", expiredList.getNumberOfElements());
            for (ProtectedContent pc : invalidList){
                LOG.debug("delete protected content due to passed deletion date: {}", pc);
            }
            protectedContentRepository.deleteAll(expiredList);
        }
	}

}
