package de.trustable.ca3s.core.service.vault;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/*
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultPkiOperations;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultSignCertificateRequestResponse;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultUnsealStatus;
*/
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;

@Service
public class VaultPKIConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultPKIConnector.class);

    long timeoutMilliSec = 1000L;

    private final CSRUtil csrUtil;

    private final CertificateUtil certUtil;

    private final ProtectedContentUtil protUtil;

    private final CertificateRepository certificateRepository;

    public VaultPKIConnector(CSRUtil csrUtil, CertificateUtil certUtil, ProtectedContentUtil protUtil, CertificateRepository certificateRepository) {
        this.csrUtil = csrUtil;
        this.certUtil = certUtil;
        this.protUtil = protUtil;
        this.certificateRepository = certificateRepository;
    }

    public de.trustable.ca3s.core.domain.Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConnConfig)
        throws GeneralSecurityException {

        LOGGER.debug("DISABLED csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());
/*
        VaultEndpoint endpoint = null;
        try {
            endpoint = VaultEndpoint.from(new URI(caConnConfig.getCaUrl()));
        } catch (URISyntaxException e) {
            LOGGER.warn("problem processing vault url '{}' : {}", caConnConfig.getCaUrl(), e.getMessage());
            throw new GeneralSecurityException(e);
        }
        String plaintextToken = protUtil.unprotectString( caConnConfig.getSecret().getContentBase64());
        VaultTemplate vaultTemplate = new VaultTemplate(endpoint, new TokenAuthentication(plaintextToken));

        VaultSysOperations adminOperations = vaultTemplate.opsForSys();
        VaultUnsealStatus unsealStatus = adminOperations.getUnsealStatus();
        LOGGER.debug("VaultUnsealStatus : {}", unsealStatus.isSealed() );
        if( unsealStatus.isSealed()){
            LOGGER.warn("vault '{}' is sealed. Please unseal", caConnConfig.getCaUrl());
            throw new GeneralSecurityException("Vault sealed");
        }

        List<String> certKeyList = vaultTemplate.list("subca_store/certs");

        VaultPkiOperations pkiOperations = vaultTemplate.opsForPki("subca_store");
        LOGGER.debug("VaultPkiOperations for endpoint {} using token {}", caConnConfig.getCaUrl(), plaintextToken);

        VaultCertificateRequest request = VaultCertificateRequest.builder()
            .commonName(csrUtil.getCommonName(csr))
            .build();

        VaultSignCertificateRequestResponse certificateResponse = pkiOperations.signCertificateRequest(caConnConfig.getSelector(), csr.getCsrBase64(), request);

        try {
            handleCertificate(certificateResponse.getRequiredData().getIssuingCaCertificate(), true);
        }catch(GeneralSecurityException gse){
            LOGGER.info("Issuing certificate from Vault causes processing problems", gse);
        }

        Certificate certDao = handleCertificate(certificateResponse.getRequiredData().getCertificate(), false);

        LOGGER.debug("Certificate '" + certDao.getSubject() + "' from Vault response");

        certDao.setRevocationCA(caConnConfig);
        certificateRepository.save(certDao);

        csr.setCertificate(certDao);
        csr.setStatus(CsrStatus.ISSUED);

        return certDao;
*/
        return null;
    }

    private Certificate handleCertificate(final String certBase64, final boolean reimport) throws GeneralSecurityException {
        if( certBase64 == null || certBase64.isEmpty()){
            // no additional certs
            return null;
        }
        byte[] certificateBytes = Base64.getDecoder().decode(certBase64);

        Certificate certDao = certUtil.createCertificate(certificateBytes,
            null, null, true);
        certificateRepository.save(certDao);

        LOGGER.debug("Additional cert '" + certDao.getSubject() + "' from CMP response");

        return certDao;
    }

}
