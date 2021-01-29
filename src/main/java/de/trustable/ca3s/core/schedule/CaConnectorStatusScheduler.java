package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.security.provider.Ca3sBundleFactory;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.ca3s.core.service.dto.CAStatus;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CaConnectorStatusScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertificateImportScheduler.class);

	@Autowired
	private CaConnectorAdapter caConnAd;

	@Scheduled(fixedDelay = 10000)
	public void updateCAConnectorStatus() {

        LOG.debug("starting to update the CA connector status list");
        long startTime = System.currentTimeMillis();
        caConnAd.updateCAConnectorStatus();
        LOG.debug("updating the CA connector status list took " + (System.currentTimeMillis() - startTime) + " ms.");
	}
}
