package de.trustable.ca3s.core.service.adcs;


import java.io.IOException;
import java.math.BigInteger;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;

import de.trustable.ca3s.adcsCertUtil.ACDSException;
import de.trustable.ca3s.adcsCertUtil.ACDSProxyTLSException;
import de.trustable.ca3s.adcsCertUtil.ACDSProxyUnavailableException;
import de.trustable.ca3s.adcsCertUtil.ADCSNativeImpl;
import de.trustable.ca3s.adcsCertUtil.ADCSWinNativeConnector;
import de.trustable.ca3s.adcsCertUtil.CertificateEnrollmentResponse;
import de.trustable.ca3s.adcsCertUtil.GetCertificateResponse;
import de.trustable.ca3s.adcsCertUtil.NoLocalACDSException;
import de.trustable.ca3s.adcsCertUtil.OODBConnectionsACDSException;
import de.trustable.ca3s.adcsCertUtil.SubmitStatus;
import de.trustable.ca3s.adcsCertUtil.WinClassesUnavailableException;
import de.trustable.ca3s.client.api.RemoteADCSClient;
import de.trustable.ca3s.client.model.CertificateRequestElements;
import de.trustable.ca3s.client.model.CertificateRequestElementsAttributes;
import de.trustable.ca3s.client.model.CertificateRevocationRequest;
import de.trustable.ca3s.client.model.GetCertificateResponseValues;
import de.trustable.ca3s.client.model.JWSWrappedRequest;
import de.trustable.ca3s.client.model.RequestIdsResponse;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.security.provider.Ca3sTrustManager;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import io.swagger.client.ApiException;

@Service
public class ADCSConnector {


	Logger LOGGER = LoggerFactory.getLogger(ADCSConnector.class);

	@Autowired
	private CryptoService cryptoUtil;

	@Autowired
	CertificateUtil certUtil;
	
	@Autowired
	private Ca3sTrustManager ca3sTrustManager;
	
	@Autowired
	CSRRepository csrRepository;

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private ProtectedContentUtil protUtil;
	

	/**
	 * Adapter class to connect to an ADCS server using the parameter given in a CaConnectorConfig
	 */
	public ADCSConnector() {

	}

	ADCSWinNativeConnector getConnector(CAConnectorConfig config) throws ACDSProxyUnavailableException {
		
		LOGGER.debug("connector '"+config.getName()+"', Url configured as '" + config.getCaUrl() + "'");
		if( "inProcess".equalsIgnoreCase(config.getCaUrl()) ) {
			LOGGER.debug("ADCSConnector trying to load Windows connection classes...");
			try {
				return new ADCSNativeImpl();
			} catch (UnsatisfiedLinkError ule) {
				LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.");
			} catch (ACDSException e) {
				LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.", e);
			}
		}else {

			if( config.getSecret() == null) {
				throw new ACDSProxyUnavailableException("passphrase missing in ca configuration for ca '" + config.getName() + "' !");
			}
			
			String plainSecret = protUtil.unprotectString( config.getSecret().getContentBase64());

			RemoteADCSClient rc = new RemoteADCSClient(config.getCaUrl());

			rc.getApiClient().setConnectTimeout(30 * 1000);
			rc.getApiClient().setReadTimeout(60 * 1000);

			TrustManager[] managers = {ca3sTrustManager};
			rc.getApiClient().setTrustManagers(managers);
			
			try {
				ADCSWinNativeConnector adcsConnector = new ADCSWinNativeConnectorAdapter(rc, plainSecret);
				LOGGER.debug("ADCSConnector trying to connect to remote ADCS proxy ...");
				String info = adcsConnector.getInfo();
				LOGGER.debug("info call returns '{}'", info);
				
				return adcsConnector;
			} catch (ACDSProxyUnavailableException pue) {
				LOGGER.info("info call for ADCS proxy did not succeeded! Trying later ...");
				throw pue;
			} catch (ACDSException | GeneralSecurityException e) {
				LOGGER.warn("info call failed", e);
			}
		}
		
		return new EmptyADCSWinNativeConnectorAdapter();
	}
	
