package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.CRLUpdateInfo;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

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

	private final CertificateUtil certUtil;

    private final CRLUtil crlUtil;


    private final PreferenceUtil preferenceUtil;

    private final NotificationService notificationService;

    public CertExpiryScheduler(@Value("${ca3s.batch.maxRecordsPerTransaction:1000}") int maxRecordsPerTransaction,
                               CertificateRepository certificateRepo,
                               CertificateUtil certUtil,
                               AuditService auditService,
                               CRLUtil crlUtil, PreferenceUtil preferenceUtil,
                               NotificationService notificationService) {

        this.maxRecordsPerTransaction = maxRecordsPerTransaction;
        this.certificateRepo = certificateRepo;
        this.certUtil = certUtil;
        this.crlUtil = crlUtil;
        this.preferenceUtil = preferenceUtil;
        this.notificationService = notificationService;
    }


    @Scheduled(fixedDelay = 3600000)
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


	@Scheduled(fixedDelay = 3600000)
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
                CRLUpdateInfo crlInfo = certUtil.checkAllCRLsForCertificate( cert,
                    CertificateUtil.convertPemToCertificate(cert.getContent()),
                    crlUtil,
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

	/**
	 * @return number of expiring certificates
	 */
	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(cron = "0 15 2 * * ?")
//	@Scheduled(fixedDelay = 60000)
	public int notifyRAOfficerHolderOnExpiry() {

        try {
            return notificationService.notifyRAOfficerHolderOnExpiry();
        } catch (MessagingException e) {
            LOG.info("Problem sending notification email", e);
        }
        return 0;
    }
}
