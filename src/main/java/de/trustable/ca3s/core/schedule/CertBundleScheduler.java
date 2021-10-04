package de.trustable.ca3s.core.schedule;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.security.provider.Ca3sBundleFactory;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.CertificateUtil;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CertBundleScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertBundleScheduler.class);

	private final CAConnectorConfigRepository caConfigRepo;
	private final CaConnectorAdapter caConnAd;
	private final CertificateUtil certUtil;
	private final TimedRenewalCertMapHolder timedRenewalCertMapHolder;
    private final String dnSuffix;
    private final String sans;

    public CertBundleScheduler(CAConnectorConfigRepository caConfigRepo,
                               CaConnectorAdapter caConnAd,
                               CertificateUtil certUtil,
                               TimedRenewalCertMapHolder timedRenewalCertMapHolder,
                               @Value("${ca3s.https.certificate.dnSuffix:}") String dnSuffix,
                               @Value("${ca3s.https.certificate.sans:}") String sans) {
        this.caConfigRepo = caConfigRepo;
        this.caConnAd = caConnAd;
        this.certUtil = certUtil;
        this.timedRenewalCertMapHolder = timedRenewalCertMapHolder;
        this.dnSuffix = dnSuffix;
        this.sans = sans;
    }

    @Scheduled(fixedDelay = 60000)
	public void retrieveCertificates() {

		for (CAConnectorConfig caConfigDao : caConfigRepo.findAll()) {

			if (caConfigDao.isActive() && caConfigDao.isDefaultCA()) {
				if( CAStatus.Active.equals(caConnAd.getStatus(caConfigDao))) {

					if( timedRenewalCertMapHolder.getCertMap().getBundleFactory() == null) {
						timedRenewalCertMapHolder.getCertMap().setBundleFactory( new Ca3sBundleFactory(caConfigDao, caConnAd, certUtil, dnSuffix, sans));
						LOG.info("Ca3sBundleFactory registered for TLS certificate production");
					}
				}else {
					LOG.info("CA default connector not active");
				}
			}
		}
	}
}
