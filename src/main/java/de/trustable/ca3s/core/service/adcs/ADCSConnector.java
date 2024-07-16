package de.trustable.ca3s.core.service.adcs;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import de.trustable.ca3s.adcsCertUtil.CertificateEnrollmentResponse;
import de.trustable.ca3s.adcsCertUtil.GetCertificateResponse;
import de.trustable.ca3s.adcsCertUtil.*;
import de.trustable.ca3s.client.api.RemoteADCSClient;
import de.trustable.ca3s.client.invoker.ApiException;
import de.trustable.ca3s.client.model.*;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.security.provider.Ca3sTrustManager;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.ws.rs.ProcessingException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.*;


@Service
public class ADCSConnector {


    Logger LOGGER = LoggerFactory.getLogger(ADCSConnector.class);

    private final CryptoService cryptoUtil;
    private final CertificateUtil certUtil;
    private final Ca3sTrustManager ca3sTrustManager;
    private final CSRRepository csrRepository;
    private final CSRUtil csrUtil;
    private final CertificateRepository certificateRepository;
    private final ProtectedContentUtil protUtil;
    private final AuditService auditService;
    private final String ca3sSalt;
    private final int iterations;
    private final String apiKeySalt;
    private final int apiKeyIterations;
    private final String pbeAlgo;


    /**
     * Adapter class to connect to an ADCS server using the parameter given in a CaConnectorConfig
     */
    public ADCSConnector(CryptoService cryptoUtil,
                         CertificateUtil certUtil,
                         Ca3sTrustManager ca3sTrustManager,
                         CSRRepository csrRepository,
                         CSRUtil csrUtil,
                         CertificateRepository certificateRepository,
                         ProtectedContentUtil protUtil,
                         AuditService auditService,
                         @Value("${ca3s.connection.protection.salt:ca3sSalt}") String ca3sSalt,
                         @Value("${ca3s.connection.protection.iterations:4567}") int iterations,
                         @Value("${ca3s.connection.protection.api-key-salt:apiKeySalt}") String apiKeySalt,
                         @Value("${ca3s.connection.protection.api-key-iterations:3756}") int apiKeyIterations,
                         @Value("${ca3s.connection.protection.pbeAlgo:PBKDF2WithHmacSHA256}") String pbeAlgo) {
        this.cryptoUtil = cryptoUtil;
        this.certUtil = certUtil;
        this.ca3sTrustManager = ca3sTrustManager;
        this.csrRepository = csrRepository;
        this.csrUtil = csrUtil;
        this.certificateRepository = certificateRepository;
        this.protUtil = protUtil;
        this.auditService = auditService;
        this.ca3sSalt = ca3sSalt;
        this.iterations = iterations;
        this.apiKeySalt = apiKeySalt;
        this.apiKeyIterations = apiKeyIterations;

        this.pbeAlgo = pbeAlgo;
    }

    ADCSWinNativeConnector getConnector(CAConnectorConfig config) throws ADCSProxyUnavailableException {

        LOGGER.debug("connector '" + config.getName() + "', Url configured as '" + config.getCaUrl() + "'");
        if ("inProcess".equalsIgnoreCase(config.getCaUrl())) {
            LOGGER.debug("ADCSConnector trying to load Windows connection classes...");
            try {
                return new ADCSNativeImpl();
            } catch (UnsatisfiedLinkError ule) {
                LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.");
            } catch (ADCSException e) {
                LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.", e);
            }
        } else {

            if (config.getSecret() == null) {
                throw new ADCSProxyUnavailableException("passphrase missing in ca configuration for ca '" + config.getName() + "' !");
            }

            String plainSecret = protUtil.unprotectString(config.getSecret().getContentBase64());

            try {
                ADCSWinNativeConnector adcsConnector = new ADCSWinNativeConnectorAdapter(
                    config.getCaUrl(),
                    plainSecret,
                    ca3sTrustManager,
                    ca3sSalt,
                    iterations,
                    apiKeySalt,
                    apiKeyIterations );
                LOGGER.debug("ADCSConnector trying to connect to remote ADCS proxy ...");
                String info = adcsConnector.getInfo();
                LOGGER.debug("info call returns '{}'", info);

                return adcsConnector;
            } catch (ADCSProxyUnavailableException pue) {
                LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
                throw pue;
            } catch (ADCSException | GeneralSecurityException e) {
                LOGGER.warn("info call failed", e);
            }
        }

        return new EmptyADCSWinNativeConnectorAdapter();
    }

    ADCSWinNativeConnector getConnector(final String caUrl, final String secret ) throws ADCSProxyUnavailableException {

        LOGGER.debug("connector direct call to '" + caUrl + "'");
        if ("inProcess".equalsIgnoreCase(caUrl)) {
            LOGGER.debug("ADCSConnector trying to load Windows connection classes...");
            try {
                return new ADCSNativeImpl();
            } catch (UnsatisfiedLinkError ule) {
                LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.");
            } catch (ADCSException e) {
                LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.", e);
            }
        } else {

            if (secret == null || secret.isEmpty()) {
                throw new ADCSProxyUnavailableException("passphrase missing in connector direct call to '" + caUrl + "' !");
            }

            try {
                ADCSWinNativeConnector adcsConnector = new ADCSWinNativeConnectorAdapter(
                    caUrl,
                    secret,
                    ca3sTrustManager,
                    ca3sSalt,
                    iterations,
                    apiKeySalt,
                    apiKeyIterations );
                LOGGER.debug("ADCSConnector trying to connect to remote ADCS proxy ...");
                String info = adcsConnector.getInfo();
                LOGGER.debug("info call returns '{}'", info);

                return adcsConnector;
            } catch (ADCSProxyUnavailableException pue) {
                LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
                throw pue;
            } catch (ADCSException | GeneralSecurityException e) {
                LOGGER.warn("info call failed", e);
            }
        }

        return new EmptyADCSWinNativeConnectorAdapter();
    }


