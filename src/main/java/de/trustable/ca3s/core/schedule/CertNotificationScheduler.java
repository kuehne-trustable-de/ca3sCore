package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.CRLUpdateInfo;
import de.trustable.ca3s.core.service.util.*;
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

import javax.mail.MessagingException;
import javax.naming.NamingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION;
import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CertNotificationScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertNotificationScheduler.class);

	private final int maxRecordsPerTransaction;

	private final CertificateRepository certificateRepo;

    private final BPMNAsyncUtil bpmnAsyncUtil;


    public CertNotificationScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                                     CertificateRepository certificateRepo,
                                     BPMNAsyncUtil bpmnAsyncUtil) {

        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
        this.certificateRepo = certificateRepo;
        this.bpmnAsyncUtil = bpmnAsyncUtil;
    }


    /**
     * try to invoke certificate notification every hour
     */
    @Scheduled(fixedRateString="${ca3s.schedule.rate.certNotification:3600000}")
	public void retrieveCertificates() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Page<Certificate> notificationPendingList = certificateRepo.findByAttribute(
            PageRequest.of(0, maxRecordsPerTransaction),
            ATTRIBUTE_CERTIFICATE_NOTIFICATION);

		int count = 0;
		for (Certificate certificate : notificationPendingList) {

            bpmnAsyncUtil.processChange( certificate, auth);
			LOG.info("processed notification for certificate {} ", certificate.getId());

			if( count++ > maxRecordsPerTransaction) {
				LOG.info("limited certificate notification processing to {} per call", maxRecordsPerTransaction);
				break;
			}
		}
	}
}
