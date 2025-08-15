package de.trustable.ca3s.core.service.vault;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.service.util.VaultUtil;
import de.trustable.util.CryptoUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultSysOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultUnsealStatus;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class VaultCertificateConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(VaultCertificateConnector.class);
    public static final String BEGIN_CERTIFICATE_PEM = "-----BEGIN CERTIFICATE-----";

    private final CertificateUtil certUtil;

    private final VaultUtil vaultUtil;

    private final CertificateRepository certificateRepository;

    private final AuditService auditService;

    public VaultCertificateConnector(CertificateUtil certUtil, VaultUtil vaultUtil, CertificateRepository certificateRepository, AuditService auditService) {
        this.certUtil = certUtil;
        this.vaultUtil = vaultUtil;
        this.certificateRepository = certificateRepository;
        this.auditService = auditService;
    }

    /**
     *
     * @param caConfig
     * @return
     */
    public CAStatus getStatus(final CAConnectorConfig caConfig) {

        try {
            VaultTemplate vaultTemplate = vaultUtil.getVaultTemplate(caConfig);
            VaultSysOperations adminOperations = vaultTemplate.opsForSys();
            VaultUnsealStatus unsealStatus = adminOperations.getUnsealStatus();
            LOGGER.debug("VaultUnsealStatus : {}", unsealStatus.isSealed() );
            if( unsealStatus.isSealed()){
                return CAStatus.Deactivated;
            }
            return CAStatus.Active;
        }catch(GeneralSecurityException ge){
            return CAStatus.Problem;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int retrieveCertificates(CAConnectorConfig caConfig) throws IOException, GeneralSecurityException {

        VaultTemplate vaultTemplate = vaultUtil.getVaultTemplate(caConfig);

        VaultSysOperations adminOperations = vaultTemplate.opsForSys();
        VaultUnsealStatus unsealStatus = adminOperations.getUnsealStatus();
        LOGGER.debug("VaultUnsealStatus : {}", unsealStatus.isSealed() );
        if( unsealStatus.isSealed()){
            LOGGER.warn("vault '{}' is sealed. Please unseal", caConfig.getCaUrl());
            return 0;
        }

        String selector = caConfig.getSelector().trim();
        VaultResponse vaultResponseChain = vaultTemplate.read(selector + "/cert/ca_chain");
        String chain = (String)(vaultResponseChain.getData().get("ca_chain"));

        for( String certPem: splitPemChain(chain)){

            LOGGER.debug("ca chain splitted : {}", certPem );
            importCertificate(certPem,
                caConfig.getName(),
                null,
                caConfig);
        }

        List<String> certKeyList = vaultTemplate.list(selector + "/certs");

        LOGGER.debug("certKeyList : {}", certKeyList );

        for( String vaultKey:certKeyList){

            List<Certificate> knownCertList = certificateRepository.findByAttributeValue(CertificateAttribute.ATTRIBUTE_VAULT_KEY, vaultKey);
            if( knownCertList.isEmpty()) {
                VaultResponse vaultResponse = vaultTemplate.read(selector + "/cert/" + vaultKey);

                LOGGER.debug("vaultResponse.getData() : {}", vaultResponse.getData());

                Certificate certificate = importCertificate((String) (vaultResponse.getData().get("certificate")),
                    caConfig.getName(),
                    vaultKey,
                    caConfig);
            }else{
                LOGGER.debug("certificate with vaultKey {} already imported", vaultKey);
            }
        }
        return 0;

    }

    private List<String> splitPemChain(String pemChain){
        String [] parts = pemChain.split(BEGIN_CERTIFICATE_PEM);
        List<String> certList = new ArrayList<>();

        for( String part: parts){
            if( !part.trim().isEmpty()){
                certList.add(BEGIN_CERTIFICATE_PEM + part);
            }
        }
        return certList;
    }

    /**
     * retrieve a single certificate content and store it in the internal database
     *
     * @param caName        the textual description of the ADCS CA
     * @param key         te ADCS request id of the certificate to be retrieved
     * @param config        the connection data identifying an ADCS instance
     */
    public Certificate importCertificate(final String pemCert,
                                         String caName,
                                         String key,
                                         CAConnectorConfig config){

        try {

            Certificate certDao = certUtil.getCertificateByPEM(pemCert);
            if (certDao == null) {
                certDao = certUtil.createCertificate(pemCert, null,
                    null, false);
                auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_VAULT_CERTIFICATE_IMPORTED, certDao));
            }

            certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_PROCESSING_CA, caName);
            if( key != null) {
                certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_VAULT_KEY, key);
            }

            certificateRepository.save(certDao);

            LOGGER.debug("certificate with key '{}' imported from ca '{}'", key, caName);

            return certDao;

        } catch (GeneralSecurityException | IOException e) {
            LOGGER.info("retrieving and importing certificate with key '{}' from ca '{}' causes {}",
                key, caName, e.getLocalizedMessage());


        }
        return null;
    }

}