    /**
     * Retrieve the instance details of the related ADCSProxy
     *
     * @param caUrl
     * @param secret
     * @return
     */
    public ADCSInstanceDetails getInstanceDetails(final String caUrl, final String secret) {

        try {
            return getConnector(caUrl, secret).getCAInstanceDetails();
        } catch (ADCSException adcsEx) {
            LOGGER.debug("CAConnectorType ADCS at " + caUrl + " throws Exception: {} ", adcsEx.getLocalizedMessage());
        }
        return null;

    }

    /**
     * Retrieve the current status of the ADCSProxy
     *
     * @param caConfig set of configuration items
     * @return current status
     */
    public CAStatus getStatus(final CAConnectorConfig caConfig) {

        try {
            String adcsStatus = getConnector(caConfig).getInfo();
            if ((adcsStatus != null) && (adcsStatus.trim().length() > 0)) {
                return CAStatus.Active;
            }
        } catch (ADCSException adcsEx) {
            LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl() + " throws Exception: {} ", adcsEx.getLocalizedMessage());
        }

        return CAStatus.Deactivated;

    }

    /**
     * Send a csr object to the ADCS and retrieve a created certificate
     *
     * @param csr    the CSR object, not just a P10 PEM string, holding e.g. a CRS status
     * @param config CAConnectorConfig
     * @return the freshly created certificate, already stored in the database
     * @throws GeneralSecurityException something went wrong, e.g. a rejection of the CSR. The status of the CSR is updated accordingly.
     */
    public Certificate signCertificateRequest(CSR csr, CAConnectorConfig config) throws GeneralSecurityException {

        LOGGER.debug("incoming csr for ADCS");

        createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_STARTED_TIMESTAMP, "" + System.currentTimeMillis());

        csr.setStatus(CsrStatus.PROCESSING);

        String csrString = csr.getCsrBase64();

        LOGGER.debug("request : " + csrString);
        PKCS10CertificationRequest p10Req = cryptoUtil.convertPemToPKCS10CertificationRequest(csrString);

        Certificate certDao = null;

        // send and receive ..
        try {
            String normalizedCsrString = CryptoService.pkcs10RequestToPem(p10Req);
            if (!normalizedCsrString.trim().equalsIgnoreCase(csrString)) {
                LOGGER.debug("csr normalization changes content to : " + normalizedCsrString);
            }

            Map<String, String> attrMap = new HashMap<>();

            String template = config.getSelector();
            if ((template != null) && (template.trim().length() > 0)) {
                LOGGER.debug("requesting certificate using template : " + template);
                attrMap.put("Certificate Template", template);
            } else {
                LOGGER.debug("requesting certificate without template ");
            }

            CertificateEnrollmentResponse certResponse = getConnector(config).submitRequest(normalizedCsrString, attrMap);

            if (SubmitStatus.ISSUED.equals(certResponse.getStatus())) {

                if ((certResponse.getB64CACert() != null) && !certResponse.getB64CACert().trim().isEmpty()) {
                    // install CA cert if not already known ...
                    Certificate certCADao = certUtil.createCertificate(certResponse.getB64CACert(), null, null);
                    certificateRepository.save(certCADao);
                }

                // handle response
                certDao = certUtil.createCertificate(certResponse.getB64Cert(), csr, null);

                certDao.setRevocationCA(config);

                // the Request ID is specific to ADCS
                certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID,
                    certResponse.getReqId());

                certificateRepository.save(certDao);

                csr.setCertificate(certDao);
                csr.setStatus(CsrStatus.ISSUED);

                createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP, "" + System.currentTimeMillis());
                createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID, "" + certResponse.getReqId());

                LOGGER.debug("returning certDao : " + certDao.getId());

            } else if ((SubmitStatus.DENIED.equals(certResponse.getStatus()))
                || (SubmitStatus.INCOMPLETE.equals(certResponse.getStatus()))
                || (SubmitStatus.ERROR.equals(certResponse.getStatus()))) {

                csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, "ADCS call failed with Status '" + certResponse.getStatus() + "'.");

                createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP, "" + System.currentTimeMillis());
                createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID, "" + certResponse.getReqId());

                csrRepository.save(csr);

                throw new GeneralSecurityException("adcs rejected request");

            } else if ((SubmitStatus.UNDER_SUBMISSION.equals(certResponse.getStatus()))
                || (SubmitStatus.ISSUED_OUT_OF_BAND.equals(certResponse.getStatus()))) {
                csr.setStatus(CsrStatus.PENDING);

                createCsrAttribute(csr, CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID, "" + certResponse.getReqId());

            } else {
                throw new GeneralSecurityException(
                    "adcs connector returned non-positive status '" + certResponse.getStatus() + "'");
            }

        } catch (NoLocalADCSException noLocalAdcsEx) {
            // no local ADCS available ...
            // reset the state back to pending
            csr.setStatus(CsrStatus.PENDING);

            throw new GeneralSecurityException("no local adcs connector available", noLocalAdcsEx);

        } catch (ADCSException adcsEx) {
            // no local ADCS available ...
            // reset the state back to pending
            csr.setStatus(CsrStatus.PENDING);

            throw new GeneralSecurityException("adcs connector returned exception", adcsEx);

        } catch (IOException ioex) {
            // presumably a connection problem, reset the state back to pending
            csr.setStatus(CsrStatus.PENDING);

            throw new GeneralSecurityException("adcs connector caused IOException", ioex);

        } finally {
            csrRepository.save(csr);
        }

        return (certDao);

    }

    /**
     * @param csr
     * @return
     */
    private void createCsrAttribute(CSR csr, final String name, final String value) {

        csrUtil.setCsrAttribute(csr, name, value, false);
    }

    /**
     * Revoke (or reactivate) a given certificate created by the ADCS server identified by connector config
     *
     * @param certDao        the certificate object to be revoked
     * @param crlReason      the revocation reason. The reason 'removeFromCRL' reactivates a certificate that was put 'on hold' previously.
     * @param revocationDate the revocation date
     * @param config         the connection data identifying an ADCS instance
     * @throws GeneralSecurityException something went wrong, e.g. revocation reason is unknown
     */
    public void revokeCertificate(Certificate certDao, final CRLReason crlReason, final Date revocationDate, CAConnectorConfig config)
        throws GeneralSecurityException {

        int reasonIntValue = crlReason.getValue().intValue();
        if (CRLReason.removeFromCRL == reasonIntValue) {
            reasonIntValue = 0xffffffff;
        } else if (reasonIntValue > 6) {
            throw new GeneralSecurityException("adcs connector supports revocation reasons 0..6, not " + reasonIntValue + " !");
        }

        try {
            BigInteger serial = new BigInteger(certDao.getSerial(), 10);
            String serialAsHex = serial.toString(16);
            LOGGER.debug("revoking certificate {} with serial '{}' with reason {}", certDao.getId(), serialAsHex, reasonIntValue);

            getConnector(config).revokeCertifcate(serialAsHex, reasonIntValue, revocationDate);

        } catch (ADCSException adcsEx) {
            // no local ADCS available ...
            throw new GeneralSecurityException("adcs connector returned exception", adcsEx);
        }

    }


    /**
     * Try to retrieve new certificates added since the last call. This method is usually called by a timer.
     * A chunk of certificates starting with a given offset will be requested. If there are new certificates available (with a ADCS request id greater than the offset)
     * the content of these new certificates will be retrieved in distinct calls and stored in the internal database. The highest request ID will be stored as starting
     * offset for subsequent calls.
     * The number of certificates is limited to avoid blocking the calling cron job.
     *
     * @param config the connection data identifying an ADCS instance
     * @return the number in imported certificates
     * @throws OODBConnectionsADCSException  something went wrong
     * @throws ADCSProxyUnavailableException something went wrong, the adcsProxy is unavailable
     */
    public int retrieveCertificatesOffsetOnly(CAConnectorConfig config) throws OODBConnectionsADCSException, ADCSProxyUnavailableException {

        LOGGER.debug("in retrieveCertificates");

        int limit = 100;

        int pollingOffset = 0;
        if (config.getPollingOffset() != null) {
            pollingOffset = config.getPollingOffset();
        }

        ADCSWinNativeConnector adcsConnector = getConnector(config);

        try {

            String info = adcsConnector.getInfo();

            List<String> newReqIdList = adcsConnector.getRequesIdList(limit, pollingOffset, 0L, 0L);
            if (newReqIdList.isEmpty()) {
                LOGGER.debug("no certificates retrieved at request offset {} at ca '{}'", pollingOffset, info);
            }

            for (String reqId : newReqIdList) {
                pollingOffset = Integer.parseInt(reqId);

//				LOGGER.debug("certRepository {}, info '{}', reqId {}", certRepository, info, reqId );

                List<Certificate> certDaoList = certificateRepository.findBySearchTermNamed2(
                    CertificateAttribute.ATTRIBUTE_PROCESSING_CA, info,
                    CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID, reqId);

                if (certDaoList.isEmpty()) {
                    importCertificate(adcsConnector, info, reqId, config);

                } else {
                    LOGGER.debug("certificate with requestID '{}' from ca '{}' alreeady present", reqId, info);
                }
            }

        } catch (OODBConnectionsADCSException | ADCSProxyUnavailableException oodbc) {
            throw oodbc;
        } catch (ADCSException e) {
            LOGGER.info("polling certificate list starting from {} with a limit of {} causes {}", pollingOffset,
                limit, e.getLocalizedMessage());
            LOGGER.warn("ACDSException : ", e);
        }

        int nNewCerts = (pollingOffset - config.getPollingOffset());

        if (nNewCerts > 0) {
            config.setPollingOffset(pollingOffset);
        }

        return nNewCerts;
    }

    /**
     * Try to retrieve new certificates resolved since the last call. This method is usually called by a timer.
     * A chunk of certificates with an resolved date after the timestamp of the last call will be requested. If there are new resolved certificates available
     * the content of these new certificates will be retrieved in distinct calls and stored in the internal database.
     * The number of certificates is limited to avoid blocking the calling cron job.
     *
     * @param config the connection data identifying an ADCS instance
     * @return the number in imported certificates
     * @throws OODBConnectionsADCSException  something went wrong
     * @throws ADCSProxyUnavailableException something went wrong, the adcsProxy is unavailable
     */
    public int retrieveCertificatesByResolvedDate(CAConnectorConfig config) throws OODBConnectionsADCSException, ADCSProxyUnavailableException {

        LOGGER.debug("in retrieveCertificatesByResolvedDate");
        int nNewCerts = 0;
        int limit = 100;

        ADCSWinNativeConnector adcsConnector = getConnector(config);

        try {

            String info = adcsConnector.getInfo();


            List<Certificate> certWithoutResolvedList = certificateRepository.findTimestampNotExistForCA(info, CertificateAttribute.ATTRIBUTE_CA_RESOLVED_TIMESTAMP);
            if (!certWithoutResolvedList.isEmpty()) {

                // ensure all certificates have a RESOLVED_AT timestamp. Should happen in migration scenarios only
                for (int i = 0; i < limit; i++) {
                    try {
                        Certificate certWOResolved = certWithoutResolvedList.get(i);
                        String caProcessingId = certUtil.getCertAttribute(certWOResolved, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID);
                        if (caProcessingId == null) {
                            LOGGER.warn("certificate #{} retrieved for ca '{}' but without CA_PROCESSING_ID", certWOResolved.getId(), info);
                        } else {
                            GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(caProcessingId);
                            certUtil.setCertAttribute(certWOResolved,
                                CertificateAttribute.ATTRIBUTE_CA_RESOLVED_TIMESTAMP,
                                CertificateUtil.getPaddedTimestamp(certResponse.getResolvedDate()));

                            LOGGER.warn("added resolved timestamp {} to certificate #{} retrieved for ca '{}' with CA_PROCESSING_ID '{}'",
                                CertificateUtil.getPaddedTimestamp(certResponse.getResolvedDate()),
                                certWOResolved.getId(),
                                info,
                                caProcessingId);
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // no size check ... so the exception is no problem
                        // the size of the list be big so there is no real use to ask for it.
                    }
                }
            } else {

                String maxTimestamp = certificateRepository.findMaxTimestampForCA(info, CertificateAttribute.ATTRIBUTE_CA_RESOLVED_TIMESTAMP);
                LOGGER.debug("findMaxResolvedForCA at ca '{}' returned {}  / {}", info, maxTimestamp, dateFromTimestampString(maxTimestamp));

                long timestamp = Long.parseLong(maxTimestamp);
                List<String> newReqIdList = adcsConnector.getRequesIdList(limit, 0, timestamp, 0L);
                if (newReqIdList.isEmpty()) {
                    LOGGER.debug("no certificates retrieved with a resolved timestamp after {} at ca '{}'", new Date(timestamp), info);
                }

                for (String reqId : newReqIdList) {

                    //				LOGGER.debug("certRepository {}, info '{}', reqId {}", certRepository, info, reqId );

                    List<Certificate> certDaoList = certificateRepository.findBySearchTermNamed2(
                        CertificateAttribute.ATTRIBUTE_PROCESSING_CA, info,
                        CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID, reqId);

                    if (certDaoList.isEmpty()) {
                        Certificate certImported = importCertificate(adcsConnector, info, reqId, config);
                        if (certImported == null) {
                            LOGGER.warn("import of certificate with requestID '{}' from ca '{}' failed", reqId, info);
                        } else {
                            LOGGER.info("certificate with requestID '{}' from ca '{}' imported, assigned to cert id {}", reqId, info, certImported.getId());

                            String resolvedTimestampAttribute = certUtil.getCertAttribute(certImported, CertificateAttribute.ATTRIBUTE_CA_RESOLVED_TIMESTAMP);
                            LOGGER.info("certificate with requestID '{}' from ca '{}' set to resolved timestamp {} / {}", reqId, info, resolvedTimestampAttribute, dateFromTimestampString(resolvedTimestampAttribute));

                            nNewCerts++;
                        }
                    } else {
                        LOGGER.info("certificate with requestID '{}' from ca '{}' already present with cert id {}", reqId, info, certDaoList.get(0).getId());
                    }
                }
            }

        } catch (OODBConnectionsADCSException | ADCSProxyUnavailableException oodbc) {
            throw oodbc;
        } catch (ADCSException e) {
            LOGGER.info("importing certificate list by resolved date with a limit of {} causes {}", limit, e.getLocalizedMessage());
            LOGGER.warn("ACDSException : ", e);
        }

        return nNewCerts;
    }

    public int retrieveCertificatesByRevokedDate(CAConnectorConfig config) throws OODBConnectionsADCSException, ADCSProxyUnavailableException {

        LOGGER.debug("in retrieveCertificatesByRevokedDate");

        int nNewRevokedCerts = 0;
        int limit = 100;

        ADCSWinNativeConnector adcsConnector = getConnector(config);

        try {

            String info = adcsConnector.getInfo();


            List<Certificate> certWithoutRevokedList = certificateRepository.findTimestampNotExistForCA(info, CertificateAttribute.ATTRIBUTE_CA_REVOKED_TIMESTAMP);
            if (!certWithoutRevokedList.isEmpty()) {

                // ensure all certificates have a REVOKED_AT timestamp. Should happen in migration scenarios only
                for (int i = 0; i < limit; i++) {
                    try {
                        Certificate certWORevoked = certWithoutRevokedList.get(i);
                        String caProcessingId = certUtil.getCertAttribute(certWORevoked, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID);
                        if (caProcessingId == null) {
                            LOGGER.warn("certificate #{} retrieved for ca '{}' but without CA_PROCESSING_ID", certWORevoked.getId(), info);
                        } else {
                            GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(caProcessingId);
                            certUtil.setCertAttribute(certWORevoked,
                                CertificateAttribute.ATTRIBUTE_CA_REVOKED_TIMESTAMP,
                                CertificateUtil.getPaddedTimestamp(certResponse.getRevokedDate()));

                            LOGGER.warn("added revoked timestamp {} to certificate #{} retrieved for ca '{}' with CA_PROCESSING_ID '{}'",
                                CertificateUtil.getPaddedTimestamp(certResponse.getRevokedDate()),
                                certWORevoked.getId(),
                                info,
                                caProcessingId);
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                        // no size check ... so the exception is no problem
                        // the size of the list be big so there is no real use to ask for it.
                    }
                }
            } else {

                String maxTimestamp = certificateRepository.findMaxTimestampForCA(info, CertificateAttribute.ATTRIBUTE_CA_REVOKED_TIMESTAMP);
                LOGGER.debug("findMaxRevokedForCA at ca '{}' returned {}", info, dateFromTimestampString(maxTimestamp));

                long timestamp = Long.parseLong(maxTimestamp);
                List<String> newReqIdList = adcsConnector.getRequesIdList(limit, 0, 0L, timestamp);
                if (newReqIdList.isEmpty()) {
                    LOGGER.debug("no certificates retrieved with a revoked timestamp after {} at ca '{}'", new Date(timestamp), info);
                }

                for (String reqId : newReqIdList) {

                    List<Certificate> certDaoList = certificateRepository.findBySearchTermNamed2(
                        CertificateAttribute.ATTRIBUTE_PROCESSING_CA, info,
                        CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID, reqId);

                    Certificate certRevoked;
                    if (certDaoList.isEmpty()) {
                        LOGGER.warn("certificate with revocation status to be updated with requestID '{}' from ca '{}' not present in database!", reqId, info);
                        certRevoked = importCertificate(adcsConnector, info, reqId, config);
                        if (certRevoked == null) {
                            LOGGER.warn("import of certificate with requestID '{}' from ca '{}' failed", reqId, info);
                        }
                    } else if (certDaoList.size() == 1) {
                        certRevoked = certDaoList.get(0);
                    } else {
                        LOGGER.warn("retrieved more than one ({}) certificate found for requestID '{}' from ca '{}' in database!", certDaoList.size(), reqId, info);
                        certRevoked = certDaoList.get(0);
                    }

                    if (certRevoked != null) {
                        GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(reqId);

                        boolean alreadyRevoked = certRevoked.isRevoked();
                        String reason = cryptoUtil.crlReasonAsString(cryptoUtil.crlReasonFromString(certResponse.getRevokedReason()));
                        Date revocationDate = new Date(Long.parseLong(certResponse.getRevokedDate()));
                        certUtil.setRevocationStatus(certRevoked, reason, revocationDate);

                        certUtil.setCertAttribute(certRevoked,
                            CertificateAttribute.ATTRIBUTE_CA_REVOKED_TIMESTAMP,
                            CertificateUtil.getPaddedTimestamp(certResponse.getRevokedDate()));

                        if (alreadyRevoked) {
                            LOGGER.info("certificate with requestID '{}' from ca '{}' with cert id {} is already revoked", reqId, info, certRevoked.getId());
                        } else {
                            LOGGER.info("certificate with requestID '{}' from ca '{}' with cert id {} changed to revoked", reqId, info, certRevoked.getId());
                            nNewRevokedCerts++;
                        }
                    }
                }
            }

        } catch (OODBConnectionsADCSException | ADCSProxyUnavailableException oodbc) {
            throw oodbc;
        } catch (ADCSException e) {
            LOGGER.info("importing certificate list by revoked date with a limit of {} causes {}", limit, e.getLocalizedMessage());
            LOGGER.warn("ACDSException : ", e);
        }

        return nNewRevokedCerts;
    }

    private Date dateFromTimestampString(String timestamp) {
        long timestampAsLong = Long.parseLong(timestamp);
        return new Date(timestampAsLong);
    }

    /**
     * Try to retrieve new certificates added since the last call. This method is usually called by a timer.
     * The number of certificates is limited to avoid blocking the calling cron job.
     *
     * @param config the connection data identifying an ADCS instance
     * @return the number in imported certificates
     * @throws OODBConnectionsADCSException  something went wrong
     * @throws ADCSProxyUnavailableException something went wrong, the adcsProxy is unavailable
     */
    @Transactional
    public int retrieveCertificates(CAConnectorConfig config) throws OODBConnectionsADCSException, ADCSProxyUnavailableException {

        return retrieveCertificatesOffsetOnly(config);
/*
		retrieveCertificatesByRevokedDate(config);
		return retrieveCertificatesByResolvedDate(config) ;
*/
    }

    /**
     * retrieve a single certificate content and store it in the internal database
     *
     * @param adcsConnector the current connector
     * @param caName        the textual description of the ADCS CA
     * @param reqId         te ADCS request id of the certificate to be retrieved
     * @param config        the connection data identifying an ADCS instance
     * @throws ADCSException something went wrong
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Certificate importCertificate(ADCSWinNativeConnector adcsConnector,
                                         String caName,
                                         String reqId,
                                         CAConnectorConfig config)
        throws ADCSException {

        GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(reqId);

        if (certResponse.getB64Cert() == null || certResponse.getB64Cert().trim().length() == 0) {
            LOGGER.debug("reqId '{}' has not certificate, yet. Ignoring ...", reqId);
            return null;
        }

        try {

            Certificate certDao = certUtil.getCertificateByPEM(certResponse.getB64Cert());
            if (certDao == null) {
                certDao = certUtil.createCertificate(certResponse.getB64Cert(), null,
                    null, false);
                auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_ADCS_CERTIFICATE_IMPORTED, certDao));
            }

            // in this special of importing we know where to revoke this certificate
            certDao.setRevocationCA(config);

            // @todo : implement more sophisticated strategies
            if (certDao.isSelfsigned()) {
                certDao.setTrusted(true);
            }

            // the Request ID is specific to ADCS instance
            certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_PROCESSING_CA, caName);
            certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID, certResponse.getReqId());
            certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_RESOLVED_TIMESTAMP, CertificateUtil.getPaddedTimestamp(certResponse.getResolvedDate()));

            certificateRepository.save(certDao);

            LOGGER.debug("certificate with reqId '{}' imported from ca '{}'", reqId, caName);

            return certDao;

        } catch (GeneralSecurityException | IOException e) {
            LOGGER.info("retrieving and importing certificate with reqId '{}' from ca '{}' causes {}",
                reqId, caName, e.getLocalizedMessage());

            throw new ADCSException(e.getLocalizedMessage());
        }
    }


}

/**
 * Unify a local and a remote instance of ADCS connector
 *
 * @author kuehn
 */
class ADCSWinNativeConnectorAdapter implements ADCSWinNativeConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(ADCSWinNativeConnectorAdapter.class);

    private RemoteADCSClient remoteClient;
    private byte[] sharedSecret;

    /**
     * @param caUrl
     * @param secret
     * @param ca3sTrustManager
     * @throws GeneralSecurityException
     */
    public ADCSWinNativeConnectorAdapter(String caUrl,
                                         String secret,
                                         TrustManager ca3sTrustManager,
                                         String ca3sSalt,
                                         int iterations,
                                         String apiKeySalt,
                                         int apiKeyIterations) throws GeneralSecurityException {

        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        PBEKeySpec specApiKey = new PBEKeySpec(secret.toCharArray(), apiKeySalt.getBytes(), apiKeyIterations, 256);
        PBEKeySpec specSecKey = new PBEKeySpec(secret.toCharArray(), ca3sSalt.getBytes(), iterations, 256);

        this.sharedSecret = skf.generateSecret(specSecKey).getEncoded();

        String apiKey = Base64.encodeBase64String(skf.generateSecret(specApiKey).getEncoded());

        TrustManager[] trustManagers = {ca3sTrustManager};
        this.remoteClient = new RemoteADCSClient(caUrl, apiKey, trustManagers);

        this.remoteClient.getApiClient().setConnectTimeout(30 * 1000);
        this.remoteClient.getApiClient().setReadTimeout(60 * 1000);

//		LOGGER.debug("secret '{}', sharedSecret '{}', apiKey '{}'", secret, Base64.encodeBase64String(sharedSecret), apiKey);
    }


    @Override
    public CertificateEnrollmentResponse submitRequest(String b64Csr, Map<String, String> attrMap)
        throws ADCSException {

        CertificateRequestElements cre = new CertificateRequestElements();
        cre.setCsr(b64Csr);
        List<CertificateRequestElementsAttributes> attributes = new ArrayList<>();
        for (String key : attrMap.keySet()) {
            CertificateRequestElementsAttributes crea = new CertificateRequestElementsAttributes();
            crea.setName(key);
            crea.setValue(attrMap.get(key));
            attributes.add(crea);
        }
        cre.setAttributes(attributes);


        try {

            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(cre);

//	        LOGGER.debug("calculated secret as ({} bytes) : {} ", sharedSecret.length,  Base64.encodeBase64String(sharedSecret));

            // Create HMAC signer
            JWSSigner signer = new MACSigner(sharedSecret);

            // Prepare JWS object with serialized CertificateRequestElements object as payload
            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(payload));

            // Apply the HMAC
            jwsObject.sign(signer);

            // To serialize to compact form, produces something like
            // eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
            JWSWrappedRequest jwsRequest = new JWSWrappedRequest();
            jwsRequest.setJws(jwsObject.serialize());

            LOGGER.debug("calling ADCSProxy with JWS: " + jwsRequest);
            de.trustable.ca3s.client.model.CertificateEnrollmentResponse response = remoteClient.buildCertificate(jwsRequest);

            CertificateEnrollmentResponse resp = new CertificateEnrollmentResponse();
            resp.setReqId(response.getReqId());
            resp.setStatus(SubmitStatus.valueOf(response.getStatus()));
            resp.setB64Cert(response.getCert());
            resp.setB64CACert(response.getCertCA());
            return resp;

        } catch (ApiException e) {
            if (e.getCode() == 503) {
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            }

            LOGGER.warn("ADCSException : " + e.getCode(), e);
            throw new ADCSException(e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.warn("IOException writing JSON object ", e);
            throw new ADCSException(e.getLocalizedMessage());
        } catch (JOSEException e) {
            LOGGER.warn("JOSEException writing JSON object ", e);
            throw new ADCSException(e.getLocalizedMessage());
        }

    }

    @Override
    public void revokeCertifcate(String serial, int reason, Date revocationDate) throws ADCSException {

        try {

            CertificateRevocationRequest crr = new CertificateRevocationRequest();
            crr.serial(serial);
            crr.setReason(reason);

            crr.setRevTime(revocationDate.getTime());

            ObjectMapper objectMapper = new ObjectMapper();
            String payload = objectMapper.writeValueAsString(crr);

//	        LOGGER.debug("calculated secret as ({} bytes) : {} ", sharedSecret.length,  Base64.encodeBase64String(sharedSecret));

            // Create HMAC signer
            JWSSigner signer = new MACSigner(sharedSecret);

            // Prepare JWS object with serialized CertificateRequestElements object as payload
            JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), new Payload(payload));

            // Apply the HMAC
            jwsObject.sign(signer);

            // To serialize to compact form, produces something like
            // eyJhbGciOiJIUzI1NiJ9.SGVsbG8sIHdvcmxkIQ.onO9Ihudz3WkiauDO2Uhyuz0Y18UASXlSc1eS0NkWyA
            JWSWrappedRequest jwsRequest = new JWSWrappedRequest();
            jwsRequest.setJws(jwsObject.serialize());

            LOGGER.debug("calling ADCSProxy with JWS: " + jwsRequest);

            remoteClient.revokeCertificate(jwsRequest);
        } catch (ApiException e) {
            if (e.getCode() == 503) {
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            }
            LOGGER.warn("ACDSException : " + e.getCode(), e);
            throw new ADCSException(e.getLocalizedMessage());
        } catch (IOException e) {
            LOGGER.warn("IOException writing JSON object ", e);
            throw new ADCSException(e.getLocalizedMessage());
        } catch (JOSEException e) {
            LOGGER.warn("JOSEException writing JSON object ", e);
            throw new ADCSException(e.getLocalizedMessage());
        }
    }

    @Override
    public List<String> getRequesIdList(int limit, int offset, long resolvedWhenTimestamp, long revokedEffectiveWhen)
        throws ADCSException {
        try {
            List<String> rir = remoteClient.getRequestIdList(offset, resolvedWhenTimestamp, revokedEffectiveWhen, limit);
            return rir;
        } catch (ApiException e) {
            if (e.getCode() == 503) {
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            } else if (e.getCause() instanceof SocketTimeoutException) {
                throw new ADCSProxyUnavailableException(e.getCause().getMessage());
            }

            LOGGER.warn("ADCSException : " + e.getCode(), e);
            throw new ADCSException(e.getLocalizedMessage());
        }
    }

    @Override
    public GetCertificateResponse getCertificateByRequestId(String reqId) throws ADCSException {
        try {
            de.trustable.ca3s.client.model.GetCertificateResponse gcr = remoteClient.getRequestById(reqId);
            GetCertificateResponse resp = new GetCertificateResponse();

            if (gcr.getValues() != null) {
                for (GetCertificateResponseValues value : gcr.getValues()) {
                    if ("ReqId".equals(value.getName())) {
                        resp.setReqId(value.getValue());
                    } else if ("Template".equals(value.getName())) {
                        resp.setTemplate(value.getValue());
                    } else if ("Cert".equals(value.getName())) {
                        resp.setB64Cert(value.getValue());
                    } else if ("ResolvedDate".equals(value.getName())) {
                        resp.setResolvedDate(value.getValue());
                    } else if ("RevokedDate".equals(value.getName())) {
                        resp.setRevokedDate(value.getValue());
                    } else if ("RevokedReason".equals(value.getName())) {
                        resp.setRevokedReason(value.getValue());
                    } else if ("Disposition".equals(value.getName())) {
                        resp.setDisposition(value.getValue());
                    } else if ("DispositionMessage".equals(value.getName())) {
                        resp.setDispositionMessage(value.getValue());
                    }
                }
            }
            return resp;
        } catch (ApiException e) {
            if (e.getCode() == 503) {
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            } else if (e.getCause() instanceof SocketTimeoutException) {
                throw new ADCSProxyUnavailableException(e.getCause().getMessage());
            }
            LOGGER.warn("ADCSException : " + e.getCode(), e);
            throw new ADCSException(e.getLocalizedMessage());
        }
    }

    @Override
    public String getInfo() throws ADCSException {
        try {
            return remoteClient.getADCSInfo();
        } catch (ProcessingException pe) {
            if (pe.getCause() instanceof SSLException) {
                LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
                throw new ADCSProxyUnavailableException("connection problem accessing ca : " + pe.getCause().getLocalizedMessage());
            }
            throw pe;
        } catch (ApiException e) {
            if (e.getCause() instanceof ConnectException) {
                LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
                throw new ADCSProxyUnavailableException("connection problem accessing ca : " + e.getCause().getLocalizedMessage());
            }

            if (e.getCause() instanceof ConnectException) {
                LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
                throw new ADCSProxyUnavailableException("connection problem accessing ca : " + e.getCause().getLocalizedMessage());
            }

            if (e.getCode() == 503) {
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            } else if (e.getCode() == 401) {
                LOGGER.info("info call rejected by ADCS proxy. Authentication wrong or missing.");
                throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
            } else if (e.getCause() instanceof SocketTimeoutException) {
                LOGGER.info("info call to ADCS proxy timed out.", e.getCause());
                throw new ADCSProxyUnavailableException(e.getCause().getMessage());
            } else if (e.getCause() instanceof ConnectException) {
                LOGGER.warn("Connection problem", e.getCause());
                throw new ADCSProxyUnavailableException(e.getCause().getMessage());
            } else if (e.getCause() instanceof SSLHandshakeException) {
                String msg = "TLS problem : configure trust anchor for ADCS proxy at " + remoteClient.getApiClient().getBasePath();
                LOGGER.debug(msg, e);
                LOGGER.warn(msg);
                throw new ADCSProxyTLSException(msg);
            }

            LOGGER.warn("ADCSException : " + e.getCode(), e);
            throw new ADCSException(e.getLocalizedMessage());
        }
    }

    @Override
    public String[] getCATemplates() throws ADCSException {
        String[] templateArr = {"FooTemplate"};
        return templateArr;
    }

    @Override
    public ADCSInstanceDetails getCAInstanceDetails() throws ADCSException {
        ADCSInstanceDetails details = new ADCSInstanceDetails();

		try {
			ADCSInstanceDetailsResponse detailsResp = remoteClient.getCAInstanceDetails();

			details.setCaName(detailsResp.getCaName());
			details.setCaType(detailsResp.getCaType());
			details.setDnsName(detailsResp.getDnsName());
			details.setFileVersion(detailsResp.getFileVersion());
			details.setParentCaName(detailsResp.getParentCaName());
			details.setProductVersion(detailsResp.getProductVersion());
			details.setSigningCertChains(fromList(detailsResp.getSigningCertChains()));
			details.setSigningCerts(fromList(detailsResp.getSigningCerts()));

			details.setSubjectTemplateOIDs(fromList(detailsResp.getSubjectTemplateOIDs()));
			details.setTemplates(fromList(detailsResp.getTemplates()));

		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ADCSProxyUnavailableException(e.getLocalizedMessage());
			}else if( e.getCause() instanceof SocketTimeoutException){
				throw new ADCSProxyUnavailableException(e.getCause().getMessage());
			}else if( e.getCause() instanceof ConnectException){
				LOGGER.warn("Connection problem", e );
				throw new ADCSProxyUnavailableException(e.getCause().getMessage());
			}else if( e.getCause() instanceof SSLHandshakeException){
				LOGGER.warn("TLS problem : configure trust anchor for ADCS proxy at " + remoteClient.getApiClient().getBasePath() );
				throw new ADCSProxyTLSException(e.getCause().getMessage());
			}

			LOGGER.warn("ADCSException : " + e.getCode() , e );
			throw new ADCSException(e.getLocalizedMessage());
		}

        return details;
    }

    String[] fromList(List<String> list) {
        String[] retArr = new String[list.size()];
        return list.toArray(retArr);
    }
}

