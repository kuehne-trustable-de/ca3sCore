package de.trustable.ca3s.core.schedule;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.List;

import javax.naming.NamingException;

import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.util.AuditUtil;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.DateUtil;
import de.trustable.util.CryptoUtil;

/**
 * 
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CertExpiryScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertExpiryScheduler.class);

	final static int MAX_RECORDS_PER_TRANSACTION = 1000;
	
	@Autowired
	private CertificateRepository certificateRepo;
	
	@Autowired
	private CRLUtil crlUtil;
	
	@Autowired
	private CertificateUtil certUtil;
	
	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	

	@Scheduled(fixedDelay = 3600000)
	public void retrieveCertificates() {

		Instant now = Instant.now();
		
		List<Certificate> becomingValidList = certificateRepo.findInactiveCertificatesByValidFrom(now);

		int count = 0;
		for (Certificate cert : becomingValidList) {
			cert.setActive(true);
			certificateRepo.save(cert);
			LOG.info("Certificate {} becoming active passing 'validFrom'", cert.getId());
			
			if( count++ > MAX_RECORDS_PER_TRANSACTION) {
				LOG.info("limited certificate validity processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
				break;
			}
		}

		List<Certificate> becomingInvalidList = certificateRepo.findActiveCertificatesByValidTo(now);
		  
		count = 0;
		for (Certificate cert : becomingInvalidList) {
			cert.setActive(false);
			certificateRepo.save(cert);
			LOG.info("Certificate {} becoming inactive due to expiry", cert.getId());
			
			if( count++ > MAX_RECORDS_PER_TRANSACTION) {
				LOG.info("limited certificate validity processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
				break;
			}
		}
		
	}
	@Scheduled(fixedDelay = 3600000)
	public void updateRevocationStatus() {

		Instant now = Instant.now();
		
		List<Certificate> certWithURLList = certificateRepo.findActiveCertificateByCrlURL();

		int count = 0;
		for (Certificate cert : certWithURLList) {
			LOG.debug("Checking certificate {} for CRL status", cert.getId());
			boolean bCRLDownloadSuccess = false; 
			for( CertificateAttribute certAtt: cert.getCertificateAttributes()) {
				try {
					X509Certificate x509Cert = certUtil.convertPemToCertificate(cert.getContent());
					if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
						try {
							X509CRL crl = crlUtil.downloadCRL(certAtt.getValue());
							
							if( crl.isRevoked(x509Cert) ) {
								X509CRLEntry crlItem = crl.getRevokedCertificate(new BigInteger(cert.getSerial()));
								cert.setActive(false);
								cert.setRevoked(true);
								String revocationReason = "unspecified";
								if( crlItem.getRevocationReason() != null ) {
									if( cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal())) != null ) {
										revocationReason = cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal()));
									}
								}
								cert.setRevocationReason(revocationReason);
								
								if( crlItem.getRevocationDate() != null) {
									cert.setRevokedSince(DateUtil.asInstant(crlItem.getRevocationDate()));
								}else {
									LOG.debug("Checking certificate {}: no RevocationDate present for reason {}!", cert.getId(), revocationReason);
								}
								
								applicationEventPublisher.publishEvent(
								        new AuditApplicationEvent(
								        		"SYSTEM", AuditUtil.AUDIT_CERTIFICATE_REVOKED, "certificate " + cert.getId() + " revocation detected in CRL"));
							}
							bCRLDownloadSuccess = true;
							break;
						} catch (CertificateException | CRLException | IOException | NamingException e2) {
							LOG.info("Problem retrieving CRL for certificate "+ cert.getId(), e2);
						}
					}
				} catch (GeneralSecurityException e) {
					LOG.info("Problem reading X509 content of certificate {} " + cert.getId(), e);
				}
			}
			if( !bCRLDownloadSuccess ) {
				LOG.info("Downloading CRL for certificate {} failed", cert.getId());
			}

/*			
			if( count++ > MAX_RECORDS_PER_TRANSACTION) {
				LOG.info("limited certificate URL processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
				break;
			}
*/			
		}
		
	}
}
