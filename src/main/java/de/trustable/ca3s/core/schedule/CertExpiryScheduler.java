package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.NotificationService;
import de.trustable.ca3s.core.service.dto.CRLUpdateInfo;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.naming.NamingException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.time.Instant;
import java.util.*;

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

    private final HashMap<String, Long> crlNextCheck = new HashMap<>();

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


    @Scheduled(fixedRateString="${ca3s.schedule.rate.certRetrieval:3600000}")
	public void retrieveCertificates() {

		Instant now = Instant.now();

		Page<Certificate> becomingValidList = certificateRepo.findInactiveCertificatesByValidFrom( PageRequest.of(0, maxRecordsPerTransaction), now);

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

        Page<Certificate> becomingInvalidList = certificateRepo.findActiveCertificatesByValidTo(PageRequest.of(0, maxRecordsPerTransaction), now);

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

	public void updateRevocationStatus() {

		if( !preferenceUtil.isCheckCrl()){
			LOG.info("Check of CRL status disabled");
			return;
		}

        long excessiveNextUpdate = System.currentTimeMillis() + (2L * 1000L * preferenceUtil.getMaxNextUpdatePeriodCRLSec());

        long startTime = System.currentTimeMillis();

		HashSet<String> brokenCrlUrlList = new HashSet<>();

        /*
         @ToDo: check for pageable !!
         */
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
                        LOG.debug("No CRL check for certificate {}, {} sec left until nextUpdate ...", cert.getId(), (nextUpdateMilliSec - startTime) / 1000L);
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

//    @Scheduled(cron="${ca3s.schedule.cron.expiryNotificationCron:0 15 2 * * ?}")
    @Scheduled(fixedRateString="${ca3s.schedule.rate.revocationCheck:10000}")
    public void updateRevocationStatus2() {

        if( !preferenceUtil.isCheckCrl()){
            LOG.info("Check of CRL status disabled");
            return;
        }

        long now = System.currentTimeMillis();

        List<String> crlURLList = certificateRepo.findDistinctCrlURLForActiveCertificates();
        LOG.debug("findDistinctCrlURLForActiveCertificates returns #{} certificates in {} ms", crlURLList.size(), System.currentTimeMillis() - now);

        for( String crlUrl: crlURLList){
            if( crlNextCheck.containsKey(crlUrl) ){
                Long nextCheck = crlNextCheck.get(crlUrl);
                if( nextCheck > now){
                    LOG.debug("next check for '{}' in {} sec.", crlUrl, (nextCheck - now)/1000L);
                    continue;
                }
            }

            try {
                LOG.debug("downloading CRL '{}'", crlUrl);
                X509CRL crl = crlUtil.downloadCRL(crlUrl);
                if (crl == null) {
                    LOG.debug("downloaded CRL == null ");
                    continue;
                }

                if (crl.getNextUpdate() == null) {
                    LOG.warn("nextUpdate missing in CRL '{}'", crlUrl);
                } else {
                    long nextUpdate = crl.getNextUpdate().getTime();

                    Set<? extends X509CRLEntry> crlEntrySet = crl.getRevokedCertificates();
                    if( crlEntrySet != null) {
                        LOG.debug("CRL {} contains #{} items", crlUrl, crlEntrySet.size());
                        List<Certificate> certWithRevokedSerialList = getAllKnownCertificatesinList(crlUrl, crlEntrySet);
                        LOG.debug("CRL {} has #{} probable revocation candidates", crlUrl, certWithRevokedSerialList.size());

                        for(Certificate cert: certWithRevokedSerialList){
                            X509Certificate x509Cert;
                            try {
                                x509Cert = CryptoService.convertPemToCertificate(cert.getContent());
                                if( crl.getRevokedCertificate(x509Cert) != null ){
                                    LOG.debug("Cert {} revoked by CRL {}", x509Cert, crlUrl);
                                    certUtil.setRevocationStatus(cert, x509Cert, crl);
                                }
                            } catch (GeneralSecurityException e) {
                                LOG.warn("Problem parsing cert #{} ", cert.getId());
                            }
                        }
                    }
                    crlNextCheck.put(crlUrl, nextUpdate);

                }
            } catch (CertificateException | CRLException | IOException | NamingException e2) {

                if( LOG.isDebugEnabled()) {
                    LOG.debug("CRL retrieval for '" + crlUrl + "' failed", e2);
                }
                LOG.warn("CRL retrieval for '" + crlUrl + "' failed with reason {}", e2.getMessage());
            }
        }

//        LOG.info("#{} certificate revocation checks in {} mSec", count, System.currentTimeMillis() - startTime );

    }

    /**
	 * @return number of expiring certificates
	 */
//	@Scheduled(fixedDelay = 60000)
    @Scheduled(cron="${ca3s.schedule.cron.expiryNotificationCron:0 15 2 * * ?}")
	public int notifyRAOfficerHolderOnExpiry() {

        try {
            notificationService.notifyRAOfficerHolderOnExpiry();
        } catch (MessagingException e) {
            LOG.info("Problem sending ra officer notification email", e);
        }

        notificationService.notifyRequestorOnExpiry(null,true);

        return 0;
    }

    List<Certificate> getAllKnownCertificatesinList(final String crlUrl, final Set<? extends X509CRLEntry> crlEntrySet){

        List<Certificate> certWithRevokedSerialList = new ArrayList<>();
        List<String> revokedSerialsList = new ArrayList<>();
        for( X509CRLEntry entry: crlEntrySet){
            revokedSerialsList.add(entry.getSerialNumber().toString());
            if( revokedSerialsList.size() > 1000){
                certWithRevokedSerialList.addAll(certificateRepo.findActiveCertificatesBySerialInList(crlUrl,revokedSerialsList ));
                revokedSerialsList.clear();
            }
        }
        certWithRevokedSerialList.addAll(certificateRepo.findActiveCertificatesBySerialInList(crlUrl,revokedSerialsList ));

        return certWithRevokedSerialList;
    }

}
