package de.trustable.ca3s.core.service.vault;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.vault.SignRequest;
import de.trustable.ca3s.core.service.dto.vault.SignResponse;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;

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

         LOGGER.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());

        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout((int) timeoutMilliSec);
        simpleClientHttpRequestFactory.setReadTimeout((int) timeoutMilliSec);
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);

        SignRequest signRequest = new SignRequest();
        signRequest.setCsr(csr.getCsrBase64());
        signRequest.setCommon_name(csrUtil.getCommonName(csr));
        LOGGER.debug("Vault request  {}",signRequest);

        HttpHeaders responseHeaders = new HttpHeaders();

        String token  = protUtil.unprotectString(caConnConfig.getSecret().getContentBase64());
        responseHeaders.add("X-Vault-Token", token);
        HttpEntity<SignRequest> request = new HttpEntity<>(signRequest, responseHeaders);

        ResponseEntity<SignResponse> responseEntity = restTemplate.postForEntity(caConnConfig.getCaUrl(), request, SignResponse.class);

        if( responseEntity.getStatusCode() != HttpStatus.OK){
            LOGGER.info("Vault request returns HTTP status {}",responseEntity.getStatusCode());
            LOGGER.debug("Vault request returns HTTP response {}",responseEntity);

            throw new GeneralSecurityException(
                "Vault request returns HTTP status " + responseEntity.getStatusCode());
        }

        SignResponse signResponse = responseEntity.getBody();
        if( signResponse == null ||
            signResponse.getData() == null ||
            signResponse.getData().getCertificate() == null ||
            signResponse.getData().getCertificate().trim().isEmpty()){

            throw new GeneralSecurityException("Vault request does not return a certificate");
        }

        String certificate = signResponse.getData().getCertificate();

        Certificate certDao = null;
        try {
            certDao = certUtil.createCertificate(certificate,
                csr, null, false);
        } catch (IOException e) {
            LOGGER.debug("Vault request returns \n{}",certificate);
            throw new GeneralSecurityException("Vault request returns unparseable certificate");
        }

        certDao.setRevocationCA(caConnConfig);
        certificateRepository.save(certDao);

        csr.setCertificate(certDao);
        csr.setStatus(CsrStatus.ISSUED);

        return certDao;
    }
}
