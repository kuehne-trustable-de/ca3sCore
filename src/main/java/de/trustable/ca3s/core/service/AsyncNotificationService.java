package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.util.CSRUtil;
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
    private final UserRepository userRepository;
    private final CSRUtil csrUtil;

    public AsyncNotificationService(NotificationService notificationService, UserRepository userRepository, CSRUtil csrUtil) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
        this.csrUtil = csrUtil;
    }

    @Async
    public void notifyRAOfficerOnRequestAsync(CSR csr ){

        try {
            notificationService.notifyRAOfficerOnRequest(csr);
        } catch (Exception e) {
            LOG.error("problem sending ra officer notification", e);
        }
    }

    public void notifyInterestedPartiesByEMail(Certificate cert, CSR csr) {

        Set<String> additionalEmailSet = csrUtil.getAdditionalEmailRecipients(csr);

        Optional<User> optUser = userRepository.findOneByLogin(csr.getRequestedBy());
        if( optUser.isPresent()) {
            User requestor = optUser.get();
            if (requestor.getEmail() == null) {
                LOG.debug("Email doesn't exist for user '{}'", requestor.getLogin());
            }else {
                notifyUserCertificateIssuedAsync(requestor, cert, additionalEmailSet);
            }
        } else {
            LOG.warn("certificate requestor '{}' unknown!", csr.getRequestedBy());
        }
    }


    @Async
    public void notifyUserCertificateIssuedAsync(User requestor, Certificate cert, Set<String> additionalEmailSet ){

        try {
            notificationService.notifyUserCertificateIssued(requestor, cert, additionalEmailSet, true );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for issued cert", e);
        }
    }


    @Async
    public void notifyUserCertificateRejectedAsync(User requestor, CSR csr, Set<String> additionalEmailSet ){

        try {
            notificationService.notifyUserCertificateRejected(requestor, csr, additionalEmailSet, true );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for rejected request", e);
        }
    }

    @Async
    public void notifyUserCertificateRevokedAsync(User requestor, Certificate cert , CSR csr, Set<String> additionalEmailSet ){

        try {
            notificationService.notifyCertificateRevoked(requestor, cert, csr, additionalEmailSet, true );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for revoked certificate", e);
        }
    }

    @Async
    public void notifyRAOfficerOnUserRevocation(Certificate cert){
        notificationService.notifyRAOfficerOnUserRevocation( cert );
    }

}
