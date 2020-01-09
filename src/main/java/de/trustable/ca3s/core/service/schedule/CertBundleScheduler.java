package de.trustable.ca3s.core.service.schedule;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.security.provider.Ca3sBundleFactory;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.ca3s.core.service.util.CAStatus;
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

	transient Logger LOG = LoggerFactory.getLogger(CertificateImportScheduler.class);

	@Autowired
	private CAConnectorConfigRepository caConfigRepo;

	@Autowired
	private CaConnectorAdapter caConnAd;
	
	@Autowired
	private CertificateUtil certUtil;
	
	@Autowired
	private TimedRenewalCertMapHolder timedRenewalCertMapHolder;

	@Scheduled(fixedDelay = 60000)
	public void retrieveCertificates() {

		for (CAConnectorConfig caConfigDao : caConfigRepo.findAll()) {

			if (caConfigDao.isActive() && caConfigDao.isDefaultCA()) {
				if( CAStatus.Active.equals(caConnAd.getStatus(caConfigDao))) {
					
					if( timedRenewalCertMapHolder.getCertMap().getBundleFactory() == null) {
						timedRenewalCertMapHolder.getCertMap().setBundleFactory( new Ca3sBundleFactory(caConfigDao, caConnAd, certUtil));
						LOG.info("Ca3sBundleFactory registered for TLS certificate poduction");
					}
				}else {
					LOG.info("CA default connector not active");
				}
			}						
		}
	}
}
