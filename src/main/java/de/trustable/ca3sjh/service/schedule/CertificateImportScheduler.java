package de.trustable.ca3sjh.service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import de.trustable.ca3s.adcsCertUtil.ACDSProxyUnavailableException;
import de.trustable.ca3s.adcsCertUtil.OODBConnectionsACDSException;
import de.trustable.ca3sjh.domain.CAConnectorConfig;
import de.trustable.ca3sjh.domain.enumeration.CAConnectorType;
import de.trustable.ca3sjh.repository.CAConnectorConfigRepository;
import de.trustable.ca3sjh.service.util.ADCSConnectorController;


/**
 * 
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CertificateImportScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertificateImportScheduler.class);

	@Autowired
	CAConnectorConfigRepository caConfigRepo;
	
	@Autowired
	private ADCSConnectorController adcsController;
	

	@Scheduled(fixedDelay = 30000)
	public void retrieveCertificates() {
		
		for( CAConnectorConfig caConfigDao: caConfigRepo.findAll()) {
		
			CAConnectorType conType = caConfigDao.getCaConnectorType();
			if( CAConnectorType.Adcs.equals(conType)) {
				if( caConfigDao.isActive()) {
					try {
						
						int nNewCerts = adcsController.retrieveCertificates(caConfigDao);
						
						if( nNewCerts > 0 ) {
							LOG.info("ADCS certificate retrieval for '{}' (url '{}') processed {} certificates", caConfigDao.getName(), caConfigDao.getCaUrl(), nNewCerts );
							caConfigRepo.save(caConfigDao);
						}else {
							LOG.debug("ADCS certificate retrieval for '{}' (url '{}') found no new certificates", caConfigDao.getName(), caConfigDao.getCaUrl());
						}
	
					} catch (OODBConnectionsACDSException e) {
						LOG.warn("defering ADCS querying for '{}'", caConfigDao.getName() );
					} catch (ACDSProxyUnavailableException e) {
						LOG.warn("ADCS proxy '{}' unavailable, trying later ...", caConfigDao.getName());
					} catch (Throwable th) {
						LOG.info("ADCS certificate retrieval for '{}' (url '{}') failed with msg '{}'", caConfigDao.getName(), caConfigDao.getCaUrl(), th.getMessage() );
						LOG.debug("ADCS certificate retrieval", th);
					}
				}else {
					LOG.info("ADCS proxy '{}' disabled", caConfigDao.getName() );
				}
			}else {
				LOG.debug("CAConnectorType '{}' not suitable for certificate retrieval", conType );
			}
		}
		LOG.debug("retrieveCertificates finished");
	}
}

