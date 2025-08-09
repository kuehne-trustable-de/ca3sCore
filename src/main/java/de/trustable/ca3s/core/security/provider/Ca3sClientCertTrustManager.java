package de.trustable.ca3s.core.security.provider;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class Ca3sClientCertTrustManager extends Ca3sTrustManager{

    private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sClientCertTrustManager.class);

    public Ca3sClientCertTrustManager(CertificateRepository certificateRepository,
                                      CryptoService cryptoUtil,
                                      CertificateUtil certUtil,
                                      AuditService auditService) {
        super( certificateRepository, cryptoUtil, certUtil, auditService);
    }

    List<Certificate> getAcceptedIssuerList() {

        LOGGER.debug("In getAcceptedIssuerList ... ");

        Set<Certificate> clientCertRootSet = new HashSet<>();
        List<Certificate> clientCertList =  certificateRepository.findActiveByAttribute(
            CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT);

        for(Certificate cert : clientCertList) {
            if (cert.getRootCertificate() != null) {
                clientCertRootSet.add(cert.getRootCertificate());
            }
        }

        LOGGER.debug("#{} active client certs, #{} corresponding roots", clientCertList.size(), clientCertRootSet.size());
        for( Certificate cert : clientCertRootSet ) {
            LOGGER.debug("client cert root {}", cert.getSubject());
        }
        return new ArrayList<>(clientCertRootSet);
    }

}
