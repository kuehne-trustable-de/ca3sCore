package de.trustable.ca3s.core.security.provider;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.net.ssl.X509TrustManager;

import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;


@Service
public class Ca3sTrustManager implements X509TrustManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(Ca3sTrustManager.class);

	protected final CertificateRepository certificateRepository;

	private final CryptoService cryptoUtil;

    private final CertificateUtil certUtil;

    private final AuditService auditService;


    public Ca3sTrustManager(CertificateRepository certificateRepository, CryptoService cryptoUtil, CertificateUtil certUtil, AuditService auditService) {
        this.certificateRepository = certificateRepository;
        this.cryptoUtil = cryptoUtil;
        this.certUtil = certUtil;
        this.auditService = auditService;
    }

    @Override
	public void checkClientTrusted(X509Certificate[] cert, String authType) throws CertificateException {
		if( cert.length == 0) {
			throw new CertificateException();
		}
		LOGGER.debug("checkClientTrusted called for authType '{}' with certificate subject '{}'",authType, cert[0].getSubjectX500Principal().toString());
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if( chain.length == 0) {
			throw new CertificateException("chain.length == 0");
		}
		X509Certificate serverCert = chain[0];
		LOGGER.debug("checkServerTrusted called for authType '{}' with certificate subject '{}'",authType, serverCert.getSubjectX500Principal().toString());

		Date now = new Date();
		if( now.after(serverCert.getNotAfter()) ) {
			LOGGER.debug("checkServerTrusted:  certificate with subject '" + serverCert.getSubjectX500Principal().toString() + "' not valid anymore (now > " + serverCert.getNotAfter());
			throw new CertificateException( "certificate '" + serverCert.getSubjectX500Principal().toString() + "' expired!");
		}

		if(now.before(serverCert.getNotBefore()) ) {
			LOGGER.debug("checkServerTrusted:  certificate with subject '" + serverCert.getSubjectX500Principal().toString() + "' not valid yet (now < " + serverCert.getNotBefore());
			throw new CertificateException("certificate '" + serverCert.getSubjectX500Principal().toString() + "' not valid yet!");
		}

		try {

            Certificate issuingCACertDao = getCertificateObject(chain, serverCert);

            ArrayList<X509Certificate> certList = new ArrayList<>();
			certList.add(serverCert);
			certList.add(serverCert);
			certList.add(CryptoService.convertPemToCertificate(issuingCACertDao.getContent()));

			for( int i = 0; i < 8; i++) {
				Certificate nextCACertDao = certUtil.findIssuingCertificate(issuingCACertDao);
				if( nextCACertDao == null){
                    String msg = "checkServerTrusted : no issuing certificate found for certificate subject '" + issuingCACertDao.getSubject() + "', issuer : '" + issuingCACertDao.getIssuer() + "'";
                    LOGGER.info(msg);
					throw new CertificateException(msg);
				}

				issuingCACertDao = nextCACertDao;
				if( issuingCACertDao.isRevoked()) {
                    String msg = "checkServerTrusted : certificate for subject '" + issuingCACertDao.getSubject() + "', revoked '" + issuingCACertDao.getRevocationReason() + "' on " + issuingCACertDao.getRevokedSince();
                    LOGGER.info(msg);
					throw new CertificateException(msg);
				}
				certList.add(CryptoService.convertPemToCertificate( issuingCACertDao.getContent()));

				if( "true".equalsIgnoreCase(certUtil.getCertAttribute(issuingCACertDao, CertificateAttribute.ATTRIBUTE_SELFSIGNED))) {
					LOGGER.debug("certificate chain complete, cert id '{}' is selfsigned", issuingCACertDao.getId());
					if(!issuingCACertDao.isTrusted()){
                        LOGGER.warn("checkServerTrusted : root certificate with subject '" + issuingCACertDao.getSubject() + "' is NOT explicitly marked as trusted !");
                    }

					break;
				}
			}

			/*
			 * @todo
			 *
			 * check chain with standard trust manager
			 */

		} catch (GeneralSecurityException | IOException e) {
			LOGGER.info("checkServerTrusted exception for certificate subject '" + serverCert.getSubjectX500Principal().toString() + "'", e);
			throw new CertificateException(e.getMessage());
		} catch (Throwable th) {
			LOGGER.warn("checkServerTrusted: caught Throwable", th);
			throw th;
		}

		LOGGER.debug("checkServerTrusted succeeded for certificate subject '" + serverCert.getSubjectX500Principal().toString() + "'");

	}

    private synchronized Certificate getCertificateObject(X509Certificate[] chain, X509Certificate serverCert) throws GeneralSecurityException, IOException {
//        LOGGER.debug("checkServerTrusted : entering synchronized block!" );

        Certificate serverCertDao = certUtil.getCertificateByX509(serverCert);
        if( serverCertDao!= null){
            LOGGER.debug("checkServerTrusted : server certificate found in database  '" + serverCertDao.getSubject() + "' with id  '" + serverCertDao.getId() + "'" );
        }else {
            serverCertDao = certUtil.createCertificate(cryptoUtil.x509CertToPem(serverCert), null,
                null,
                false);
            auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_TLS_CERTIFICATE_IMPORTED, serverCertDao));
        }

        if( serverCertDao.isRevoked()) {
            LOGGER.debug("checkServerTrusted : certificate for subject '" + serverCert.getSubjectX500Principal().toString() + "', revoked '" + serverCertDao.getRevocationReason() + "' on " + serverCertDao.getRevokedSince());
            throw new CertificateException();
        }

        Certificate issuingCACertDao = null;
        try {
            issuingCACertDao = certUtil.findIssuingCertificate(serverCertDao);
        }catch( GeneralSecurityException gse){
            LOGGER.debug("checkServerTrusted : issuing certificate not available for certificate subject '" + serverCertDao.getSubject() + "', checking TLS chain with " + chain.length + " elements");
        }

        if( issuingCACertDao == null && (chain.length > 1)) {
            LOGGER.debug("checkServerTrusted : no issuing certificate found in database for certificate subject '" + serverCertDao.getSubject() + "',  : chain has " + chain.length + " elements");

            issuingCACertDao = certUtil.createCertificate(cryptoUtil.x509CertToPem(chain[1]), null,
                    null,
                    false);
            auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_TLS_INTERMEDIATE_CERTIFICATE_IMPORTED, serverCertDao));
            LOGGER.debug("checkServerTrusted importing issuing CA cert '" + chain[1].getSubjectX500Principal().toString() + "'");

        }
        if( issuingCACertDao == null){
            LOGGER.debug("checkServerTrusted : no issuing certificate found for certificate subject '" + serverCert.getSubjectX500Principal().toString() + "', issuer : '" + serverCert.getIssuerX500Principal().toString() + "'");
            throw new CertificateException();
        }

        if( issuingCACertDao.isRevoked()) {
            LOGGER.debug("checkServerTrusted : certificate for subject '" + issuingCACertDao.getSubject() + "', revoked '" + issuingCACertDao.getRevocationReason() + "' on " + issuingCACertDao.getRevokedSince());
            throw new CertificateException();
        }
        return issuingCACertDao;
    }

    @Override
//    @Cacheable("AcceptedIssuer")
	public X509Certificate[] getAcceptedIssuers() {
		LOGGER.debug("getAcceptedIssuers call !");

		List<Certificate> acceptedIssuerList = getAcceptedIssuerList();
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

    List<Certificate> getAcceptedIssuerList() {
        List<Certificate> list =  certificateRepository.findBySearchTermNamed1(CertificateAttribute.ATTRIBUTE_CA, "true");
        LOGGER.debug("getAcceptedIssuerList returns {} elements", list.size());
        return list;
    }

}
