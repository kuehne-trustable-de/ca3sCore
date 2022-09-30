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
import java.util.*;

import javax.mail.MessagingException;
import javax.naming.NamingException;

import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
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

	private final int maxRecordsPerTransaction;

	private final CertificateRepository certificateRepo;

	private final CRLUtil crlUtil;

	private final CertificateUtil certUtil;

	private final CryptoUtil cryptoUtil;

    private final AuditService auditService;

	private final PreferenceUtil preferenceUtil;

    private final NotificationService notificationService;

    public CertExpiryScheduler( @Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                               CertificateRepository certificateRepo,
                               CRLUtil crlUtil,
                               CertificateUtil certUtil,
                               CryptoUtil cryptoUtil,
                               AuditService auditService,
                               PreferenceUtil preferenceUtil,
                               NotificationService notificationService) {

        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
        this.certificateRepo = certificateRepo;
        this.crlUtil = crlUtil;
        this.certUtil = certUtil;
        this.cryptoUtil = cryptoUtil;
        this.auditService = auditService;
        this.preferenceUtil = preferenceUtil;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedRateString="${ca3s.schedule.rate.certRetrieval:3600000}")
	public void retrieveCertificates() {

		Instant now = Instant.now();

		List<Certificate> becomingValidList = certificateRepo.findInactiveCertificatesByValidFrom(now);

		int count = 0;
		for (Certificate cert : becomingValidList) {
			cert.setActive(true);
			certificateRepo.save(cert);
			LOG.info("Certificate {} becoming active passing 'validFrom'", cert.getId());

			if( count++ > maxRecordsPerTransaction) {
				LOG.info("limited certificate validity processing to {} per call", maxRecordsPerTransaction);
				break;
			}
		}

		List<Certificate> becomingInvalidList = certificateRepo.findActiveCertificatesByValidTo(now);

		count = 0;
		for (Certificate cert : becomingInvalidList) {
			cert.setActive(false);
			certificateRepo.save(cert);
			LOG.info("Certificate {} becoming inactive due to expiry", cert.getId());

			if( count++ > maxRecordsPerTransaction) {
				LOG.info("limited certificate validity processing to {} per call", maxRecordsPerTransaction);
				break;
			}
		}

	}


    @Scheduled(fixedRateString="${ca3s.schedule.rate.revocationCheck:3600000}")
	public void updateRevocationStatus() {

		if( !preferenceUtil.isCheckCrl()){
			LOG.info("Check of CRL status disabled");
			return;
		}

        long excessiveNextUpdate = System.currentTimeMillis() + (2L * 1000L * preferenceUtil.getMaxNextUpdatePeriodCRLSec());

        long startTime = System.currentTimeMillis();

		HashSet<String> brokenCrlUrlList = new HashSet<>();

		List<Object[]> certWithURLList = certificateRepo.findActiveCertificateOrderedByCrlURL();
        LOG.debug("findActiveCertificateOrderedByCrlURL returns #{} certificates in {} ms", certWithURLList.size(), System.currentTimeMillis() - startTime);

		int count = 0;
		for (Object[] resultArr : certWithURLList) {
            Certificate cert = (Certificate) resultArr[0];

			LOG.debug("Checking certificate {} for CRL status, URL '{}'", cert.getId(), resultArr[1]);

            String nextUpdate = certUtil.getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE);
            if( nextUpdate != null ) {
                try {
                    long nextUpdateMilliSec = Long.parseLong(nextUpdate);
                    if( nextUpdateMilliSec > excessiveNextUpdate) {
                        LOG.info("Excessively long CRL validity period for certificate {} ({} sec left), enforcing check.", cert.getId(), (nextUpdateMilliSec - startTime) / 1000L);
                    } else if( startTime < nextUpdateMilliSec ) {
                        LOG.debug("No CRL check for certificate {}, {} sec left ...", cert.getId(), (nextUpdateMilliSec - startTime) / 1000L);
                        continue;
                    }
                } catch(NumberFormatException nfe) {
                    LOG.warn("unexpected value for 'next update' in ATTRIBUTE_CRL_NEXT_UPDATE: {} in cert {}", nextUpdate, cert.getId());
                }
            }

            try {
                CRLUpdateInfo crlInfo = checkAllCRLsForCertificate( cert,
                    CertificateUtil.convertPemToCertificate(cert.getContent()),
                    brokenCrlUrlList);

                if( !crlInfo.isbCRLDownloadSuccess() ) {
                    LOG.info("Downloading all CRL #{} for certificate {} failed", crlInfo.getCrlUrlCount(), cert.getId());
                }

            }catch(GeneralSecurityException gse){
                LOG.debug("problem converting certificate id '"+ cert.getId()+"' to X509",gse);
                continue;
            }

            if( count++ > maxRecordsPerTransaction) {
                LOG.info("limited certificate revocation check to {} per call", maxRecordsPerTransaction);
                break;
            }

		}

		if( !brokenCrlUrlList.isEmpty()) {
            LOG.info("#{} CRL URLs marked as inaccessible / broken", brokenCrlUrlList.size());
        }
        LOG.info("#{} certificate revocation checks in {} mSec", count, System.currentTimeMillis() - startTime );
	}

    CRLUpdateInfo checkAllCRLsForCertificate(Certificate cert, X509Certificate x509Cert, HashSet<String> brokenCrlUrlList){

        CRLUpdateInfo info = new CRLUpdateInfo();
        long maxNextUpdate = System.currentTimeMillis() + 1000L * preferenceUtil.getMaxNextUpdatePeriodCRLSec();

        for( CertificateAttribute certAtt: cert.getCertificateAttributes()) {


            // iterate all CRL URLs
            if( CertificateAttribute.ATTRIBUTE_CRL_URL.equals(certAtt.getName())) {
                String crlUrl = certAtt.getValue();

                if(brokenCrlUrlList.contains(crlUrl)){
                    LOG.debug("CRL URL'{}' already marked as broken / inaccessible", crlUrl);
                    continue;
                }

                info.incUrlCount();
                try {
                    LOG.debug("downloading CRL '{}'", crlUrl);
                    X509CRL crl = crlUtil.downloadCRL(crlUrl);
                    if( crl == null) {
                        LOG.debug("downloaded CRL == null ");
                        continue;
                    }

                    long nextUpdate = crl.getNextUpdate().getTime();
                    if( nextUpdate > maxNextUpdate ){
                        LOG.debug("nextUpdate {} from CRL limited to {}", crl.getNextUpdate(), new Date(maxNextUpdate));
                        nextUpdate = maxNextUpdate;
                    }

                    // set the crl's 'next update' timestamp to the certificate
                    certUtil.setCertAttribute(cert, CertificateAttribute.ATTRIBUTE_CRL_NEXT_UPDATE, Long.toString(nextUpdate), false);

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

                        auditService.saveAuditTrace(auditService.createAuditTraceCertificate(AuditService.AUDIT_CERTIFICATE_REVOKED_BY_CRL, cert));
                    }
                    info.setSuccess();
                    break;
                } catch (CertificateException | CRLException | IOException | NamingException e2) {
                    LOG.info("Problem retrieving CRL for certificate "+ cert.getId());
                    LOG.debug("CRL retrieval for certificate "+ cert.getId() + " failed", e2);
                    brokenCrlUrlList.add(crlUrl);
                }
            }
        }

        return info;
    }

	/**
	 * @return number of expiring certificates
	 */
//	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(fixedDelay = 60000)
    @Scheduled(cron="${ca3s.schedule.cron.expiryNotificationCron:0 15 2 * * ?}")
	public int notifyRAOfficerHolderOnExpiry() {

        try {
            return notificationService.notifyRAOfficerHolderOnExpiry();
        } catch (MessagingException e) {
            LOG.info("Problem sending notification email", e);
        }
        return 0;
    }


	 static class CRLUpdateInfo{
        boolean bCRLDownloadSuccess = false;
        int crlUrlCount = 0;

        public CRLUpdateInfo(){}

        public void setSuccess(){ bCRLDownloadSuccess = true;}

        public void incUrlCount(){ crlUrlCount++;}

        public boolean isbCRLDownloadSuccess() {
            return bCRLDownloadSuccess;
        }

        public int getCrlUrlCount() {
            return crlUrlCount;
        }
    }
}
