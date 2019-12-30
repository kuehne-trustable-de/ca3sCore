package de.trustable.ca3s.core.security.provider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;



@Service
public class Ca3sTrustManager implements X509TrustManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sTrustManager.class);

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private CryptoService cryptoUtil;

	@Autowired
	private CertificateUtil certUtil;

	@Override
	public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {
		if( cert.length == 0) {
			throw new CertificateException();
		}
		LOGGER.debug("checkClientTrusted called for authType '{}' with certificate subject '{}'",authType, cert[0].getSubjectDN().getName());
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if( chain.length == 0) {
			throw new CertificateException();
		}
		X509Certificate serverCert = chain[0];
		LOGGER.debug("checkServerTrusted called for authType '{}' with certificate subject '{}'",authType, serverCert.getSubjectDN().getName());
		
		Date now = new Date();
		if( now.after(serverCert.getNotAfter()) ) {
			LOGGER.debug("checkServerTrusted:  certificate with subject '" + serverCert.getSubjectDN().getName() + "' not valid anymore (now > " + serverCert.getNotAfter());
			throw new CertificateException();
		}
		
		if(now.before(serverCert.getNotBefore()) ) {
			LOGGER.debug("checkServerTrusted:  certificate with subject '" + serverCert.getSubjectDN().getName() + "' not valid yet (now < " + serverCert.getNotBefore());
			throw new CertificateException();
		}
		
		try {
			Certificate serverCertDao = certUtil.createCertificate(cryptoUtil.x509CertToPem(serverCert), null, 
					null,
					false);

			LOGGER.debug("checkServerTrusted : server certificate found in database  '" + serverCertDao.getSubject() + "' with id  '" + serverCertDao.getId() + "'" );

			if( serverCertDao.isRevoked()) {
				LOGGER.debug("checkServerTrusted : certificate for subject '" + serverCert.getSubjectDN().getName() + "', revoked '" + serverCertDao.getRevocationReason() + "' on " + serverCertDao.getRevokedSince());
				throw new CertificateException();
			}
			
			Certificate issuingCACertDao = certUtil.findIssuingCertificate(serverCertDao);
			LOGGER.debug("checkServerTrusted : no issuing certificate found for certificate subject '" + serverCertDao.getSubject() + "',  : chain has " + chain.length + " elements");
			if( issuingCACertDao == null && (chain.length > 1)) {
				
				issuingCACertDao = certUtil.createCertificate(cryptoUtil.x509CertToPem(chain[1]), null, 
						null,
						false);
				LOGGER.debug("checkServerTrusted importing Issueing CA cert '" + chain[1].getSubjectDN().getName() + "'");
				
				serverCertDao = certUtil.createCertificate(cryptoUtil.x509CertToPem(serverCert), null, 
						null,
						false);
			}
			if( issuingCACertDao == null){
				LOGGER.debug("checkServerTrusted : no issuing certificate found for certificate subject '" + serverCert.getSubjectDN().getName() + "', issuer : '" + serverCert .getIssuerDN().getName() + "'");
				throw new CertificateException();
			}
			
			if( issuingCACertDao.isRevoked()) {
				LOGGER.debug("checkServerTrusted : certificate forsubject '" + issuingCACertDao.getSubject() + "', revoked '" + issuingCACertDao.getRevocationReason() + "' on " + issuingCACertDao.getRevokedSince());
				throw new CertificateException();
			}

			ArrayList<X509Certificate> certList = new ArrayList<X509Certificate>();
			certList.add(serverCert);
			certList.add(serverCert);
			certList.add(CryptoService.convertPemToCertificate(issuingCACertDao.getContent()));
			
			for( int i = 0; i < 8; i++) {
				Certificate nextCACertDao = certUtil.findIssuingCertificate(issuingCACertDao);
				if( nextCACertDao == null){
					LOGGER.debug("checkServerTrusted : no issuing certificate found for certificate subject '" + issuingCACertDao.getSubject() + "', issuer : '" + issuingCACertDao.getIssuer() + "'");
					throw new CertificateException();
				}
				
				issuingCACertDao = nextCACertDao;
				if( issuingCACertDao.isRevoked()) {
					LOGGER.debug("checkServerTrusted : certificate forsubject '" + issuingCACertDao.getSubject() + "', revoked '" + issuingCACertDao.getRevocationReason() + "' on " + issuingCACertDao.getRevokedSince());
					throw new CertificateException();
				}
				certList.add(CryptoService.convertPemToCertificate( issuingCACertDao.getContent()));
				
				if( issuingCACertDao.getId() == issuingCACertDao.getIssuingCertificate().getId()) {
					LOGGER.debug("certificate chain complete, cert id '{}' is selfsigned", issuingCACertDao.getId());
					break;
				}
			}
			
			/**
			 * @todo
			 * 
			 * check chain with standard trust manager
			 */
			
		} catch (GeneralSecurityException | IOException e) {
			LOGGER.debug("checkServerTrusted exception for certificate subject '" + serverCert.getSubjectDN().getName() + "'", e);
			throw new CertificateException();
		} catch (Throwable th) {
			LOGGER.debug("checkServerTrusted: caught Throwable", th);
			throw th;
		}

		LOGGER.debug("checkServerTrusted succeeded for certificate subject '" + serverCert.getSubjectDN().getName() + "'");

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		LOGGER.debug("getAcceptedIssuers call !");
		
		List<Certificate> acceptedIssuerList = certificateRepository.findBySearchTermNamed1(CertificateAttribute.ATTRIBUTE_CA, "true");
		X509Certificate[] certArray = new X509Certificate[acceptedIssuerList.size()];
		for( int i = 0; i < acceptedIssuerList.size(); i++) {
			try {
				certArray[i] = CryptoService.convertPemToCertificate( acceptedIssuerList.get(i).getContent());
			} catch (GeneralSecurityException e) {
				LOGGER.debug("getAcceptedIssuers exception processing certificate dao with id  '" + acceptedIssuerList.get(i).getId() + "'", e);
			}
		}
		
		LOGGER.debug("getAcceptedIssuers returns {} elements", certArray.length);
		return certArray;
	}

}
