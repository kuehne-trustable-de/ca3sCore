package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static de.trustable.ca3s.core.domain.CertificateAttribute.ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER;

@Service
public class CertificateAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateAsyncUtil.class);

    final private ReplacementCandidateUtil replacementCandidateUtil;
    private final CertificateUtil certUtil;
    private final CertificateRepository certificateRepository;

    public CertificateAsyncUtil(ReplacementCandidateUtil replacementCandidateUtil, CertificateUtil certUtil, CertificateRepository certificateRepository) {
        this.replacementCandidateUtil = replacementCandidateUtil;
        this.certUtil = certUtil;
        this.certificateRepository = certificateRepository;
    }

    @Async
    @Transactional
    public void onChange(Certificate certificate, List<String> emailList) {

        LOG.debug("Async call to onChange( '{}')", certificate);

        if( certificate == null){
            return;
        }

        replacementCandidateUtil.notifyOnExcessiveActiveCertificates(certificate, emailList);
    }


    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementNotificationCounter(Certificate certificate) {

        String counterString = certUtil.getCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER);
        if( counterString == null){
            return;
        }
        int retryCount = Integer.parseInt(counterString) + 1;
        certUtil.setCertAttribute(certificate, ATTRIBUTE_CERTIFICATE_NOTIFICATION_COUNTER, "" + retryCount, false);

        certificateRepository.save(certificate);

        LOG.info("increment notification counter for cert #{} to {}", certificate.getId(), retryCount);

    }
}
