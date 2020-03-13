package de.trustable.ca3s.core.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import de.trustable.ca3s.core.service.util.BPMNUtil;

@Component
public class StartupApplicationListener implements
  ApplicationListener<ContextRefreshedEvent> {
 
	Logger LOG = LoggerFactory.getLogger(StartupApplicationListener.class);

	@Autowired
	BPMNUtil bpmnUtil;
	
    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        LOG.info("Basic application startup finished. Starting ca3s startup tasks");
        
        bpmnUtil.updateProcessDefinitions();
    }
}