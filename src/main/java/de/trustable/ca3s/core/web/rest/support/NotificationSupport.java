package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.util.NameAndRoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.Optional;

/**
 * REST controller for processing PKCS10 requests and Certificates.
 */
@RestController
@RequestMapping("/api")
public class NotificationSupport {

    private final Logger LOG = LoggerFactory.getLogger(NotificationSupport.class);

    private final NameAndRoleUtil nameAndRoleUtil;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final CertificateRepository certificateRepository;

    private final CSRRepository csrRepository;

    public NotificationSupport(NameAndRoleUtil nameAndRoleUtil, UserRepository userRepository, NotificationService notificationService, CertificateRepository certificateRepository, CSRRepository csrRepository) {
        this.nameAndRoleUtil = nameAndRoleUtil;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
    }


    /**
     * {@code POST  api/notification/sendExpiryPendingSummary} : send out certificate expiry and request pending summary.
     *
     * @return the number of expiring certificates .
     */
    @PostMapping("notification/sendExpiryPendingSummary")
    public int notifyRAOfficerHolderOnExpiry() throws MessagingException {

        return notificationService.notifyRAOfficerHolderOnExpiry();
    }

    /**
     * {@code POST  api/notification/sendUserCertificateIssued} : send out certificate issuance info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateIssued/{certId}")
    public void notifyUserCertificateIssued(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User requestor = optUser.get();

            Certificate cert = certificateRepository.getOne(Long.parseLong(certId));
            notificationService.notifyUserCerificateIssued(requestor, cert);
        }
    }

    /**
     * {@code POST  api/notification/sendUserCertificateRejected} : send out certificate request rejection info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateRejected/{csrId}")
    public void notifyUserCertificateRejected(@PathVariable String csrId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User requestor = optUser.get();

            CSR csr = csrRepository.getOne(Long.parseLong(csrId));
            notificationService.notifyUserCerificateRejected(requestor, csr);
        }
    }

}
