package de.trustable.ca3s.core.service.cmp;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.cmp.CMPCertificate;
import org.bouncycastle.asn1.cmp.CertRepMessage;
import org.bouncycastle.asn1.cmp.CertResponse;
import org.bouncycastle.asn1.cmp.ErrorMsgContent;
import org.bouncycastle.asn1.cmp.GenMsgContent;
import org.bouncycastle.asn1.cmp.InfoTypeAndValue;
import org.bouncycastle.asn1.cmp.PKIBody;
import org.bouncycastle.asn1.cmp.PKIFreeText;
import org.bouncycastle.asn1.cmp.PKIHeader;
import org.bouncycastle.asn1.cmp.PKIMessage;
import org.bouncycastle.asn1.cmp.PKIStatusInfo;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.cmp.CMPException;
import org.bouncycastle.cert.crmf.CRMFException;
import org.bouncycastle.cert.jcajce.JcaX500NameUtil;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.RDNAttribute;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CAStatus;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.CryptoUtil;

@Service
public class CaCmpConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(CaCmpConnector.class);

	@Autowired
	private RemoteConnector remoteConnector;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
	CertificateUtil certUtil;

	@Autowired
	private CertificateRepository certificateRepository;

	/**
	 * 
	 */
	public CaCmpConnector() {

		LOGGER.info("CaCmpConnector cTor ...");
	}

	/**
	 * 
	 * @param csr
	 * @param user
	 * @param password
	 * @param hmacSecret
	 * @param cmpEndpoint
	 * @param alias
	 * 
	 * @return the created certificate, pem encoded
	 * 
	 * @throws GeneralSecurityException
	 */
	public de.trustable.ca3s.core.domain.Certificate signCertificateRequest(CSR csr, CAConnectorConfig caConnConfig)
			throws GeneralSecurityException {

		long certReqId = new Random().nextLong();

		try {

			LOGGER.debug("csr contains #{} CsrAttributes, #{} RequestAttributes and #{} RDN", csr.getCsrAttributes().size(), csr.getRas().size(), csr.getRdns().size());

			// build a CMP request from the CSR
			PKIMessage pkiRequest = buildCertRequest(certReqId, csr, caConnConfig.getSecret());

			byte[] requestBytes = pkiRequest.getEncoded();

			LOGGER.debug("requestBytes : " + java.util.Base64.getEncoder().encodeToString(requestBytes));

			// send and receive ..
			byte[] responseBytes = remoteConnector.sendHttpReq(caConnConfig.getCaUrl() + "/" + caConnConfig.getName(),
					requestBytes);

			if (responseBytes == null) {
				throw new GeneralSecurityException("remote connector returned 'null'");
			}

			LOGGER.debug("responseBytes : " + java.util.Base64.getEncoder().encodeToString(responseBytes));

			// extract the certificate
			de.trustable.ca3s.core.domain.Certificate cert = readCertResponse(responseBytes, pkiRequest, csr);

			csr.setStatus(CsrStatus.ISSUED);

			return cert;

		} catch (CRMFException e) {
			LOGGER.info("CMS format problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (CMPException e) {
			LOGGER.info("CMP problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (IOException e) {
			LOGGER.info("IO / encoding problem", e);
			throw new GeneralSecurityException(e.getMessage());
		}
	}

	public void revokeCertificate(X509Certificate x509Cert, final CRLReason crlReason, String hmacSecret,
			String cmpEndpoint, String alias) throws GeneralSecurityException {

		revokeCertificate(JcaX500NameUtil.getIssuer(x509Cert), JcaX500NameUtil.getSubject(x509Cert),
				x509Cert.getSerialNumber(), crlReason, hmacSecret, cmpEndpoint, alias);

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
				new BigInteger(certDao.getSerial()), crlReason, caConnConfig.getSecret(), caConnConfig.getCaUrl(),
				caConnConfig.getName());
	}

	/**
	 * 
	 * @param csr
	 * @param user
	 * @param password
	 * @param hmacSecret
	 * @param cmpEndpoint
	 * @param alias
	 * @return
	 * @throws GeneralSecurityException
	 */
	public void revokeCertificate(final X500Name issuerDN, final X500Name subjectDN, final BigInteger serial,
			final CRLReason crlReason, String hmacSecret, String cmpEndpoint, String alias)
			throws GeneralSecurityException {

		long certRevId = new Random().nextLong();

		try {

			// build a CMP request from the revocation infos
			byte[] revocationRequestBytes = cryptoUtil.buildRevocationRequest(certRevId, issuerDN, subjectDN, serial,
					crlReason, hmacSecret);

			// send and receive ..
			LOGGER.info("revocation requestBytes : "
					+ java.util.Base64.getEncoder().encodeToString(revocationRequestBytes));
			byte[] responseBytes = remoteConnector.sendHttpReq(cmpEndpoint + "/" + alias, revocationRequestBytes);
			LOGGER.info("revocation responseBytes : " + java.util.Base64.getEncoder().encodeToString(responseBytes));

			// handle the response
			cryptoUtil.readRevResponse(responseBytes);

			return;

		} catch (CRMFException e) {
			LOGGER.info("CMS format problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (CMPException e) {
			LOGGER.info("CMP problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (IOException e) {
			LOGGER.info("IO / encoding problem", e);
			throw new GeneralSecurityException(e.getMessage());
		}
	}

	/**
	 * 
	 * @param certReqId
	 * @param csr
	 * @param publicKey
	 * @param hmacSecret
	 * @return
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	public PKIMessage buildCertRequest(long certReqId, final CSR csr, final String hmacSecret)
			throws GeneralSecurityException {

		// read the pem csr and verify the signature
		PKCS10CertificationRequest p10Req;
		try {
			p10Req = cryptoUtil.parseCertificateRequest(csr.getCsrBase64()).getP10Req();
		} catch (IOException e) {
			LOGGER.error("parsing csr", e);
			throw new GeneralSecurityException(e.getMessage());
		}

		List<RDN> rdnList = new ArrayList<RDN>();
		for (de.trustable.ca3s.core.domain.RDN rdnDao : csr.getRdns()) {
			LOGGER.debug("rdnDao : " + rdnDao.getRdnAttributes());
			List<AttributeTypeAndValue> attrTVList = new ArrayList<AttributeTypeAndValue>();
			if (rdnDao != null && rdnDao.getRdnAttributes() != null) {
				for (RDNAttribute rdnAttr : rdnDao.getRdnAttributes()) {
					ASN1ObjectIdentifier aoi = new ASN1ObjectIdentifier(rdnAttr.getAttributeType());
					ASN1Encodable ae = new DERUTF8String(rdnAttr.getAttributeValue());
					AttributeTypeAndValue attrTV = new AttributeTypeAndValue(aoi, ae);
					attrTVList.add(attrTV);
				}
			}
			RDN rdn = new RDN(attrTVList.toArray(new AttributeTypeAndValue[attrTVList.size()]));
			LOGGER.debug("rdn : " + rdn.size() + " elements");
			rdnList.add(rdn);
		}

		X500Name subjectDN = new X500Name(rdnList.toArray(new RDN[rdnList.size()]));
		LOGGER.debug("subjectDN : " + subjectDN.toString());

		Collection<Extension> certExtList = new ArrayList<Extension>();

		final SubjectPublicKeyInfo keyInfo = p10Req.getSubjectPublicKeyInfo();

		return cryptoUtil.buildCertRequest(certReqId, subjectDN, certExtList, keyInfo, hmacSecret);
	}

	/**
	 * 
	 * @param certReqId
	 * @param p10Req
	 * @param hmacSecret
	 * @return
	 * @throws GeneralSecurityException
	 */
	PKIMessage buildCertRequest(long certReqId, final PKCS10CertificationRequest p10Req, final String hmacSecret)
			throws GeneralSecurityException {

		X500Name subjectDN = p10Req.getSubject();
		Collection<Extension> certExtList = new ArrayList<Extension>();

		Attribute[] attrs = p10Req.getAttributes();
		for (Attribute attr : attrs) {
			for (ASN1Encodable asn1Enc : attr.getAttributeValues()) {

				boolean critical = false;
				Extension ext;
				try {
					ext = new Extension(attr.getAttrType(), critical, asn1Enc.toASN1Primitive().getEncoded());
					LOGGER.debug("Csr Extension from PKCS10Attr : " + ext.getExtnId().getId() + " -> "
							+ ext.getParsedValue().toString());

					certExtList.add(ext);
				} catch (IOException e) {
					LOGGER.error("reading attribute", e);
					throw new GeneralSecurityException(e.getMessage());
				}
			}
		}

		return cryptoUtil.buildCertRequest(certReqId, subjectDN, certExtList, p10Req.getSubjectPublicKeyInfo(),
				hmacSecret);

	}

	public CAStatus getStatus(final CAConnectorConfig caConnConfig) {
		
		try {
			GenMsgContent infoContent = getGeneralInfo(caConnConfig.getSecret(), caConnConfig.getCaUrl(), caConnConfig.getName());
			
			InfoTypeAndValue[] infoTypeAndValueArr = infoContent.toInfoTypeAndValueArray();
	
			for (InfoTypeAndValue infoTypeAndValue : infoTypeAndValueArr) {
				LOGGER.debug("CMP instance {} returns {}: {}", caConnConfig.getName(), infoTypeAndValue.getInfoType().getId(), infoTypeAndValue.getInfoValue().toString());
			}
			return CAStatus.Active;
			
		} catch( UnrecoverableEntryException ree ) {
			// the CA responded with a proper CMP message but does not support the 'Status' request
			return CAStatus.Active;
			
		} catch( GeneralSecurityException gse) {
			LOGGER.error("status call to CMP instance '" + caConnConfig.getName() + "' failed", gse);
		}
		
		return CAStatus.Deactivated;
	}

	/**
	 * 
	 */
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
			GenMsgContent genMsgContent = cryptoUtil.readGenMsgResponse(responseBytes);
			return genMsgContent;

		} catch (CRMFException e) {
			LOGGER.info("CMS format problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (CMPException e) {
			LOGGER.info("CMP problem", e);
			throw new GeneralSecurityException(e.getMessage());
		} catch (IOException e) {
			LOGGER.info("IO / encoding problem", e);
			throw new GeneralSecurityException(e.getMessage());
		}
	}

	/**
	 * 
	 * 
	 * @param responseBytes
	 * @param pkiRequest
	 * @return
	 * @throws IOException
	 * @throws CRMFException
	 * @throws CMPException
	 * @throws GeneralSecurityException
	 */
	public de.trustable.ca3s.core.domain.Certificate readCertResponse(final byte[] responseBytes,
			PKIMessage pkiMessageReq, final CSR csr)
			throws IOException, CRMFException, CMPException, GeneralSecurityException {

		final ASN1Primitive derObject = cryptoUtil.getDERObject(responseBytes);
		final PKIMessage pkiMessage = PKIMessage.getInstance(derObject);
		if (pkiMessage == null) {
			throw new GeneralSecurityException("No CMP message could be parsed from received Der object.");
		}

		printPKIMessageInfo(pkiMessage);

		PKIHeader pkiHeaderReq = pkiMessageReq.getHeader();
		PKIHeader pkiHeaderResp = pkiMessage.getHeader();

		if (!pkiHeaderReq.getSenderNonce().equals(pkiHeaderResp.getRecipNonce())) {
			ASN1OctetString asn1Oct = pkiHeaderResp.getRecipNonce();
			if (asn1Oct == null) {
				LOGGER.info("Recip nonce  == null");
			} else {
				LOGGER.info("sender nonce "
						+ java.util.Base64.getEncoder().encodeToString(pkiHeaderReq.getSenderNonce().getOctets())
						+ " != " + java.util.Base64.getEncoder().encodeToString(asn1Oct.getOctets()));
			}
			throw new GeneralSecurityException("Sender / Recip nonce mismatch");
		}
		/*
		 * if( !pkiHeaderReq.getSenderKID().equals(pkiHeaderResp.getRecipKID())){
		 * ASN1OctetString asn1Oct = pkiHeaderResp.getRecipKID(); if( asn1Oct == null ){
		 * LOGGER.info("Recip kid  == null"); }else{ LOGGER.info("sender kid " +
		 * Base64.encodeBase64String( pkiHeaderReq.getSenderKID().getOctets() ) +
		 * " != recip kid " + Base64.encodeBase64String( asn1Oct.getOctets() )); } //
		 * throw new GeneralSecurityException( "Sender / Recip Key Id mismatch");
		 * 
		 * asn1Oct = pkiHeaderResp.getSenderKID(); if( asn1Oct == null ){
		 * LOGGER.info("sender kid  == null"); }else{ LOGGER.info("sender kid " +
		 * Base64.encodeBase64String( pkiHeaderReq.getSenderKID().getOctets() ) + " != "
		 * + Base64.encodeBase64String( asn1Oct.getOctets() )); } }
		 */

		if (!pkiHeaderReq.getTransactionID().equals(pkiHeaderResp.getTransactionID())) {
			ASN1OctetString asn1Oct = pkiHeaderResp.getTransactionID();
			if (asn1Oct == null) {
				LOGGER.info("transaction id == null");
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("transaction id "
							+ java.util.Base64.getEncoder().encodeToString(pkiHeaderReq.getTransactionID().getOctets())
							+ " != " + java.util.Base64.getEncoder().encodeToString(asn1Oct.getOctets()));
				}
			}
			throw new GeneralSecurityException("Sender / Recip Transaction Id mismatch");
		}

		final PKIBody body = pkiMessage.getBody();

		int tagno = body.getType();

		if (tagno == PKIBody.TYPE_ERROR) {
			handleCMPError(body);

		} else if (tagno == PKIBody.TYPE_CERT_REP || tagno == PKIBody.TYPE_INIT_REP) {
			// certificate successfully generated
			CertRepMessage certRepMessage = CertRepMessage.getInstance(body.getContent());

			try {
				// CMPCertificate[] cmpCertArr = certRepMessage.getCaPubs();
				CMPCertificate[] cmpCertArr = pkiMessage.getExtraCerts();
				LOGGER.info("CMP Response body contains " + cmpCertArr.length + " extra certificates");
				for (int i = 0; i < cmpCertArr.length; i++) {
					CMPCertificate cmpCert = cmpCertArr[i];
					LOGGER.info("Added CA '" + cmpCert.getX509v3PKCert().getSubject() + "' from CMP Response body");

					de.trustable.ca3s.core.domain.Certificate certDao = certUtil.createCertificate(cmpCert.getEncoded(),
							null, null, true);
					certificateRepository.save(certDao);

					LOGGER.debug("Additional CA '" + certDao.getSubject() + "' from CMP Response body");
				}
			} catch (NullPointerException npe) { // NOSONAR
				// just ignore
			}

			CertResponse[] respArr = certRepMessage.getResponse();
			if (respArr == null || (respArr.length == 0)) {
				throw new GeneralSecurityException("No CMP response found.");
			}

			LOGGER.info("CMP Response body contains " + respArr.length + " elements");

			for (int i = 0; i < respArr.length; i++) {

				if (respArr[i] == null) {
					throw new GeneralSecurityException("No CMP response returned.");
				}

				BigInteger status = BigInteger.ZERO;
				String statusText = "";

				PKIStatusInfo pkiStatusInfo = respArr[i].getStatus();
				if (pkiStatusInfo != null) {
					PKIFreeText freeText = pkiStatusInfo.getStatusString();
					if (freeText != null) {
						for (int j = 0; j < freeText.size(); j++) {
							statusText = freeText.getStringAt(j) + "\n";
						}
					}
				}

				if ((respArr[i].getCertifiedKeyPair() == null)
						|| (respArr[i].getCertifiedKeyPair().getCertOrEncCert() == null)) {

					throw new GeneralSecurityException(
							"CMP response contains no certificate, status :" + status + "\n" + statusText);
				}

				CMPCertificate cmpCert = respArr[i].getCertifiedKeyPair().getCertOrEncCert().getCertificate();
				if (cmpCert != null) {
					org.bouncycastle.asn1.x509.Certificate cmpCertificate = cmpCert.getX509v3PKCert();
					if (cmpCertificate != null) {

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("#" + i + ": " + cmpCertificate);
						}

						final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509", "BC");

						/*
						 * version returning just the end entity ...
						 */
						final Collection<? extends java.security.cert.Certificate> certificateChain = certificateFactory
								.generateCertificates(new ByteArrayInputStream(cmpCertificate.getEncoded()));

						X509Certificate[] certArray = certificateChain.toArray(new X509Certificate[0]);

						X509Certificate cert = certArray[0];
						if (LOGGER.isDebugEnabled()) {
							LOGGER.info("#" + i + ": " + cert);
						}

						de.trustable.ca3s.core.domain.Certificate certDao = certUtil
								.createCertificate(cert.getEncoded(), csr, null, false);
						certificateRepository.save(certDao);

						return certDao;
					}
				}
			}
		} else {
			throw new GeneralSecurityException("unexpected PKI body type :" + tagno);
		}

		return null;
	}

	/**
	 * @param body
	 * @throws GeneralSecurityException
	 */
	private void handleCMPError(final PKIBody body) throws GeneralSecurityException {

		ErrorMsgContent errMsgContent = ErrorMsgContent.getInstance(body.getContent());
		String errMsg = "errMsg : #" + errMsgContent.getErrorCode() + " " + errMsgContent.getErrorDetails() + " / "
				+ errMsgContent.getPKIStatusInfo().getFailInfo();

		LOGGER.info(errMsg);

		try {
			if (errMsgContent != null && errMsgContent.getPKIStatusInfo() != null) {
				PKIFreeText freeText = errMsgContent.getPKIStatusInfo().getStatusString();
				for (int i = 0; i < freeText.size(); i++) {
					LOGGER.info("#" + i + ": " + freeText.getStringAt(i));
				}
			}
		} catch (NullPointerException npe) { // NOSONAR
			// just ignore
		}

		throw new GeneralSecurityException(errMsg);
	}

	/**
	 * @param pkiMessage
	 * @return
	 */
	private void printPKIMessageInfo(final PKIMessage pkiMessage) {

		final PKIHeader header = pkiMessage.getHeader();
		final PKIBody body = pkiMessage.getBody();

		int tagno = body.getType();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Received CMP message with pvno=" + header.getPvno() + ", sender="
					+ header.getSender().toString() + ", recipient=" + header.getRecipient().toString());
			LOGGER.debug("Body is of type: " + tagno);
			LOGGER.debug("Transaction id: " + header.getTransactionID());
		}
	}

}
