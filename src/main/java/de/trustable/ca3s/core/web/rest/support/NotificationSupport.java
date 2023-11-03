package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.util.NameAndRoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for test sending of notification.
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
    @Transactional
    @PostMapping("notification/sendExpiryPendingSummary")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int notifyRAOfficerHolderOnExpiry() throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User admin = optUser.get();

            List<User> raOfficerList = new ArrayList<>();
            raOfficerList.add(admin);

            List<User> domainOfficerList = new ArrayList<>();

            return notificationService.notifyRAOfficerHolderOnExpiry(raOfficerList, domainOfficerList, false);
        }

        return 0;
    }

    /**
     * {@code POST  api/notification/sendRAOfficerOnRequest} : send out notification on new request to assigned RA.
     *
     */
    @Transactional
    @PostMapping("notification/sendRAOfficerOnRequest/{csrId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyRAOfficerOnRequest(@PathVariable String csrId) {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User admin = optUser.get();

            List<User> raOfficerList = new ArrayList<>();
            raOfficerList.add(admin);

            List<User> domainOfficerList = new ArrayList<>();

            CSR csr = csrRepository.getOne(Long.parseLong(csrId));
            notificationService.notifyRAOfficerOnRequest(csr, raOfficerList, domainOfficerList, false);
        }
    }

    /**
     * {@code POST  api/notification/sendUserCertificateIssued} : send out certificate issuance info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateIssued/{certId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyUserCertificateIssued(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User requestor = optUser.get();

            Certificate cert = certificateRepository.getOne(Long.parseLong(certId));
            notificationService.notifyUserCerificateIssued(requestor, cert, new HashSet<>());
        }
    }


    /**
     * {@code POST  api/notification/sendUserCertificateRejected} : send out certificate request rejection info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateRejected/{csrId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyUserCertificateRejected(@PathVariable String csrId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User requestor = optUser.get();

            CSR csr = csrRepository.getOne(Long.parseLong(csrId));
            notificationService.notifyUserCerificateRejected(requestor, csr);
        }
    }

    /**
     * {@code POST  api/notification/sendCertificateRevoked} : send out certificate revocation info.
     */
    @Transactional
    @PostMapping("notification/sendCertificateRevoked/{certId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyCerificateRevoked(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User requestor = optUser.get();
            Certificate cert = certificateRepository.getOne(Long.parseLong(certId));
            notificationService.notifyCerificateRevoked(requestor, cert, cert.getCsr());
        }
    }

    /**
     * {@code POST  api/notification/sendUserCertificateRevoked} : send out certificate revocation info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateRevoked/{certId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyUserCerificateRevoked(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            Certificate cert = certificateRepository.getOne(Long.parseLong(certId));
            notificationService.notifyRAOfficerOnUserRevocation(cert);
        }
    }

}