/**
 * dummy implementation just telling it's not a valid connector
 *
 * @author kuehn
 */
class EmptyADCSWinNativeConnectorAdapter implements ADCSWinNativeConnector {

    Logger logger = LoggerFactory.getLogger(EmptyADCSWinNativeConnectorAdapter.class);

    @Override
    public CertificateEnrollmentResponse submitRequest(String b64Csr, Map<String, String> attrMap) throws ADCSException {
        throw new WinClassesUnavailableException();
    }

    @Override
    public void revokeCertifcate(String serial, int reason, Date revocationDate) throws ADCSException {
        throw new WinClassesUnavailableException();
    }

    @Override
    public GetCertificateResponse getCertificateByRequestId(String reqId) throws ADCSException {
        throw new WinClassesUnavailableException();
    }

    @Override
    public String getInfo() throws ADCSException {
        logger.debug("calling 'getInfo()' in a dummy adapter instance");
        return "EmptyADCSWinNativeConnectorAdapter";
//		throw new WinClassesUnavailableException();
    }

    @Override
    public List<String> getRequesIdList(int limit, int offset, long resolvedWhenTimestamp, long revokedEffectiveWhen)
        throws ADCSException {
        throw new WinClassesUnavailableException();
    }

    @Override
    public String[] getCATemplates() throws ADCSException {
        String[] templateArr = {"FooTemplate"};
        return templateArr;
    }

    @Override
    public ADCSInstanceDetails getCAInstanceDetails() throws ADCSException {
        throw new WinClassesUnavailableException();
    }
}

