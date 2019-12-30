package de.trustable.ca3s.core.service.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;

@Component
public class StartupApplicationListener implements
  ApplicationListener<ContextRefreshedEvent> {
 
	Logger LOG = LoggerFactory.getLogger(StartupApplicationListener.class);

 
	@Autowired
	CAConnectorConfigRepository caConfigRepo;

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Basic application startup finished. Starting ca3s startup tasks");
        
    }
}