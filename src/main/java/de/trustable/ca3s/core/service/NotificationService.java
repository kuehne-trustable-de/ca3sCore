package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.PipelineUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Handling notification
 */
@Service
public class NotificationService {

	private final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private final CertificateRepository certificateRepo;
    private final CSRRepository csrRepo;
    private final UserRepository userRepository;
    private final PipelineUtil pipelineUtil;
    private final CertificateUtil certificateUtil;
    private final MailService mailService;
    private final AuditService auditService;
    private final int nDaysExpiryEE;
    private final int nDaysExpiryCA;
    private final int nDaysPending;
    private final List<Integer> notificationDayList;
    private final List<String> notificationARAAttributes;
    private final boolean notifyUserOnly;

    private final boolean notifyRAOfficerHolderOnExpiry;
    private final boolean notifyRequestorOnExpiry;
    private final boolean notifyCerificateRevoked;
    private final boolean notifyUserCerificateRejected;
    private final boolean notifyUserCerificateIssued;
    private final boolean notifyRAOfficerOnRequest;
    private final boolean notifyRAOfficerOnUserRevocation;
    private final boolean notifyRequestorOnExcessiveAvtiveCertificates;




    @Autowired
    public NotificationService(CertificateRepository certificateRepo, CSRRepository csrRepo,
                               UserRepository userRepository, PipelineUtil pipelineUtil,
                               CertificateUtil certificateUtil,
                               MailService mailService,
                               AuditService auditService,
                               @Value("${ca3s.schedule.ra-officer-notification.days-before-expiry.ee:30}") int nDaysExpiryEE,
                               @Value("${ca3s.schedule.ra-officer-notification.days-before-expiry.ca:90}")int nDaysExpiryCA,
                               @Value("${ca3s.schedule.ra-officer-notification.days-pending:30}") int nDaysPending,
                               @Value("${ca3s.schedule.requestor.notification.days:30,14,7,6,5,4,3,2,1}") String notificationDays,
                               @Value("${ca3s.schedule.requestor.notification.attributes:}") String notificationARAAttributesString,
                               @Value("${ca3s.schedule.requestor.notification.user-only:false}") boolean notifyUserOnly,
                               @Value("${ca3s.notify.raOfficerHolderOnExpiry:true}") boolean notifyRAOfficerHolderOnExpiry,
                               @Value("${ca3s.notify.requestorOnExpiry:true}") boolean notifyRequestorOnExpiry,
                               @Value("${ca3s.notify.cerificateRevoked:true}") boolean notifyCerificateRevoked,
                               @Value("${ca3s.notify.userCerificateRejected:true}") boolean notifyUserCerificateRejected,
                               @Value("${ca3s.notify.userCerificateIssued:true}") boolean notifyUserCerificateIssued,
                               @Value("${ca3s.notify.raOfficerOnRequest:true}") boolean notifyRAOfficerOnRequest,
                               @Value("${ca3s.notify.raOfficerOnUserRevocation:true}") boolean notifyRAOfficerOnUserRevocation,
                               @Value("${ca3s.notify.requestorOnExcessiveAvtiveCertificates:true}") boolean notifyRequestorOnExcessiveAvtiveCertificates) {
        this.certificateRepo = certificateRepo;
        this.csrRepo = csrRepo;
        this.userRepository = userRepository;
        this.pipelineUtil = pipelineUtil;
        this.certificateUtil = certificateUtil;
        this.mailService = mailService;
        this.auditService = auditService;
        this.nDaysExpiryEE = nDaysExpiryEE;
        this.nDaysExpiryCA = nDaysExpiryCA;
        this.nDaysPending = nDaysPending;
        this.notifyUserOnly = notifyUserOnly;
        this.notifyRAOfficerHolderOnExpiry = notifyRAOfficerHolderOnExpiry;
        this.notifyRequestorOnExpiry = notifyRequestorOnExpiry;
        this.notifyCerificateRevoked = notifyCerificateRevoked;
        this.notifyUserCerificateRejected = notifyUserCerificateRejected;
        this.notifyUserCerificateIssued = notifyUserCerificateIssued;
        this.notifyRAOfficerOnRequest = notifyRAOfficerOnRequest;
        this.notifyRAOfficerOnUserRevocation = notifyRAOfficerOnUserRevocation;
        this.notifyRequestorOnExcessiveAvtiveCertificates = notifyRequestorOnExcessiveAvtiveCertificates;

        this.notificationDayList = new ArrayList<>();
        String[] parts = notificationDays.split(",");
        for( String part: parts){
            try {
                notificationDayList.add(Integer.parseInt(part));
            }catch(NumberFormatException nfe){
                LOG.info("Unexpected value '{}' in 'ca3s.schedule.requestor.notification.days': {}", part, nfe.getMessage());
            }
        }

        String[] araParts = notificationARAAttributesString.split(",");
        notificationARAAttributes = Arrays.asList(araParts);

    }


