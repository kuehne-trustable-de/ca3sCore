package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
public class ReplacementCandidateUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReplacementCandidateUtil.class);

    final private CertificateRepository certificateRepository;
    final private UserRepository userRepository;
    final private AcmeAccountRepository acmeAccountRepository;


    final private AuditService auditService;
    final private NotificationService notificationService;

    final int notifyActiveParallelCertificates;

    final int rejectActiveParallelCertificates;

    public ReplacementCandidateUtil(CertificateRepository certificateRepository, UserRepository userRepository,
                                    AcmeAccountRepository acmeAccountRepository,
                                    AuditService auditService,
                                    @Lazy NotificationService notificationService,
                                    @Value("${ca3s.issuance.limit.notify.active-parallel-certificates:20}") int notifyActiveParallelCertificates,
                                    @Value("${ca3s.issuance.limit.reject.active-parallel-certificates:100}") int rejectActiveParallelCertificates) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.acmeAccountRepository = acmeAccountRepository;
        this.auditService = auditService;
        this.notificationService = notificationService;
        this.notifyActiveParallelCertificates = notifyActiveParallelCertificates;
        this.rejectActiveParallelCertificates = rejectActiveParallelCertificates;
    }


    /**
     * @param sanArr SAN array
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(String[] sanArr) {

        return findReplaceCandidates(null, sanArr);
    }

    public List<Certificate> findReplaceCandidates(String cn, String[] sanArr) {
        return findReplaceCandidates(Instant.now(), cn, sanArr);
    }

    /**
     * @param sanArr SAN array
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(Instant validOn, String cn, String[] sanArr) {

        List<String> sans = new ArrayList<>();
        for (String san : sanArr) {
            LOG.debug("SAN present: {} ", san);
            sans.add(san.toLowerCase(Locale.ROOT));
        }

        return findReplaceCandidates(validOn, cn, sans, null);

    }

    /**
     * @param sanList SAN list
     * @return list of certificates
     */
    public List<Certificate> findReplaceCandidates(Instant validOn,
                                                   String cn, List<String> sanList,
                                                   Certificate cert) {

        List<String> sanListLowerCase = new ArrayList<>();
        if(sanList != null){
            for( String san: sanList){
                sanListLowerCase.add(san.toLowerCase(Locale.ROOT));
            }
        }
        if (cn != null) {
            if (!sanListLowerCase.contains(cn.toLowerCase(Locale.ROOT))) {
                if( CertificateUtil.isIPAddress(cn)) {
                    sanListLowerCase.add("IP:" + cn.toLowerCase(Locale.ROOT));
                }else{
                    sanListLowerCase.add("DNS:" +cn.toLowerCase(Locale.ROOT));
                }
            }
        }
        return findReplaceCandidates(validOn, sanListLowerCase, cert);

    }

    /**
     * @param sans SANs as List
     * @return list of certificates
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Certificate> findReplaceCandidates(Instant validOn, List<String> sans, Certificate cert) {

        LOG.debug("sans list contains {} elements", sans.size());

        List<Certificate> candidateList = new ArrayList<>();

        if (sans.size() == 0) {
            return candidateList;
        }

        int maxPageRows = notifyActiveParallelCertificates;
        if( rejectActiveParallelCertificates > maxPageRows){
            maxPageRows = rejectActiveParallelCertificates;
        }
        maxPageRows += 10; // give some head space


        Page<Certificate> matchingCertList = certificateRepository.findActiveCertificatesBySANs(
            PageRequest.of(0, maxPageRows), validOn, sans);
        int resultRowCount = matchingCertList.getNumberOfElements();
        LOG.debug("objArrList contains {} elements", resultRowCount);


        for (Certificate candidateCert : matchingCertList) {
            LOG.debug("replacement candidate {}: {} ", candidateCert.getId(), candidateCert.getSubject());

            boolean matches = true;
            for (CertificateAttribute certAttr : candidateCert.getCertificateAttributes()) {

                if (certAttr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_SAN) || certAttr.getName().equals(CsrAttribute.ATTRIBUTE_TYPED_VSAN)) {
                    String san = certAttr.getValue().toLowerCase(Locale.ROOT);
                    if (!sans.contains(san)) {
                        matches = false;
                        LOG.debug("candidate san {} NOT in provided san list", san);
                        break;
                    }
                }
            }
            if (matches) {
                candidateList.add(candidateCert);
                LOG.debug("replacement candidate {}: contains all SANs", candidateCert.getId());
            }
        }

        if( candidateList.size() > rejectActiveParallelCertificates ){
            LOG.warn("maximum number of active, parallel certificates (#{}) exceeded for certificate id {}", resultRowCount, cert.getId() );
        }

        return candidateList;
    }

    public void notifyOnExcessiveActiveCertificates(Certificate cert, List<String> emailAddressList) {

        if (emailAddressList == null || emailAddressList.isEmpty()) {
            return;
        }

        for (CertificateAttribute certAttr : cert.getCertificateAttributes()) {
            if (certAttr.getName().equals(CertificateAttribute.ATTRIBUTE_REPLACES_NUMBER_OF_CERTS)) {
                try {
                    int nReplacedCerts = Integer.parseInt(certAttr.getValue());
                    if (nReplacedCerts > notifyActiveParallelCertificates) {
                        notificationService.notifyRequestorOnExcessiveActiveCertificates(emailAddressList.get(0), nReplacedCerts, cert);
                    }
                } catch (NumberFormatException nfe) {
                    LOG.warn("number of replaced certificates '{}' for certificate id {} is not a number", certAttr.getValue(), cert.getId());
                }
            }
        }
    }
}
