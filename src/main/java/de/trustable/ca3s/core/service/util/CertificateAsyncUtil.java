package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CertificateAsyncUtil {

    private static final Logger LOG = LoggerFactory.getLogger(CertificateAsyncUtil.class);

    final private ReplacementCandidateUtil replacementCandidateUtil;

    public CertificateAsyncUtil(ReplacementCandidateUtil replacementCandidateUtil) {
        this.replacementCandidateUtil = replacementCandidateUtil;
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
}
