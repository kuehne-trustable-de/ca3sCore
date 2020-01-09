package de.trustable.ca3s.core.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;

@Service
public class ConfigUtil {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigUtil.class);

	@Autowired
	private CAConnectorConfigRepository caccRepo;
	
	public CAConnectorConfig getDefaultConfig() {
		
		CAConnectorConfig caConfigDefault = null;
		for( CAConnectorConfig caConfig: caccRepo.findAll()) {
			LOG.debug("checking CA configuration {}, default {}", caConfig.getName(), caConfig.isDefaultCA() );

			if( caConfig.isDefaultCA() && caConfig.isActive()) {
				caConfigDefault = caConfig;
				LOG.debug("default CA configuration {}, default {}", caConfig.getName(), caConfig.isDefaultCA() );
				break;
			}
		}
		if(caConfigDefault == null ) {
			LOG.error("no default and active CA configured" );
		}
		
		return caConfigDefault;
	}
}