    @Transactional
    public int notifyRAOfficerHolderOnExpiry() throws MessagingException {
        return notifyRAOfficerHolderOnExpiry(findAllRAOfficer(AuthoritiesConstants.RA_OFFICER),
            findAllRAOfficer(AuthoritiesConstants.DOMAIN_RA_OFFICER),
            true);
    }

    @Transactional
    public int notifyRAOfficerHolderOnExpiry(List<User> raOfficerList, List<User> domainOfficerList, boolean logNotification) {

        if( !notifyRAOfficerHolderOnExpiry){
            LOG.info("notifyRAOfficerHolderOnExpiry deactivated");
            return 0;
        }

        Instant now = Instant.now();
        Instant beforeCA = now.plus(nDaysExpiryCA, ChronoUnit.DAYS);
        List<Certificate> expiringCAList = certificateRepo.findNonRevokedByTypeAndValidTo(false, now, beforeCA);

        Instant beforeEE = now.plus(nDaysExpiryEE, ChronoUnit.DAYS);
        List<Certificate> expiringEECertList = certificateRepo.findNonRevokedByTypeAndValidTo(true, now, beforeEE);

        Instant relevantPendingStart = now.minus(nDaysPending, ChronoUnit.DAYS);
        List<CSR> pendingCsrList = csrRepo.findPendingByDay(relevantPendingStart, now);

        if( expiringEECertList.isEmpty() && pendingCsrList.isEmpty()) {
            LOG.info("No expiring certificates in the next {} days / no pending requests requested in the last {} days. No need to send a notification eMail to RA officers", nDaysExpiryEE, nDaysPending);
        }else {
            LOG.info("#{} expiring certificate in the next {} days, #{} pending requests issued in the last {} days.", expiringEECertList.size(), nDaysExpiryEE, pendingCsrList.size(), nDaysPending);

            // Process all CSRs for RA officers
            for( User raOfficer: raOfficerList) {
                Locale locale = Locale.forLanguageTag(raOfficer.getLangKey());
                Context context = new Context(locale);
                context.setVariable("expiringCAList", expiringCAList);
                context.setVariable("expiringEECertList", expiringEECertList);
                context.setVariable("pendingCsrList", pendingCsrList);
                context.setVariable("nDaysPending", nDaysPending);
                context.setVariable("nDaysExpiryEE", nDaysExpiryEE);
                context.setVariable("nDaysExpiryCA", nDaysExpiryCA);
                try {
                    mailService.sendEmailFromTemplate(context, raOfficer, null, "mail/pendingReqExpiringCertificateEmail", "email.allExpiringCertificate.subject");
                }catch (Throwable throwable){
                    LOG.warn("Problem occurred while sending a notification eMail to RA officer address '" + raOfficer.getEmail() + "'", throwable);
                    if(logNotification) {
                        auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(raOfficer.getEmail()));
                    }
                }
            }

            // Process subset of CSRs for domain officers
            for( User domainOfficer: domainOfficerList) {

                List<CSR> pendingDomainCsrList = new ArrayList<>();
                for( CSR csr: pendingCsrList){
                    if( pipelineUtil.isUserValidAsRA(csr.getPipeline(), domainOfficer) ){
                        pendingDomainCsrList.add(csr);
                    }
                }

                Locale locale = Locale.forLanguageTag(domainOfficer.getLangKey());
                Context context = new Context(locale);
                context.setVariable("expiringCAList", expiringCAList);
                context.setVariable("expiringEECertList", expiringEECertList);
                context.setVariable("pendingCsrList", pendingCsrList);
                context.setVariable("nDaysPending", nDaysPending);
                context.setVariable("nDaysExpiryEE", nDaysExpiryEE);
                context.setVariable("nDaysExpiryCA", nDaysExpiryCA);
                try {
                    mailService.sendEmailFromTemplate(context, domainOfficer, null, "mail/pendingReqExpiringCertificateEmail", "email.allExpiringCertificate.subject");
                }catch (Throwable throwable){
                    LOG.warn("Problem occurred while sending a notification eMail to RA officer address '" + domainOfficer.getEmail() + "'", throwable);
                    if(logNotification) {
                        auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(domainOfficer.getEmail()));
                    }
                }
            }
            if(logNotification) {
                auditService.saveAuditTrace(auditService.createAuditTraceExpiryNotificationSent(expiringEECertList.size()));
            }
        }

        return expiringEECertList.size();
    }

    @Transactional
    public int notifyRequestorOnExpiry(User testUser, boolean logNotification) {

        if (!notifyRequestorOnExpiry) {
            LOG.info("notifyRequestorOnExpiry deactivated");
            return 0;
        }

        Instant now = Instant.now();

        int maxExpiry = notificationDayList.stream().max(Integer::compareTo).get();
        Instant beforeEE = now.plus(maxExpiry, ChronoUnit.DAYS);
        List<Certificate> expiringEECertList = certificateRepo.findNonRevokedByTypeAndValidTo(true, now, beforeEE);

        return notifyRequestorOnExpiry(testUser, logNotification,
            expiringEECertList,
            maxExpiry,
            false);
    }

    @Transactional
    public int notifyRequestorOnExpiry(User testUser, boolean logNotification, final String certId) {

        Optional<Certificate> optionalCertificate = certificateRepo.findById(Long.parseLong(certId));
        if( optionalCertificate.isPresent()) {

            return notifyRequestorOnExpiry(testUser, logNotification,
                Collections.singletonList(optionalCertificate.get()),
                9999,
                true);
        }
        return 0;
    }

    private int notifyRequestorOnExpiry(User testUser, boolean logNotification,
                                       List<Certificate> expiringEECertList,
                                       int maxExpiry,
                                       boolean forceSendAnyday) {

        Instant now = Instant.now();

        if( expiringEECertList.isEmpty()) {
            LOG.info("No expiring certificates in the next {} days / no pending requests requested in the last {} days. No need to send a notification eMail to RA officers", nDaysExpiryEE, nDaysPending);
        }else {
            LOG.info("#{} expiring certificate in the next {} days.", expiringEECertList.size(), maxExpiry);

            Map<User, List<Certificate>> certListGroupedByUser = new HashMap<>();
            for( Certificate cert: expiringEECertList){
                // check for relevant expiry time slots
                int diffDays = (int)ChronoUnit.DAYS.between(now, cert.getValidTo());
                if( notificationDayList.contains(diffDays) || forceSendAnyday){
                    LOG.debug("#{} days until expiry are in the list of notification days.", diffDays);
                }else{
                    LOG.debug("#{} days until expiry are NOT in the list of notification days.", diffDays);
                    continue;
                }

                if( cert.getCsr() != null &&
                    cert.getCsr().getRequestedBy() != null &&
                    cert.getCsr().getPipeline() != null ){

                    if(PipelineType.WEB.equals( cert.getCsr().getPipeline().getType())){
                        LOG.debug("Web Pipelines will be processed for notification.");
                    }else{
                        LOG.debug("Non-Web Pipelines will be ignored for notification.");
                        continue;
                    }

                    Optional<User> optionalUser = userRepository.findOneByLogin(cert.getCsr().getRequestedBy());
                    if(optionalUser.isPresent()){
                        User user = testUser;
                        if( user == null) {
                            user = optionalUser.get();
                        }
                        if( certListGroupedByUser.containsKey(user)){
                            certListGroupedByUser.get(user).add(cert);
                        }else{
                            List<Certificate> certificateList = new ArrayList<>();
                            certificateList.add(cert);
                            certListGroupedByUser.put(user, certificateList);
                        }
                    }
                }
            }

            for( User requestor: certListGroupedByUser.keySet()) {

                LOG.info("#{} expiring certificates for requestor {}.", certListGroupedByUser.get(requestor).size(), requestor.getId());

                Locale locale = Locale.forLanguageTag(requestor.getLangKey());
                Context context = new Context(locale);
                context.setVariable("now", now);
                context.setVariable("user", requestor);

                if(notifyUserOnly) {
                    sentExpiryNotificationUserOnly(logNotification, certListGroupedByUser, requestor, context);
                }else{
                    sentExpiryNotificationAllParticipants(logNotification, certListGroupedByUser, requestor, context);
                }
            }

            if(logNotification) {
                auditService.saveAuditTrace(auditService.createAuditTraceExpiryNotificationSent(expiringEECertList.size()));
            }
        }

        return expiringEECertList.size();
    }

    private void sentExpiryNotificationUserOnly(boolean logNotification, Map<User, List<Certificate>> certListGroupedByUser, User requestor, Context context) {

        context.setVariable("expiringCertList", certListGroupedByUser.get(requestor));
        try {
            mailService.sendEmailFromTemplate(context, requestor, null, "mail/expiringUserCertificateEmail", "email.allExpiringCertificate.subject");
        } catch (Throwable throwable) {
            LOG.warn("Problem occurred while sending a notification eMail to requestor address '" + requestor.getEmail() + "'", throwable);
            if (logNotification) {
                auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(requestor.getEmail()));
            }
        }
    }

    private void sentExpiryNotificationAllParticipants(boolean logNotification, Map<User, List<Certificate>> certListGroupedByUser, User requestor, Context context) {

        for( Certificate cert : certListGroupedByUser.get(requestor)) {
            LOG.info("sending notification for expiring certificate {} to requestor {}.", cert.getId(), requestor.getId());

            List<String> ccList = new ArrayList<>();
            if( cert.getCsr() != null &&
                cert.getCsr().getPipeline() != null ){
                Pipeline pipeline = cert.getCsr().getPipeline();

                String additionalEmailRecipients = pipelineUtil.getPipelineAttribute(pipeline, PipelineUtil.ADDITIONAL_EMAIL_RECIPIENTS, "");
                addSplittedString(ccList, additionalEmailRecipients);

                ccList.addAll(findAdditionalRecipients(cert));
/*
                for( String araAttribute: notificationARAAttributes) {
                    String emailAttribute = certificateUtil.getCertAttribute(cert, CsrAttribute.ARA_PREFIX + araAttribute, "");
                    addSplittedString(ccList, emailAttribute);
                }
 */
            }

            context.setVariable("expiringCertList", Collections.singletonList(cert));
            try {
                mailService.sendEmailFromTemplate(context, requestor, ccList.toArray(new String[0]), "mail/expiringUserCertificateEmail", "email.allExpiringCertificate.subject");
            } catch (Throwable throwable) {
                LOG.warn("Problem occurred while sending a notification eMail to requestor address '" + requestor.getEmail() + "'", throwable);
                if (logNotification) {
                    auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(requestor.getEmail()));
                }
            }
        }
    }

    private void addSplittedString(List<String> ccList, String additionalEmailRecipients) {
        if( !additionalEmailRecipients.isEmpty()) {
            String[] parts = additionalEmailRecipients.split(", ");
            LOG.debug("#{} parts selected from additionalEmailRecipients '{}'.", parts.length, additionalEmailRecipients);
            ccList.addAll(Arrays.asList(parts));
        }
    }

    public void notifyRequestorOnExcessiveAvtiveCertificates(String requestorEmail, int numberActive, Certificate certificate) {

        if( !notifyRequestorOnExcessiveAvtiveCertificates){
            LOG.info("notifyRequestorOnExcessiveAvtiveCertificates deactivated");
            return;
        }

        Locale locale = Locale.getDefault();
        Context context = new Context(locale);
        context.setVariable("numberActive", numberActive);
        context.setVariable("certificate", certificate);
        try {
            mailService.sendEmailFromTemplate(context, null, requestorEmail, null, "mail/excessiveActiveCertificates", "email.excessive.active.title");
        }catch (Throwable throwable){
            LOG.warn("Problem occurred while sending a notification eMail to requestor address '" + requestorEmail + "'", throwable);
        }

    }


    @Transactional
    public void notifyRAOfficerOnUserRevocation(Certificate certificate) {

        notifyRAOfficerOnUserRevocation(certificate,
            findAllRAOfficer(AuthoritiesConstants.RA_OFFICER),
            findAllRAOfficer(AuthoritiesConstants.DOMAIN_RA_OFFICER),
            true);
    }

    public void notifyRAOfficerOnUserRevocation(Certificate certificate,
                                         List<User> raOfficerList,
                                         List<User> domainOfficerList,
                                         boolean logNotification) {

        if( !notifyRAOfficerOnUserRevocation){
            LOG.info("notifyRAOfficerOnUserRevocation deactivated");
            return;
        }

        LOG.info("certificate revoked by user (certificate # {})", certificate.getId());

        String revokedByUser = certificateUtil.getCertAttribute( certificate, CertificateAttribute.ATTRIBUTE_REVOKED_BY);

            // Notify RA officers
        for( User raOfficer: raOfficerList) {
            Locale locale = Locale.forLanguageTag(raOfficer.getLangKey());
            Context context = new Context(locale);
            context.setVariable("cert", certificate);
            context.setVariable("revokedByUser", revokedByUser);
            try {
                mailService.sendEmailFromTemplate(context, raOfficer, null, "mail/userRevokedCertificateEmail", "email.userRevokedCertificateEmail.subject");
            }catch (Throwable throwable){
                LOG.warn("Problem occurred while sending a notification eMail to RA officer address '" + raOfficer.getEmail() + "'", throwable);
/*
                if(logNotification) {
                    auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(raOfficer.getEmail()));
                }
 */
            }
        }

        if( certificate.getCsr() != null &&
            certificate.getCsr().getPipeline() != null ) {

            Pipeline pipeline = certificate.getCsr().getPipeline();

            // Process subset of CSRs for domain officers
            for (User domainOfficer : domainOfficerList) {

                if (pipelineUtil.isUserValidAsRA(pipeline, domainOfficer)) {
                    Locale locale = Locale.forLanguageTag(domainOfficer.getLangKey());
                    Context context = new Context(locale);
                    context.setVariable("cert", certificate);
                    context.setVariable("revokedByUser", revokedByUser);
                    try {
                        mailService.sendEmailFromTemplate(context, domainOfficer, null, "mail/newPendingRequestEmail", "email.newPendingRequestEmail.subject");
                    } catch (Throwable throwable) {
                        LOG.warn("Problem occurred while sending a notification eMail to domain officer address '" + domainOfficer.getEmail() + "'", throwable);
                        if (logNotification) {
                            auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(domainOfficer.getEmail()));
                        }
                    }
                }
            }
        }
    }

    @Transactional
    public void notifyRAOfficerOnRequest(CSR csr) {

        notifyRAOfficerOnRequest( csr,
            findAllRAOfficer(AuthoritiesConstants.RA_OFFICER),
            findAllRAOfficer(AuthoritiesConstants.DOMAIN_RA_OFFICER),
            true);
    }

    public void notifyRAOfficerOnRequest(CSR csr, List<User> raOfficerList, List<User> domainOfficerList,
                                         boolean logNotification) {

        if( !notifyRAOfficerOnRequest){
            LOG.info("notifyRAOfficerOnRequest deactivated");
            return;
        }

        LOG.info("certificate requested, causing a new pending requests (CSR # {})", csr.getId());

        List<CSR> newCsrList = new ArrayList<>();
        newCsrList.add(csr);

        // Notify RA officers
        for( User raOfficer: raOfficerList) {
            Locale locale = Locale.forLanguageTag(raOfficer.getLangKey());
            Context context = new Context(locale);
            context.setVariable("newCsrList", newCsrList);
            try {
                mailService.sendEmailFromTemplate(context, raOfficer, null, "mail/newPendingRequestEmail", "email.newPendingRequestEmail.subject");
            }catch (Throwable throwable){
                LOG.warn("Problem occurred while sending a notification eMail to RA officer address '" + raOfficer.getEmail() + "'", throwable);
/*
                if(logNotification) {
                    auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(raOfficer.getEmail()));
                }
 */
            }
        }

        // Process subset of CSRs for domain officers
        for( User domainOfficer: domainOfficerList) {

            if( pipelineUtil.isUserValidAsRA(csr.getPipeline(), domainOfficer) ){
                Locale locale = Locale.forLanguageTag(domainOfficer.getLangKey());
                Context context = new Context(locale);
                context.setVariable("newCsrList", newCsrList);
                try {
                    mailService.sendEmailFromTemplate(context, domainOfficer, null, "mail/newPendingRequestEmail", "email.newPendingRequestEmail.subject");
                }catch (Throwable throwable){
                    LOG.warn("Problem occurred while sending a notification eMail to domain officer address '" + domainOfficer.getEmail() + "'", throwable);
                    if(logNotification) {
                        auditService.saveAuditTrace(auditService.createAuditTraceNotificationFailed(domainOfficer.getEmail()));
                    }
                }
            }
        }
    }

    @Async
    public void notifyUserCerificateIssuedAsync(User requestor, Certificate cert, Set<String> additionalEmailSet ){

        try {
            notifyUserCerificateIssued(requestor, cert, additionalEmailSet );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for issued cert", e);
        }
    }

    @Transactional
    public void notifyUserCerificateIssued(User requestor, Certificate cert, Set<String> additionalEmailSet ) throws MessagingException {

        if( !notifyUserCerificateIssued){
            LOG.info("notifyUserCerificateIssued deactivated");
            return;
        }

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

        mailService.sendEmailFromTemplate(context, requestor, additionalEmailSet.toArray(new String[0]), "mail/acceptedRequestEmail", "email.acceptedRequest.title");
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

        if( !notifyUserCerificateRejected){
            LOG.info("notifyUserCerificateRejected deactivated");
            return;
        }

        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
        Context context = new Context(locale);
        context.setVariable("csr", csr);
        mailService.sendEmailFromTemplate(context, requestor, null, "mail/rejectedRequestEmail", "email.request.rejection.title");
    }


    @Async
    public void notifyUserCerificateRevokedAsync(User requestor, Certificate cert , CSR csr ){

        try {
            notifyCerificateRevoked(requestor, cert, csr );
        } catch (MessagingException e) {
            LOG.error("problem sending user notification for revoked certificate", e);
        }
    }

    @Transactional
    public void notifyCerificateRevoked(User requestor, Certificate cert, CSR csr ) throws MessagingException {

        if( !notifyCerificateRevoked){
            LOG.info("notifyCerificateRevoked deactivated");
            return;
        }

        Locale locale = Locale.forLanguageTag(requestor.getLangKey());
        Context context = new Context(locale);
        context.setVariable("csr", csr);
        context.setVariable("cert", cert);
        String subject = cert.getSubject();
        if (subject == null) {
            subject = "";
        }
        String[] args = {subject, cert.getSerial(), cert.getIssuer()};
        mailService.sendEmailFromTemplate(context, requestor, null, "mail/revokedCertificateEmail", "email.revokedCertificate.title", args);
    }

    private List<String> findAdditionalRecipients(Certificate cert){
        List<String> recipientList = new ArrayList<>();
        for( String araAttribute: notificationARAAttributes) {
            String emailAttribute = certificateUtil.getCertAttribute(cert, CsrAttribute.ARA_PREFIX + araAttribute, "");
            addSplittedString(recipientList, emailAttribute);
        }
        return recipientList;
    }

    /**
     *
     * @return
     */
    private List<User> findAllRAOfficer(String authority){

        List<User> raOfficerList = new ArrayList<>();
        for( User user: userRepository.findAll()) {
            for( Authority auth: user.getAuthorities()) {
                LOG.debug("user {} {} has role {}", user.getFirstName(), user.getLastName(), auth.getName());
                if( authority.equalsIgnoreCase(auth.getName())) {
                    raOfficerList.add(user);
                    LOG.debug("found user {} {} having the role of a RA officers", user.getFirstName(), user.getLastName());
                    break;
                }
            }
        }
        return raOfficerList;
    }

}