	/**
	 * Retrieve the current status of the ADCSProxy
	 * 
	 * @param caConfig set of configuration items
	 * 
	 * @return current status
	 */
	public CAStatus getStatus(final CAConnectorConfig caConfig) {
	
		try {
			String adcsStatus = getConnector(caConfig).getInfo();
			if((adcsStatus != null) && (adcsStatus.trim().length() > 0)) {
				return CAStatus.Active;
			}
		} catch (ACDSException adcsEx) {
			LOGGER.debug("CAConnectorType ADCS at " + caConfig.getCaUrl() + " throws Exception: {} ", adcsEx.getLocalizedMessage());
		}

		return CAStatus.Deactivated;

	}
	
	/**
	 * Send a csr object to the ADCS and retrieve a created certificate
	 * 
	 * @param csr the CSR object, not just a P10 PEM string, holding e.g. a CRS status
	 * 
	 * @return the freshly created certificate, already stored in the database
	 * 
	 * @throws GeneralSecurityException something went wrong, e.g. a rejection of the CSR. The status of the CSR is updated accordingly.
	 */
	public Certificate signCertificateRequest(CSR csr, CAConnectorConfig config) throws GeneralSecurityException {

		LOGGER.debug("incoming csr for ADCS");

		Set<CsrAttribute> csrAttrs = csr.getCsrAttributes();
		
		csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_STARTED_TIMESTAMP,"" + System.currentTimeMillis()));

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

			Map<String, String> attrMap = new HashMap<String, String>();
			
			String template = config.getSelector();
			if( (template != null) && (template.trim().length() > 0) ) {
				LOGGER.debug("requesting certificate using template : " + template );
				attrMap.put("Certificate Template", template);
			}else {
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

				csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP,"" + System.currentTimeMillis()));
				csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID,"" + certResponse.getReqId()));

			} else if ((SubmitStatus.DENIED.equals(certResponse.getStatus()))
					|| (SubmitStatus.INCOMPLETE.equals(certResponse.getStatus()))
					|| (SubmitStatus.ERROR.equals(certResponse.getStatus()))) {

				csr.setStatus(CsrStatus.REJECTED);
				
				csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP,"" + System.currentTimeMillis()));
				csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID,"" + certResponse.getReqId()));

				csr.setCsrAttributes(csrAttrs);
				csrRepository.save(csr);

				throw new GeneralSecurityException("adcs rejected request");

			} else if ((SubmitStatus.UNDER_SUBMISSION.equals(certResponse.getStatus()))
					|| (SubmitStatus.ISSUED_OUT_OF_BAND.equals(certResponse.getStatus()))) {
				csr.setStatus(CsrStatus.PENDING);
				
				csrAttrs.add(createCsrAttribute(csr,CsrAttribute.ATTRIBUTE_CA_PROCESSING_ID,"" + certResponse.getReqId()));

			} else {
				throw new GeneralSecurityException(
						"adcs connector returned non-positive status '" + certResponse.getStatus() + "'");
			}

		} catch (NoLocalACDSException noLocalAdcsEx) {
			// no local ADCS available ...
			// reset the state back to pending
			csr.setStatus(CsrStatus.PENDING);

			throw new GeneralSecurityException("no local adcs connector available", noLocalAdcsEx);

		} catch (ACDSException adcsEx) {
			// no local ADCS available ...
			// reset the state back to pending
			csr.setStatus(CsrStatus.PENDING);
			
			throw new GeneralSecurityException("adcs connector returned exception", adcsEx);

		} catch (IOException ioex) {
			// presumably a connection problem, reset the state back to pending
			csr.setStatus(CsrStatus.PENDING);
			
			throw new GeneralSecurityException("adcs connector caused IOException", ioex);
			
		}finally {
			csr.setCsrAttributes(csrAttrs);
			csrRepository.save(csr);
		}
		
		LOGGER.debug("returning certDao : " + certDao.getId());

		return (certDao);

	}

	/**
	 * @param csr
	 * @return
	 */
	private CsrAttribute createCsrAttribute(CSR csr, final String name, final String value) {
		
		CsrAttribute csrAttr = new CsrAttribute();
		csrAttr.setCsr(csr);
		csrAttr.setName(name);
		csrAttr.setValue(value);
		return csrAttr;
	}

	/**
	 * Revoke a given certificate created by the ADCS server identified by connector config
	 * 
	 * @param certDao the certificate object to be revoked
	 * @param crlReason the revocation reason
	 * @param revocationDate the revocation date
	 * @param config the connection data identifying an ADCS instance
	 * 
	 * @throws GeneralSecurityException
	 */
	public void revokeCertificate(Certificate certDao, final CRLReason crlReason, final Date revocationDate, CAConnectorConfig config)
			throws GeneralSecurityException {

		try {
			BigInteger serial = new BigInteger(certDao.getSerial(), 10);
			String serialAsHex = serial.toString(16);
			LOGGER.debug("revoking certificate {} with serial '{}' with reason {}", certDao.getId(), serialAsHex, crlReason.getValue());
			
			getConnector(config).revokeCertifcate(serialAsHex, crlReason.getValue().intValue(), revocationDate);

		} catch (ACDSException adcsEx) {
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
	 * 
	 * @return the number in imported certificates
	 * 
	 * @throws OODBConnectionsACDSException
	 * @throws ACDSProxyUnavailableException
	 */
	public int retrieveCertificatesOffsetOnly(CAConnectorConfig config) throws OODBConnectionsACDSException, ACDSProxyUnavailableException {

		LOGGER.debug("in retrieveCertificates");

		int limit = 100;

		int pollingOffset = config.getPollingOffset();
		
		ADCSWinNativeConnector adcsConnector = getConnector(config);

		try {
			
			String info = adcsConnector.getInfo();

			List<String> newReqIdList = adcsConnector.getRequesIdList(pollingOffset, limit);
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

		} catch (OODBConnectionsACDSException oodbc) {
			throw oodbc;
		} catch (ACDSProxyUnavailableException pue) {
			throw pue;
		} catch (ACDSException e) {
			LOGGER.info("polling certificate list starting from {} with a limit of {} causes {}", pollingOffset,
					limit, e.getLocalizedMessage());
			LOGGER.warn("ACDSException : ", e);
		}

		int nNewCerts = (pollingOffset - config.getPollingOffset());
		
		if( nNewCerts > 0 ) {
			config.setPollingOffset(pollingOffset);
		}

		return nNewCerts;
	}


	/**
	 * Try to retrieve new certificates added since the last call. This method is usually called by a timer.
	 * A chunk of certificates starting with a given offset will be requested. If there are new certificates available (with a ADCS request id greater than the offset)
	 * the content of these new certificates will be retrieved in distinct calls and stored in the internal database. The highest request ID will be stored as starting 
	 * offset for subsequent calls. 
	 * The number of certificates is limited to avoid blocking the calling cron job.  
	 *  
	 * @param config the connection data identifying an ADCS instance
	 * 
	 * @return the number in imported certificates
	 * 
	 * @throws OODBConnectionsACDSException
	 * @throws ACDSProxyUnavailableException
	 */
	public int retrieveCertificates(CAConnectorConfig config) throws OODBConnectionsACDSException, ACDSProxyUnavailableException {

		LOGGER.debug("in retrieveCertificates by 'resolvedWhen'");

		int limit = 100;

		int pollingOffset = config.getPollingOffset();
		
		ADCSWinNativeConnector adcsConnector = getConnector(config);

		try {
			
			String info = adcsConnector.getInfo();

			List<String> newReqIdList = adcsConnector.getRequesIdList(pollingOffset, limit);
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

		} catch (OODBConnectionsACDSException oodbc) {
			throw oodbc;
		} catch (ACDSProxyUnavailableException pue) {
			throw pue;
		} catch (ACDSException e) {
			LOGGER.info("polling certificate list starting from {} with a limit of {} causes {}", pollingOffset,
					limit, e.getLocalizedMessage());
			LOGGER.warn("ACDSException : ", e);
		}

		int nNewCerts = (pollingOffset - config.getPollingOffset());
		
		if( nNewCerts > 0 ) {
			config.setPollingOffset(pollingOffset);
		}

		return nNewCerts;
	}

	/**
	 * retrieve a single certificate content and store it in the internal database
	 * 
	 * @param adcsConnector the current connector
	 * @param caName the textual description of the ADCS CA
	 * @param reqId te ADCS request id of the certificate to be retrieved
	 * @param config the connection data identifying an ADCS instance
	 * 
	 * @throws ACDSException
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	private void importCertificate(ADCSWinNativeConnector adcsConnector, String caName, String reqId, CAConnectorConfig config)
			throws ACDSException {
		
		GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(reqId);

		try {
			Certificate certDao = certUtil.createCertificate(certResponse.getB64Cert(), null,
					null, false);

			// in this special of importing we know where to revoke this certificate
			certDao.setRevocationCA(config);
			
			// @todo : implement more sophisticated strategies
			if( certDao.isSelfsigned()) {
				certDao.setTrusted(true);
			}
			
			// the Request ID is specific to ADCS instance
			certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_PROCESSING_CA, caName);
			certUtil.setCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID, certResponse.getReqId());
			
			certificateRepository.save(certDao);

			LOGGER.debug("certificate with reqId '{}' imported from ca '{}'", reqId, caName);

		} catch (GeneralSecurityException | IOException e) {
			LOGGER.info("retrieving and importing certificate with reqId '{}' from ca '{}' causes {}",
					reqId, caName, e.getLocalizedMessage());
			
			throw new ACDSException(e.getLocalizedMessage());
		}
	}


}

/**
 * Unify a local and a remote instance of ADCS connector
 * 
 * @author kuehn
 *
 */
class ADCSWinNativeConnectorAdapter implements ADCSWinNativeConnector {

	  private static final Logger LOGGER = LoggerFactory.getLogger(ADCSWinNativeConnectorAdapter.class);

	RemoteADCSClient remoteClient;
	byte[] sharedSecret;

	/**
	 * 
	 * @param remoteClient
	 * @param secret
	 * @throws GeneralSecurityException
	 */
	public ADCSWinNativeConnectorAdapter(RemoteADCSClient remoteClient, String secret) throws GeneralSecurityException {
		this.remoteClient = remoteClient;
		
    	int iterations = 4567;
        byte[] salt = "ca3sSalt".getBytes();
 
        PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), salt, iterations, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        sharedSecret = skf.generateSecret(spec).getEncoded();
	}

	@Override
	public CertificateEnrollmentResponse submitRequest(String b64Csr, Map<String, String> attrMap)
			throws ACDSException {

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
			
			LOGGER.debug("calling ADCSProxy with JWS: " + jwsRequest );
			de.trustable.ca3s.client.model.CertificateEnrollmentResponse response = remoteClient.buildCertificate(jwsRequest);
			
			CertificateEnrollmentResponse resp = new CertificateEnrollmentResponse();
			resp.setReqId(response.getReqId());
			resp.setStatus(SubmitStatus.valueOf(response.getStatus()));
			resp.setB64Cert(response.getCert());
			resp.setB64CACert(response.getCertCA());
			return resp;
			
		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ACDSProxyUnavailableException(e.getLocalizedMessage());
			}
			
			LOGGER.warn("ACDSException : " + e.getCode() , e );
			throw new ACDSException(e.getLocalizedMessage());
		} catch (IOException e) {
			LOGGER.warn("IOException writing JSON object ", e );
			throw new ACDSException(e.getLocalizedMessage());
		} catch (JOSEException e) {
			LOGGER.warn("JOSEException writing JSON object ", e );
			throw new ACDSException(e.getLocalizedMessage());
		}

	}

	@Override
	public void revokeCertifcate(String serial, int reason, Date revocationDate) throws ACDSException {
		
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
			
			LOGGER.debug("calling ADCSProxy with JWS: " + jwsRequest );
			
			remoteClient.revokeCertificate(jwsRequest);
		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ACDSProxyUnavailableException(e.getLocalizedMessage());
			}
			LOGGER.warn("ACDSException : " + e.getCode() , e );
			throw new ACDSException(e.getLocalizedMessage());	
		} catch (IOException e) {
			LOGGER.warn("IOException writing JSON object ", e );
			throw new ACDSException(e.getLocalizedMessage());
		} catch (JOSEException e) {
			LOGGER.warn("JOSEException writing JSON object ", e );
			throw new ACDSException(e.getLocalizedMessage());
		}
	}

	@Override
	public List<String> getRequesIdList(int offset, int limit) throws ACDSException {
		try {
			RequestIdsResponse rir = remoteClient.getRequestIdList(offset, limit);
			return rir;
		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ACDSProxyUnavailableException(e.getLocalizedMessage());
			}else if( e.getCause() instanceof SocketTimeoutException){
				throw new ACDSProxyUnavailableException(e.getCause().getMessage());
			}
			
			LOGGER.warn("ACDSException : " + e.getCode() , e );
			throw new ACDSException(e.getLocalizedMessage());
		}
	}

	@Override
	public GetCertificateResponse getCertificateByRequestId(String reqId) throws ACDSException {
		try {
			de.trustable.ca3s.client.model.GetCertificateResponse gcr = remoteClient.getRequestById(reqId);
			GetCertificateResponse resp = new GetCertificateResponse();
			
			for( GetCertificateResponseValues value: gcr.getValues()) {
				if( "ReqId".equals(value.getName())){
					resp.setReqId(value.getValue());
				} else if( "Template".equals(value.getName())){
					resp.setTemplate(value.getValue());
				} else if( "Cert".equals(value.getName())){
					resp.setB64Cert(value.getValue());
				}
			}
			return resp;
		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ACDSProxyUnavailableException(e.getLocalizedMessage());
			}else if( e.getCause() instanceof SocketTimeoutException){
				throw new ACDSProxyUnavailableException(e.getCause().getMessage());
			}
			LOGGER.warn("ACDSException : " + e.getCode() , e );
			throw new ACDSException(e.getLocalizedMessage());
		}
	}

	@Override
	public String getInfo() throws ACDSException {
		try {
			String info = remoteClient.getADCSInfo();
			return info;
		} catch (ApiException e) {
			if( e.getCode() == 503) {
				throw new ACDSProxyUnavailableException(e.getLocalizedMessage());
			}else if( e.getCause() instanceof SocketTimeoutException){
				throw new ACDSProxyUnavailableException(e.getCause().getMessage());
			}else if( e.getCause() instanceof ConnectException){
				throw new ACDSProxyUnavailableException(e.getCause().getMessage());
			}else if( e.getCause() instanceof SSLHandshakeException){
				LOGGER.warn("TLS problem : configure trust anchor for ADCS proxy at " + remoteClient.getApiClient().getBasePath() );
				throw new ACDSProxyTLSException(e.getCause().getMessage());
			}
			
			LOGGER.warn("ACDSException : " + e.getCode() , e );
			throw new ACDSException(e.getLocalizedMessage());
		}
	}
}

/**
 * dummy implementation just telling it's not a valid connector
 *  
 * @author kuehn
 *
 */
class EmptyADCSWinNativeConnectorAdapter implements ADCSWinNativeConnector {

	Logger logger = LoggerFactory.getLogger(EmptyADCSWinNativeConnectorAdapter.class);

	@Override
	public CertificateEnrollmentResponse submitRequest(String b64Csr, Map<String, String> attrMap ) throws ACDSException {
		throw new WinClassesUnavailableException();
	}

	@Override
	public void revokeCertifcate(String serial, int reason, Date revocationDate) throws ACDSException {
		throw new WinClassesUnavailableException();
	}

	@Override
	public List<String> getRequesIdList(int offset, int limit) throws ACDSException {
		throw new WinClassesUnavailableException();
	}

	@Override
	public GetCertificateResponse getCertificateByRequestId(String reqId) throws ACDSException {
		throw new WinClassesUnavailableException();
	}

	@Override
	public String getInfo() throws ACDSException {
		logger.debug("calling 'getInfo()' in a dummy adapter instance");
		return "EmptyADCSWinNativeConnectorAdapter";
//		throw new WinClassesUnavailableException();
	}
};

