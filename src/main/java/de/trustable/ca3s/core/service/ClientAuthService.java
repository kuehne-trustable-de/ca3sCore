package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientAuthService {
    private final Logger log = LoggerFactory.getLogger(ClientAuthService.class);

    private final CertificateRepository certificateRepository;
    private final CertificateUtil certificateUtil;
    private final TokenProvider tokenProvider;

    public ClientAuthService(CertificateRepository certificateRepository, CertificateUtil certificateUtil, TokenProvider tokenProvider) {
        this.certificateRepository = certificateRepository;
        this.certificateUtil = certificateUtil;
        this.tokenProvider = tokenProvider;
    }

    public boolean isClientCertValidForUser(final String jwt, final User user){

        String ski = tokenProvider.getSKIClaim(jwt);
        log.info("client certificate with ski {}", ski);

        List<Certificate> certificateList =  certificateRepository.findActiveBySKI(ski);
        for( Certificate cert: certificateList){
            String certIdString = certificateUtil.getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_USER_CLIENT_CERT);
            if( user.getId().toString().equalsIgnoreCase(certIdString)){
                log.info("client certificate {} valid for user {}", cert.getId(), user.getId());
                return true;
            }else{
                log.warn("client certificate {} assigned to user {} but not requesting user {}",
                    cert.getId(),
                    certIdString,
                    user.getId());
            }
        }
        return false;
    }

}
