package de.trustable.ca3s.core.service.cmp;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CaConnectorConfigUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.cmp.client.ProtectedMessageHandler;
import de.trustable.cmp.client.cmpClient.CMPClientConfig;
import de.trustable.cmp.client.cmpClient.CMPClientImpl;
import de.trustable.cmp.client.cmpClient.DigestSigner;
import de.trustable.cmp.client.cmpClient.KeystoreSigner;
import de.trustable.util.CryptoUtil;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;

@Service
public class CaCmpConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaCmpConnector.class);

	private final RemoteConnector remoteConnector;

	private final CryptoUtil cryptoUtil;

	private final CertificateUtil certUtil;

	private final CSRUtil csrUtil;

	private final ProtectedContentUtil protUtil;

    private final CaConnectorConfigUtil caConnectorConfigUtil;

	private final CertificateRepository certificateRepository;

    /**
     * @param remoteConnector
     * @param cryptoUtil
     * @param certUtil
     * @param csrUtil
     * @param protUtil
     * @param certificateRepository
     * @param caConnectorConfigUtil
     */
	public CaCmpConnector(RemoteConnector remoteConnector,
                          CryptoUtil cryptoUtil,
                          CertificateUtil certUtil,
                          CSRUtil csrUtil,
                          ProtectedContentUtil protUtil,
                          CertificateRepository certificateRepository,
                          CaConnectorConfigUtil caConnectorConfigUtil) {

        this.remoteConnector = remoteConnector;
        this.cryptoUtil = cryptoUtil;
        this.certUtil = certUtil;
        this.csrUtil = csrUtil;
        this.protUtil = protUtil;
        this.certificateRepository = certificateRepository;
        this.caConnectorConfigUtil = caConnectorConfigUtil;
	}

    private CMPClientImpl getCMPClient(CAConnectorConfig caConnConfig) throws GeneralSecurityException {

        CMPClientConfig cmpClientConfig = new CMPClientConfig();

        Certificate certificateMessageProtection = caConnConfig.getMessageProtection();

        ProtectedMessageHandler signer;
        if (certificateMessageProtection == null) {
            LOGGER.debug("CMPClientConfig: instantiating DigestSigner");
            signer = new DigestSigner(protUtil.unprotectString(caConnConfig.getSecret().getContentBase64()));
        } else {
            LOGGER.debug("CMPClientConfig: instantiating KeystoreSigner");
            try {
                CertificateUtil.KeyStoreAndPassphrase keyStoreAndPassphrase =
                    certUtil.getContainer(certificateMessageProtection,
                        "entryAlias",
                        "passphraseChars".toCharArray(),
                        "PBEWithHmacSHA256AndAES_256");

                signer = new KeystoreSigner(keyStoreAndPassphrase.getKeyStore(),
                    "entryAlias",
                    new String(keyStoreAndPassphrase.getPassphraseChars()));
            } catch (IOException e) {
                throw new GeneralSecurityException("Problem building P12 container", e);
            }
        }

        cmpClientConfig.setMessageHandler(signer);

        Certificate certificateTlsAuthentication = caConnConfig.getTlsAuthentication();
        if (certificateTlsAuthentication != null) {
            LOGGER.debug("CMPClientConfig: using CertificateTlsAuthentication");

            try {
                CertificateUtil.KeyStoreAndPassphrase keyStoreAndPassphrase =
                    certUtil.getContainer(certificateTlsAuthentication,
                        "entryAlias",
                        "passphraseChars".toCharArray(),
                        "PBEWithHmacSHA256AndAES_256");

                cmpClientConfig.setP12ClientStore(keyStoreAndPassphrase.getKeyStore());
                cmpClientConfig.setP12ClientSecret(new String(keyStoreAndPassphrase.getPassphraseChars()));
            } catch (IOException e) {
                throw new GeneralSecurityException("Problem build P12 container", e);
            }

        }

        cmpClientConfig.setRemoteTargetHandler(remoteConnector);
        cmpClientConfig.setCaUrl(caConnConfig.getCaUrl());
        LOGGER.debug("CMPClientConfig: CaUrl '{}'", cmpClientConfig.getCaUrl());

        String contenType = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_CMP_MESSAGE_CONTENT_TYPE, "application/pkixcmp");
        cmpClientConfig.setMsgContentType(contenType);
        LOGGER.debug("CMPClientConfig: MsgContentType '{}'", cmpClientConfig.getMsgContentType());

        cmpClientConfig.setCmpAlias(caConnConfig.getSelector());
        LOGGER.debug("CMPClientConfig: CmpAlias '{}'", cmpClientConfig.getCmpAlias());

        String certIssuer = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_ISSUER_NAME, null);
        if (certIssuer != null && !certIssuer.trim().isEmpty()) {
            cmpClientConfig.setIssuerName(new X500Name(certIssuer));
            LOGGER.debug("CMPClientConfig: IssuerName '{}'", cmpClientConfig.getIssuerName());
        }

        String sni = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_SNI, null);
        cmpClientConfig.setSni(sni);
        LOGGER.debug("CMPClientConfig: SNI '{}'", cmpClientConfig.getSni());

        boolean disableHostNameVerifier = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_DISABLE_HOST_NAME_VERIFIER, true);
        cmpClientConfig.setDisableHostNameVerifier(disableHostNameVerifier);
        LOGGER.debug("CMPClientConfig: DisableHostNameVerifier '{}'", cmpClientConfig.isDisableHostNameVerifier());

        boolean multipleMessages = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_MULTIPLE_MESSAGES, true);
        cmpClientConfig.setMultipleMessages(multipleMessages);
        LOGGER.debug("CMPClientConfig: MultipleMessages '{}'", cmpClientConfig.isMultipleMessages());

        boolean implicitConfirm = caConnectorConfigUtil.getCAConnectorConfigAttribute(caConnConfig, CaConnectorConfigUtil.ATT_IMPLICIT_CONFIRM, true);
        cmpClientConfig.setImplicitConfirm(implicitConfirm);
        LOGGER.debug("CMPClientConfig: ImplicitConfirm '{}'", cmpClientConfig.isImplicitConfirm());

        cmpClientConfig.setVerbose(LOGGER.isDebugEnabled());

        return new CMPClientImpl(cmpClientConfig);
    }

	/**
	 *
	 * @param csr			csr as CSR object
	 * @param caConnConfig	CAConnectorConfig
	 *
	 * @return the created certificate, pem encoded
	 *
	 * @throws GeneralSecurityException something went wrong, e.g. no CSM format
	 */
    public de.trustable.ca3s.core.domain.Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConnConfig)
        throws GeneralSecurityException {

        LOGGER.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());

        CMPClientImpl cmpClient = getCMPClient(caConnConfig);

        ByteArrayInputStream baisCsr = new ByteArrayInputStream(csr.getCsrBase64().getBytes());
        CMPClientImpl.CertificateResponseContent certificateResponseContent = cmpClient.signCertificateRequest(baisCsr);

        de.trustable.ca3s.core.domain.Certificate cert = readCertResponse(certificateResponseContent,
            csr,
            caConnConfig);

        csr.setCertificate(cert);
        csr.setStatus(CsrStatus.ISSUED);

        return cert;
    }

	/**
	 *
	 * @param certDao
	 * @param crlReason
	 * @param revocationDate
	 * @param caConnConfig
	 * @throws GeneralSecurityException
	 */
	public void revokeCertificate(Certificate certDao, final CRLReason crlReason, final Date revocationDate,
			CAConnectorConfig caConnConfig) throws GeneralSecurityException {

		revokeCertificate(new X500Name(certDao.getIssuer()), new X500Name(certDao.getSubject()),
				new BigInteger(certDao.getSerial()), crlReason, caConnConfig);
	}

	/**
	 *
	 * @param issuerDN
	 * @param subjectDN
	 * @param serial
	 * @param crlReason
	 * @param caConnConfig
	 *
	 * @throws GeneralSecurityException
	 */
	public void revokeCertificate(final X500Name issuerDN, final X500Name subjectDN, final BigInteger serial,
			final CRLReason crlReason, CAConnectorConfig caConnConfig )
			throws GeneralSecurityException {


        CMPClientImpl cmpClient = getCMPClient(caConnConfig);

        cmpClient.revokeCertificate(issuerDN, subjectDN, serial, crlReason);
	}


	/**
	 *
	 * @param caConnConfig
	 * @return
	 */
	public CAStatus getStatus(final CAConnectorConfig caConnConfig) {

        return CAStatus.Active;
/*
		try {
			if( caConnConfig.getSecret() == null) {
				LOGGER.error("CMP instance requires 'secret' to be present");
				return CAStatus.Deactivated;
			}

			String plainSecret = protUtil.unprotectString( caConnConfig.getSecret().getContentBase64());
			GenMsgContent infoContent = getGeneralInfo(plainSecret, caConnConfig.getCaUrl(), caConnConfig.getSelector());

			InfoTypeAndValue[] infoTypeAndValueArr = infoContent.toInfoTypeAndValueArray();

			for (InfoTypeAndValue infoTypeAndValue : infoTypeAndValueArr) {
				LOGGER.debug("CMP instance {} returns {}: {}", caConnConfig.getName(), infoTypeAndValue.getInfoType().getId(), infoTypeAndValue.getInfoValue().toString());
			}
			return CAStatus.Active;

		} catch( UnrecoverableEntryException ree ) {
			// the CA responded with a proper CMP message but does not support the 'Status' request
			return CAStatus.Active;

		} catch( GeneralSecurityException gse) {
            if( LOGGER.isDebugEnabled()){
                LOGGER.error("status call to CMP instance '" + caConnConfig.getName() + "' failed", gse);
            }else {
                LOGGER.error("status call to CMP instance '" + caConnConfig.getName() + "' failed: " +  gse.getMessage());
            }
		}

		return CAStatus.Deactivated;

 */
	}

	/**
	 *
	 *
	public GenMsgContent getGeneralInfo(String hmacSecret, String cmpEndpoint, String alias)
			throws GeneralSecurityException {

		try {

			PKIMessage pkiMessage = cryptoUtil.buildGeneralMessageRequest(hmacSecret);

			// send and receive ..
			LOGGER.debug("general info requestBytes : "
					+ java.util.Base64.getEncoder().encodeToString(pkiMessage.getEncoded()));
			byte[] responseBytes = remoteConnector.sendHttpReq(cmpEndpoint + "/" + alias, pkiMessage.getEncoded());
			LOGGER.debug("general info responseBytes : " + java.util.Base64.getEncoder().encodeToString(responseBytes));

			// handle the response
            return cryptoUtil.readGenMsgResponse(responseBytes, hmacSecret);

		} catch (CRMFException e) {
			LOGGER.info("CMS format problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (CMPException e) {
			LOGGER.info("CMP problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (IOException e) {
		    if( LOGGER.isDebugEnabled()){
                LOGGER.debug("IO / encoding problem", e);
            }else {
                LOGGER.info("IO / encoding problem: {}", e.getMessage());
            }
			throw new GeneralSecurityException(e.getMessage());
		}
	}
*/

    public de.trustable.ca3s.core.domain.Certificate readCertResponse(final CMPClientImpl.CertificateResponseContent certificateResponseContent,
                                                                      final CSR csr,
                                                                      final CAConnectorConfig config)
        throws GeneralSecurityException {

        handleExtraCerts(certificateResponseContent.getAdditionalCertificates());

        if (certificateResponseContent.getCreatedCertificate() == null) {

            csrUtil.setStatus(csr, CsrStatus.REJECTED);
            csrUtil.setCsrAttribute(csr, CsrAttribute.ATTRIBUTE_FAILURE_INFO, certificateResponseContent.getMessage(), true);

            throw new GeneralSecurityException(
                "CMP response contains no certificate, \n" + certificateResponseContent.getMessage());
        }


        de.trustable.ca3s.core.domain.Certificate certDao =
            certUtil.createCertificate(certificateResponseContent.getCreatedCertificate().getEncoded(),
                csr, null, false);
        certDao.setRevocationCA(config);
        certificateRepository.save(certDao);

        return certDao;
    }

    private void handleExtraCerts(final Set<X509Certificate> certSet) throws GeneralSecurityException {
        if( certSet == null){
            // no additional certs
            return;
        }

        for( X509Certificate certificate: certSet){

            Certificate certDao = certUtil.createCertificate(certificate.getEncoded(),
                null, null, true);
            certificateRepository.save(certDao);

            LOGGER.debug("Additional cert '" + certDao.getSubject() + "' from CMP response");
        }
    }
}
