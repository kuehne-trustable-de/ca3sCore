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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.thymeleaf.context.Context;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.util.AuditUtil;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
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
	private CSRRepository csrRepo;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private CRLUtil crlUtil;
	
	@Autowired
	private CertificateUtil certUtil;
	
	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
	private MailService mailService;

	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;
	
	@Autowired
	private PreferenceUtil preferenceUtil;
	

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

		if( !preferenceUtil.isCheckCrl()){
			LOG.info("Check of CRL status disabled");
			return;
		}
		
		long startTime = System.currentTimeMillis();
		
		List<Certificate> certWithURLList = certificateRepo.findActiveCertificateByCrlURL();

		int count = 0;
		for (Certificate cert : certWithURLList) {
			LOG.debug("Checking certificate {} for CRL status", cert.getId());
			boolean bCRLDownloadSuccess = false; 
			int crlUrlCount = 0;
			for( CertificateAttribute certAtt: cert.getCertificateAttributes()) {
				String nextUpdate = certUtil.getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE);
				if( nextUpdate != null ) {
					try {
					long nextUpdateMilliSec = Long.parseLong(nextUpdate);
					if( startTime < nextUpdateMilliSec) {
						LOG.debug("No CRL check for certificate {}, {} sec left ...", cert.getId(), (nextUpdateMilliSec - startTime) / 1000L);
						continue;
					}
					} catch(NumberFormatException nfe) {
						LOG.warn("unexpected value for 'next update' in ATTRIBUTE_CRL_NEXT_UPDATE: {} in cert {}", nextUpdate, cert.getId());
					}
				}

				
				try {
					X509Certificate x509Cert = certUtil.convertPemToCertificate(cert.getContent());
					if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
						crlUrlCount++;
						try {
							X509CRL crl = crlUtil.downloadCRL(certAtt.getValue());
							if( crl == null) {
								continue;
							}

							X509CRLEntry crlItem = crl.getRevokedCertificate(new BigInteger(cert.getSerial()));

							if( (crlItem != null) && (crl.isRevoked(x509Cert) ) ) {
								
								String revocationReason = "unspecified";
								if( crlItem.getRevocationReason() != null ) {
									if( cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal())) != null ) {
										revocationReason = cryptoUtil.crlReasonAsString(CRLReason.lookup(crlItem.getRevocationReason().ordinal()));
									}
								}
								
								Date revocationDate = new Date();
								if( crlItem.getRevocationDate() != null) {
									revocationDate = crlItem.getRevocationDate();
								}else {
									LOG.debug("Checking certificate {}: no RevocationDate present for reason {}!", cert.getId(), revocationReason);
								}
								
							    certUtil.setRevocationStatus(cert, revocationReason, revocationDate);

							    certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE, crl.getNextUpdate().getTime());
							    
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
				LOG.info("Downloading all CRL #{} for certificate {} failed", crlUrlCount, cert.getId());
			}

			
			if( count++ > MAX_RECORDS_PER_TRANSACTION) {
				LOG.info("limited certificate revocation check to {} per call", MAX_RECORDS_PER_TRANSACTION);
				break;
			}
			
		}

		LOG.info("#{} certificate revocation checks in {} mSec", count, System.currentTimeMillis() - startTime );
	}

	/**
	 * @return number of expiring certificates
	 */
	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(fixedDelay = 60000)
	public int notifyRAOfficerHolderOnExpiry() {

		Instant now = Instant.now();
    	int nDays = 30;
    	Instant after = now;
    	Instant before = now.plus(nDays, ChronoUnit.DAYS);
    	Instant relevantPendingStart = now.minus(nDays, ChronoUnit.DAYS);
    	List<Certificate> expiringCertList = certificateRepo.findByValidTo(after, before);

    	List<CSR> pendingCsrList = csrRepo.findPendingByDay(relevantPendingStart, now);

    	if( expiringCertList.isEmpty() && pendingCsrList.isEmpty()) {
			LOG.info("No expiring certificates in the next {} days / no pending requests. No need to send a notificaton eMail to RA officers", nDays);
    	}else {
			LOG.info("#{} expiring certificate in the next {} days, #{} pending requests", expiringCertList.size(), nDays, pendingCsrList.size());
	    	for( User raOfficer: findAllRAOfficer()) {
		        Locale locale = Locale.forLanguageTag(raOfficer.getLangKey());
		        Context context = new Context(locale);
		        context.setVariable("expiringCertList", expiringCertList);
		        context.setVariable("pendingCsrList", pendingCsrList);
		        mailService.sendEmailFromTemplate(context, raOfficer, "mail/pendingReqExpiringCertificateEmail", "email.allExpiringCertificate.subject");
	    	}
    	}
    	return expiringCertList.size();
	}

	/**
	 * 
	 * @return
	 */
	private List<User> findAllRAOfficer(){
		
		List<User> raOfficerList = new ArrayList<User>();
    	for( User user: userRepository.findAll()) {
    		for( Authority auth: user.getAuthorities()) {
				LOG.debug("user {} {} has role {}", user.getFirstName(), user.getLastName(), auth.getName());
    			if( AuthoritiesConstants.RA_OFFICER.equalsIgnoreCase(auth.getName())) {
    				raOfficerList.add(user);
    				LOG.debug("found user {} {} having the role of a RA officers", user.getFirstName(), user.getLastName());
    				break;
    			}
    		}
    	}
		return raOfficerList;
	}
}
