package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Handling notification
 */
@Service
public class NotificationService {

	private final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private CertificateRepository certificateRepo;

    @Autowired
    private CSRRepository csrRepo;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private AuditService auditService;


    @Transactional
    public int notifyRAOfficerHolderOnExpiry() throws MessagingException {

        Instant now = Instant.now();
        int nDays = 30;
        Instant after = now;
        Instant before = now.plus(nDays, ChronoUnit.DAYS);
        Instant relevantPendingStart = now.minus(nDays, ChronoUnit.DAYS);
        List<Certificate> expiringCertList = certificateRepo.findByValidTo(after, before);

        List<CSR> pendingCsrList = csrRepo.findPendingByDay(relevantPendingStart, now);

        if( expiringCertList.isEmpty() && pendingCsrList.isEmpty()) {
            LOG.info("No expiring certificates in the next {} days / no pending requests. No need to send a notificaton eMail to RA officers", nDays);
        }else {
            LOG.info("#{} expiring certificate in the next {} days, #{} pending requests", expiringCertList.size(), nDays, pendingCsrList.size());
            for( User raOfficer: findAllRAOfficer()) {
                Locale locale = Locale.forLanguageTag(raOfficer.getLangKey());
                Context context = new Context(locale);
                context.setVariable("expiringCertList", expiringCertList);
                context.setVariable("pendingCsrList", pendingCsrList);
                try {
                    mailService.sendEmailFromTemplate(context, raOfficer, "mail/pendingReqExpiringCertificateEmail", "email.allExpiringCertificate.subject");
                }catch (Throwable throwable){
                    LOG.warn("Problem occured while sending a notificaton eMail to RA officer address '" + raOfficer.getEmail() + "'", throwable);
                    auditService.saveAuditTrace(auditService.createAuditTraceExpiryNotificationfailed(raOfficer.getEmail()));
                }
            }
            auditService.saveAuditTrace(auditService.createAuditTraceExpiryNotificationSent(expiringCertList.size()));
        }

        return expiringCertList.size();
    }

    @Async
    public void notifyUserCerificateIssuedAsync(User requestor, Certificate cert ){

        try {
            notifyUserCerificateIssued(requestor, cert );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for issued cert", e);
        }
    }

    @Transactional
    public void notifyUserCerificateIssued(User requestor, Certificate cert ) throws MessagingException {

        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
        Context context = new Context(locale);
        context.setVariable("certId", cert.getId());
        context.setVariable("subject", cert.getSubject());
        context.setVariable("sans", cert.getSans());

        String downloadFilename = CertificateUtil.getDownloadFilename(cert);

        Instant requestedOn = cert.getValidFrom();
        boolean isServersideKeyGeneration = false;
        if(cert.getCsr() != null) {
            requestedOn = cert.getCsr().getRequestedOn();
            isServersideKeyGeneration = cert.getCsr().isServersideKeyGeneration();
        }
        context.setVariable("requestedOn", requestedOn);
        context.setVariable("isServersideKeyGeneration", isServersideKeyGeneration);

        context.setVariable("filenameCrt", downloadFilename + ".crt");
        context.setVariable("filenamePem", downloadFilename + ".pem");
        context.setVariable("filenameFullChainPem", downloadFilename + ".full.pem");

        mailService.sendEmailFromTemplate(context, requestor, "mail/acceptedRequestEmail", "email.acceptedRequest.title");
    }

    @Async
    public void notifyUserCerificateRejectedAsync(User requestor, CSR csr ){

        try {
            notifyUserCerificateRejected(requestor, csr );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for rejected request", e);
        }
    }

    @Transactional
    public void notifyUserCerificateRejected(User requestor, CSR csr ) throws MessagingException {

        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
        Context context = new Context(locale);
        context.setVariable("csr", csr);
        mailService.sendEmailFromTemplate(context, requestor, "mail/rejectedRequestEmail", "email.request.rejection.title");
    }


    @Async
    public void notifyUserCerificateRevokedAsync(User requestor, Certificate cert , CSR csr ){

        try {
            notifyUserCerificateRevoked(requestor, cert, csr );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for revoked certificate", e);
        }
    }

    @Transactional
    public void notifyUserCerificateRevoked(User requestor, Certificate cert, CSR csr ) throws MessagingException {
        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
        Context context = new Context(locale);
        context.setVariable("csr", csr);
        context.setVariable("cert", cert);
        String subject = cert.getSubject();
        if (subject == null) {
            subject = "";
        }
        String[] args = {subject, cert.getSerial(), cert.getIssuer()};
        mailService.sendEmailFromTemplate(context, requestor, "mail/revokedCertificateEmail", "email.revokedCertificate.title", args);
    }

    /**
     *
     * @return
     */
    private List<User> findAllRAOfficer(){

        List<User> raOfficerList = new ArrayList<User>();
        for( User user: userRepository.findAll()) {
            for( Authority auth: user.getAuthorities()) {
                LOG.debug("user {} {} has role {}", user.getFirstName(), user.getLastName(), auth.getName());
                if( AuthoritiesConstants.RA_OFFICER.equalsIgnoreCase(auth.getName()) || AuthoritiesConstants.DOMAIN_RA_OFFICER.equalsIgnoreCase(auth.getName())) {
                    raOfficerList.add(user);
                    LOG.debug("found user {} {} having the role of a RA officers", user.getFirstName(), user.getLastName());
                    break;
                }
            }
        }
        return raOfficerList;
    }

}
