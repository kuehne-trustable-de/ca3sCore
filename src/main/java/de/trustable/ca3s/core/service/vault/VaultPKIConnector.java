package de.trustable.ca3s.core.service.vault;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CaConnectorConfigUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.VaultUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultPkiOperations;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultSignCertificateRequestResponse;
import org.springframework.vault.support.VaultUnsealStatus;


import java.security.GeneralSecurityException;
import java.util.Base64;

import static de.trustable.ca3s.core.service.util.CaConnectorConfigUtil.ATT_ATTRIBUTE_ROLE;

@Service
public class VaultPKIConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultPKIConnector.class);

    private final CSRUtil csrUtil;

    private final CertificateUtil certUtil;

    private final VaultUtil vaultUtil;

    private final CertificateRepository certificateRepository;

    private final CaConnectorConfigUtil caConnectorConfigUtil;

    public VaultPKIConnector(CSRUtil csrUtil, CertificateUtil certUtil, VaultUtil vaultUtil, CertificateRepository certificateRepository, CaConnectorConfigUtil caConnectorConfigUtil) {
        this.csrUtil = csrUtil;
        this.certUtil = certUtil;
        this.vaultUtil = vaultUtil;
        this.certificateRepository = certificateRepository;
        this.caConnectorConfigUtil = caConnectorConfigUtil;
    }

    public de.trustable.ca3s.core.domain.Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConfig)
        throws GeneralSecurityException {

        LOGGER.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());

        VaultTemplate vaultTemplate = vaultUtil.getVaultTemplate(caConfig);

        VaultSysOperations adminOperations = vaultTemplate.opsForSys();
        VaultUnsealStatus unsealStatus = adminOperations.getUnsealStatus();
        LOGGER.debug("VaultUnsealStatus : {}", unsealStatus.isSealed() );
        if( unsealStatus.isSealed()){
            LOGGER.warn("vault '{}' is sealed. Please unseal", caConfig.getCaUrl());
            throw new GeneralSecurityException("Vault sealed");
        }

        String pki = caConfig.getSelector().trim();
        VaultPkiOperations pkiOperations = vaultTemplate.opsForPki(pki);
        LOGGER.debug("VaultPkiOperations for endpoint {} / {} using given token '*****'", caConfig.getCaUrl(), pki);

        VaultCertificateRequest request = VaultCertificateRequest.builder()
            .commonName(csrUtil.getCommonName(csr))
            .build();

        String role = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConfig, ATT_ATTRIBUTE_ROLE, "tls-endpoint");
        VaultSignCertificateRequestResponse certificateResponse = pkiOperations.signCertificateRequest(role, csr.getCsrBase64(), request);

        try {
            handleCertificate(certificateResponse.getRequiredData().getIssuingCaCertificate(), true);
        }catch(GeneralSecurityException gse){
            LOGGER.info("Issuing certificate from Vault causes processing problems", gse);
        }

        Certificate certDao = handleCertificate(certificateResponse.getRequiredData().getCertificate(), false);

        LOGGER.debug("Certificate '" + certDao.getSubject() + "' from Vault response");

        certDao.setRevocationCA(caConfig);
        certificateRepository.save(certDao);

        csr.setCertificate(certDao);
        csr.setStatus(CsrStatus.ISSUED);

        return certDao;
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

        return certDao;
    }

}
