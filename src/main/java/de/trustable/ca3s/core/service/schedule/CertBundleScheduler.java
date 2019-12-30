package de.trustable.ca3s.core.service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.security.provider.Ca3sBundleFactory;

/**
 * 
 * @author kuehn
 *
 */
@Component
public class CertBundleScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertificateImportScheduler.class);

	@Autowired
	private CAConnectorConfigRepository caConfigRepo;

	@Autowired
	private TimedRenewalCertMap timedRenewalCertMap;

	@Scheduled(fixedDelay = 60000)
	public void retrieveCertificates() {

		for (CAConnectorConfig caConfigDao : caConfigRepo.findAll()) {

			if (caConfigDao.isActive() && caConfigDao.isDefaultCA()) {
				if( timedRenewalCertMap.getBundleFactory() == null) {
					timedRenewalCertMap.setBundleFactory( new Ca3sBundleFactory(caConfigDao));
					LOG.info("Ca3sBundleFactory registered for TLS certificate poduction");
				}
			}						
		}
	}
}
