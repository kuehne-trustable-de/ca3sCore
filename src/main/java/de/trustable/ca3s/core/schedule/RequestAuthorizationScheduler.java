package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class RequestAuthorizationScheduler {

	transient Logger LOG = LoggerFactory.getLogger(RequestAuthorizationScheduler.class);

	private final int maxRecordsPerTransaction;

	private final CSRRepository csrRepository;

    private final BPMNUtil bpmnUtil;


    public RequestAuthorizationScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                                         CSRRepository csrRepository,
                                         BPMNUtil bpmnUtil) {

        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
        this.csrRepository = csrRepository;
        this.bpmnUtil = bpmnUtil;
    }


    /**
     * try to invoke request authorization every minute
     */
    @Scheduled(fixedRateString="${ca3s.schedule.rate.csrAuthorization:60000}")
	public void authorizeCsr() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Page<CSR> authorizationPendingList = csrRepository.findByStatus(
            PageRequest.of(0, maxRecordsPerTransaction),
            CsrStatus.AUTHORIZING);

		int count = 0;
		for (CSR csr : authorizationPendingList) {

            bpmnUtil.startCertificateCreationProcess(csr);

			if( count++ > maxRecordsPerTransaction) {
				LOG.info("Csr authorization processing to {} per call", maxRecordsPerTransaction);
				break;
			}
		}
	}
}
