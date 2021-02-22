package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.adcsCertUtil.ADCSProxyUnavailableException;
import de.trustable.ca3s.adcsCertUtil.OODBConnectionsADCSException;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.enumeration.CAConnectorType;
import de.trustable.ca3s.core.domain.enumeration.Interval;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.adcs.ADCSConnector;
import de.trustable.ca3s.core.service.dir.DirectoryConnector;

/**
 *
 * @author kuehn
 *
 */
@Component
public class CertificateImportScheduler {

	transient Logger LOG = LoggerFactory.getLogger(CertificateImportScheduler.class);

	@Autowired
	CAConnectorConfigRepository caConfigRepo;

	@Autowired
	private ADCSConnector adcsController;

	@Autowired
	private DirectoryConnector dirConnector;

    @Autowired
    private AuditService auditService;


    @Value("${certificate.import.active:true}")
	private String certificateImportActive;

	@Scheduled(fixedDelay = 30000)
	public void runMinute() {

		if ("true".equalsIgnoreCase(certificateImportActive) ) {
			for (CAConnectorConfig caConfig : caConfigRepo.findAll()) {

//                LOG.debug("--------- runMinute for {}, interval {}, active {}", caConfig.getName(), caConfig.getInterval(), caConfig.isActive());
				if( Interval.MINUTE.equals(caConfig.getInterval()) && caConfig.isActive()){
					runImporter(caConfig);
				}
			}
			LOG.debug("retrieveCertificates 'Minute' finished");
		} else {
			LOG.debug("retrieveCertificates disabled");
		}
	}

	@Scheduled(cron = "0 3 * * * *")
	public void runHour() {

		if ("true".equalsIgnoreCase(certificateImportActive)) {
			for (CAConnectorConfig caConfig : caConfigRepo.findAll()) {

				if( Interval.HOUR.equals(caConfig.getInterval()) && caConfig.isActive()){
					runImporter(caConfig);
				}
			}
			LOG.debug("retrieveCertificates 'Hour' finished");
		} else {
			LOG.debug("retrieveCertificates disabled");
		}
	}

	@Scheduled(cron = "0 14 1 * * *")
	public void runDay() {

		if ("true".equalsIgnoreCase(certificateImportActive)) {
			for (CAConnectorConfig caConfig : caConfigRepo.findAll()) {

				if( Interval.DAY.equals(caConfig.getInterval()) && caConfig.isActive()){
					runImporter(caConfig);
				}
			}
			LOG.debug("retrieveCertificates 'Day' finished");
		} else {
			LOG.debug("retrieveCertificates disabled");
		}
	}

	@Scheduled(cron = "0 5 2 ? * SUN")
	public void runWeek() {

		if ("true".equalsIgnoreCase(certificateImportActive)) {
			for (CAConnectorConfig caConfig : caConfigRepo.findAll()) {

				if( Interval.WEEK.equals(caConfig.getInterval()) && caConfig.isActive()){
					runImporter(caConfig);
				}
			}
			LOG.debug("retrieveCertificates 'Month' finished");
		} else {
			LOG.debug("retrieveCertificates disabled");
		}
	}

	@Scheduled(cron = "22 5 3 1 * ?")
	public void runMonth() {

		if ("true".equalsIgnoreCase(certificateImportActive)) {
			for (CAConnectorConfig caConfig : caConfigRepo.findAll()) {

				if( Interval.MONTH.equals(caConfig.getInterval()) && caConfig.isActive()){
					runImporter(caConfig);
				}
			}
			LOG.debug("retrieveCertificates 'Month' finished");
		} else {
			LOG.debug("retrieveCertificates disabled");
		}
	}

	private void runImporter(CAConnectorConfig caConfig) {
		CAConnectorType conType = caConfig.getCaConnectorType();
		if (CAConnectorType.ADCS_CERTIFICATE_INVENTORY.equals(conType)) {
			if (caConfig.isActive()) {

				try {

					int nNewCerts = adcsController.retrieveCertificates(caConfig);

					if (nNewCerts > 0) {
						LOG.info("ADCS certificate retrieval for '{}' (url '{}') processed {} certificates",
								caConfig.getName(), caConfig.getCaUrl(), nNewCerts);
						caConfigRepo.save(caConfig);
					} else {
						LOG.debug("ADCS certificate retrieval for '{}' (url '{}') found no new certificates",
								caConfig.getName(), caConfig.getCaUrl());
					}

				} catch (OODBConnectionsADCSException e) {
					LOG.warn("defering ADCS querying for '{}'", caConfig.getName());
				} catch (ADCSProxyUnavailableException e) {
//					LOG.debug("problem retrieving certificates", e);
					LOG.warn("ADCS proxy '{}' unavailable, trying later ...", caConfig.getName());
				} catch (Throwable th) {
					LOG.info("ADCS certificate retrieval for '{}' (url '{}') failed with msg '{}'",
							caConfig.getName(), caConfig.getCaUrl(), th.getMessage());
					LOG.debug("ADCS certificate retrieval", th);
				}
			} else {
				LOG.info("ADCS proxy '{}' disabled", caConfig.getName());
			}

		} else if (CAConnectorType.DIRECTORY.equals(caConfig.getCaConnectorType())) {
			LOG.debug("CAConnectorType DIRECTORY for " + caConfig.getCaUrl());

			try {

				int nNewCerts = dirConnector.retrieveCertificates(caConfig);

				if (nNewCerts > 0) {
					LOG.info("Directory certificate retrieval for '{}' (url '{}') processed {} certificates",
							caConfig.getName(), caConfig.getCaUrl(), nNewCerts);
					caConfigRepo.save(caConfig);
				} else {
					LOG.debug("Directory certificate retrieval for '{}' (url '{}') found no new certificates",
							caConfig.getName(), caConfig.getCaUrl());
				}
			} catch (Throwable th) {
				LOG.info("Directory certificate retrieval for '{}' (url '{}') failed with msg '{}'",
						caConfig.getName(), caConfig.getCaUrl(), th.getMessage());
				LOG.debug("Directory certificate retrieval", th);
			}

		} else {
			LOG.debug("CAConnectorType '{}' not suitable for certificate retrieval", conType);
		}
	}
}
