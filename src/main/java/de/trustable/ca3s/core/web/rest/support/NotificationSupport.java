package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.AcmeOrderRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.NameAndRoleUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.util.*;

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
    private final AcmeOrderRepository acmeOrderRepository;

    public NotificationSupport(NameAndRoleUtil nameAndRoleUtil, UserRepository userRepository, NotificationService notificationService, CertificateRepository certificateRepository, CSRRepository csrRepository,
                               AcmeOrderRepository acmeOrderRepository) {
        this.nameAndRoleUtil = nameAndRoleUtil;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
        this.acmeOrderRepository = acmeOrderRepository;
    }


    /**
     * {@code POST  api/notification/sendAdminOnConnectorExpiry} : send out certificate and passphrase expiry summary.
     *
     * @return the number of expiring credentials.
     */
    @Transactional
    @PostMapping("notification/sendAdminOnConnectorExpiry")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int notifyAdminOnConnectorExpiry() throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User admin = optUser.get();
            return notificationService.notifyAdminOnConnectorExpiry(Collections.singletonList(admin),false);
        }

        return 0;
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
     * {@code POST  api/notification/sendExpiryPendingSummary} : send out certificate expiry and request pending summary.
     *
     * @return the number of expiring certificates .
     */
    @Transactional
    @PostMapping("notification/sendRequestorExpiry")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int notifyRequestorOnExpirySummary() throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User admin = optUser.get();
            return notificationService.notifyRequestorOnExpiry( admin, false);
        }

        return 0;
    }

    /**
     * {@code POST  api/notification/sendRequestorExpiry} : send out certificate expiry and request pending summary.
     *
     * @return the number of expiring certificates .
     */
    @Transactional
    @PostMapping("notification/sendRequestorExpiry/{certId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int notifyRequestorOnExpiry(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            User admin = optUser.get();
            return notificationService.notifyRequestorOnExpiry( admin, false, certId);
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
            notificationService.notifyUserCertificateIssued(requestor, cert, new HashSet<>());
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
            notificationService.notifyUserCertificateRejected(requestor, csr, Collections.emptySet());
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
            notificationService.notifyCertificateRevoked(requestor, cert, cert.getCsr(), Collections.emptySet());
        }
    }

    /**
     * {@code POST  api/notification/sendUserCertificateRevoked} : send out certificate revocation info.
     */
    @Transactional
    @PostMapping("notification/sendUserCertificateRevoked/{certId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyRAOfficerOnUserCerificateRevoked(@PathVariable String certId) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            Certificate cert = certificateRepository.getOne(Long.parseLong(certId));
            notificationService.notifyRAOfficerOnUserRevocation(cert);
        }
    }

    /**
     * {@code POST  api/notification/sendUserCertificateRevoked} : send out certificate revocation info.
     */
    @Transactional
    @PostMapping("notification/sendAcmeContactOnProblem/{orderId}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public void notifyAcmeContactOnProblem(@PathVariable String orderId,
                                           @RequestBody ProblemDetail problemDetail) throws MessagingException {

        Optional<User> optUser = userRepository.findOneByLogin(nameAndRoleUtil.getNameAndRole().getName());
        if (optUser.isPresent()) {
            Optional<AcmeOrder> orderOptional = acmeOrderRepository.findById(Long.parseLong(orderId));
            if(optUser.isPresent() && orderOptional.isPresent()){
                Set<String> addressSet = Collections.singleton(optUser.get().getEmail());
                notificationService.notifyAccountHolderOnACMEProblem(
                    orderOptional.get(),
                    problemDetail,
                    addressSet );
            }

        }
    }

}
