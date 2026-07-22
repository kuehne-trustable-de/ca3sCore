package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.repository.CSRRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_CSR_DECISION_NOTIFICATION;
import static de.trustable.ca3s.core.domain.CsrAttribute.ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER;

@Service
public class CSRAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CSRAsyncUtil.class);

    private final CSRUtil csrUtil;
    private final CSRRepository csrRepository;

    public CSRAsyncUtil(CSRUtil csrUtil, CSRRepository csrRepository) {
        this.csrUtil = csrUtil;
        this.csrRepository = csrRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementNotificationCounter(CSR csr) {

        String counterString = csrUtil.getCSRAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER);
        if( counterString == null){
            return;
        }
        int retryCount = Integer.parseInt(counterString) + 1;
        csrUtil.setCsrAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER, "" + retryCount, false);

        csrRepository.save(csr);

        LOG.info("increment notification counter for cert #{} to {}", csr.getId(), retryCount);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteNotificationCounter(CSR csr) {

        csrUtil.deleteCsrAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION);
        csrUtil.deleteCsrAttribute(csr, ATTRIBUTE_CSR_DECISION_NOTIFICATION_COUNTER);
    }


}
