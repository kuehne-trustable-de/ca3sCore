package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.util.*;

/**
 * Handling notification
 */
@Service
public class AsyncNotificationService {

	private static final Logger LOG = LoggerFactory.getLogger(AsyncNotificationService.class);

    private final NotificationService notificationService;

    public AsyncNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    public void notifyRAOfficerOnRequestAsync(CSR csr ){

        try {
            notificationService.notifyRAOfficerOnRequest(csr);
        } catch (Exception e) {
            LOG.error("problem sending ra officer notification", e);
        }
    }


    @Async
    public void notifyUserCertificateIssuedAsync(User requestor, Certificate cert, Set<String> additionalEmailSet ){

        try {
            notificationService.notifyUserCertificateIssued(requestor, cert, additionalEmailSet );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for issued cert", e);
        }
    }


    @Async
    public void notifyUserCertificateRejectedAsync(User requestor, CSR csr ){

        try {
            notificationService.notifyUserCertificateRejected(requestor, csr );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for rejected request", e);
        }
    }

    @Async
    public void notifyUserCertificateRevokedAsync(User requestor, Certificate cert , CSR csr ){

        try {
            notificationService.notifyCertificateRevoked(requestor, cert, csr );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for revoked certificate", e);
        }
    }

    @Async
    public void notifyRAOfficerOnUserRevocation(Certificate cert){
        notificationService.notifyRAOfficerOnUserRevocation( cert );
    }

}
