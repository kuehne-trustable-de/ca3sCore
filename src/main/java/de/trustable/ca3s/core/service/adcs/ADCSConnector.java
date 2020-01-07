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
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZoneId;

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
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
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

	/**
	 * 
	 */
	public ADCSConnector() {

	}

	ADCSWinNativeConnector getConnector(CAConnectorConfig adcsDao) throws ACDSProxyUnavailableException {
		
		LOGGER.info("connector '"+adcsDao.getName()+"', Url configured as '" + adcsDao.getCaUrl() + "'");
		if( "inProcess".equalsIgnoreCase(adcsDao.getCaUrl()) ) {
			LOGGER.info("ADCSConnector trying to load Windows connection classes...");
			try {
				return new ADCSNativeImpl();
			} catch (UnsatisfiedLinkError ule) {
				LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.");
			} catch (ACDSException e) {
				LOGGER.info("unable to load Windows connection classes, ADCS connection unavailable.", e);
			}
		}else {

			RemoteADCSClient rc = new RemoteADCSClient(adcsDao.getCaUrl(), adcsDao.getSecret());

			rc.getApiClient().setConnectTimeout(30 * 1000);
			rc.getApiClient().setReadTimeout(60 * 1000);

			TrustManager[] managers = {ca3sTrustManager};
			rc.getApiClient().setTrustManagers(managers);
			
			try {
				ADCSWinNativeConnector adcsConnector = new ADCSWinNativeConnectorAdapter(rc, adcsDao.getSecret());
				LOGGER.info("ADCSConnector trying to connect to remote ADCS proxy ...");

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
	 * 
	 * @param caConfig
	 * @return
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
	 * 
	 * @param csr
	 * @return
	 * @throws GeneralSecurityException
	 */
	public Certificate signCertificateRequest(CSR csr, CAConnectorConfig adcsDao) throws GeneralSecurityException {

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

			CertificateEnrollmentResponse certResponse = getConnector(adcsDao).submitRequest(normalizedCsrString, attrMap);

			if (SubmitStatus.ISSUED.equals(certResponse.getStatus())) {

				if ((certResponse.getB64CACert() != null) && !certResponse.getB64CACert().trim().isEmpty()) {
					// install CA cert if not already known ...
					Certificate certCADao = certUtil.createCertificate(certResponse.getB64CACert(), null, null);
					certificateRepository.save(certCADao);
				}

				// handle response
				certDao = certUtil.createCertificate(certResponse.getB64Cert(), csr, null);

				// the Request ID is specific to ADCS
				certUtil.addCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID,
						certResponse.getReqId());
				certificateRepository.save(certDao);

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
			throw new GeneralSecurityException("no local adcs connector available", noLocalAdcsEx);

		} catch (ACDSException adcsEx) {
			// no local ADCS available ...
			throw new GeneralSecurityException("adcs connector returned exception", adcsEx);

		} catch (IOException ioex) {
			throw new GeneralSecurityException("adcs connector caused IOException", ioex);
		}

		csr.setCsrAttributes(csrAttrs);
		csrRepository.save(csr);

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
		csrAttr.setName(CsrAttribute.ATTRIBUTE_CA_PROCESSING_FINISHED_TIMESTAMP);
		csrAttr.setValue( "" + System.currentTimeMillis());
		return csrAttr;
	}

	public void revokeCertificate(Certificate certDao, final CRLReason crlReason, final Date revocationDate, CAConnectorConfig adcsDao)
			throws GeneralSecurityException {

		try {
			BigInteger serial = new BigInteger(certDao.getSerial(), 10);
			String serialAsHex = serial.toString(16);
			LOGGER.debug("revoking certificate {} with serial '{}' with reason {}", certDao.getId(),
					serialAsHex, crlReason.getValue());

			
			getConnector(adcsDao).revokeCertifcate(serialAsHex, crlReason.getValue().intValue(), revocationDate);

		} catch (ACDSException adcsEx) {
			// no local ADCS available ...
			throw new GeneralSecurityException("adcs connector returned exception", adcsEx);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.trustable.ca3s.adcs.CertificateSource#retrieveCertificates()
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public int retrieveCertificates(CAConnectorConfig caConnectorDao) throws OODBConnectionsACDSException, ACDSProxyUnavailableException {

		LOGGER.debug("in retrieveCertificates");

		int limit = 100;

		int pollingOffset = caConnectorDao.getPollingOffset();
		
		ADCSWinNativeConnector adcsConnector = getConnector(caConnectorDao);

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
					GetCertificateResponse certResponse = adcsConnector.getCertificateByRequestId(reqId);

					try {
						Certificate certDao = certUtil.createCertificate(certResponse.getB64Cert(), null,
								null, false);

						// the Request ID is specific to ADCS instance
						certUtil.addCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_PROCESSING_CA, info);
						certUtil.addCertAttribute(certDao, CertificateAttribute.ATTRIBUTE_CA_PROCESSING_ID,
								certResponse.getReqId());
						certificateRepository.save(certDao);

						LOGGER.debug("certificate with reqId '{}' imported from ca '{}'", reqId, info);

					} catch (GeneralSecurityException | IOException e) {
						LOGGER.info("retrieving and importing certificate with reqId '{}' from ca '{}' causes {}",
								reqId, info, e.getLocalizedMessage());
					}

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

		int nNewCerts = (pollingOffset - caConnectorDao.getPollingOffset());
		
		if( nNewCerts > 0 ) {
			caConnectorDao.setPollingOffset(pollingOffset);
		}

		return nNewCerts;
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
			
	        LOGGER.debug("calculated secret as ({} bytes) : {} ", sharedSecret.length,  Base64.encodeBase64String(sharedSecret));

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
				throw new OODBConnectionsACDSException(e.getLocalizedMessage());
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
			
			crr.setDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(revocationDate.getTime()), ZoneId.systemDefault()));
			
			ObjectMapper objectMapper = new ObjectMapper();
			String payload = objectMapper.writeValueAsString(crr);
			
	        LOGGER.debug("calculated secret as ({} bytes) : {} ", sharedSecret.length,  Base64.encodeBase64String(sharedSecret));

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
				throw new OODBConnectionsACDSException(e.getLocalizedMessage());
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
				throw new OODBConnectionsACDSException(e.getLocalizedMessage());
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
				throw new OODBConnectionsACDSException(e.getLocalizedMessage());
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
				throw new OODBConnectionsACDSException(e.getLocalizedMessage());
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

