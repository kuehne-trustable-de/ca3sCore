package de.trustable.ca3s.core.service.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.repository.CertificateRepository;

@Service
public class BPMNUtil{

	private static final Logger LOG = LoggerFactory.getLogger(BPMNUtil.class);

	@Autowired
	private ConfigUtil configUtil;

  	@Autowired
  	private CertificateRepository certificateRepository;

  	
	@Autowired
    private RuntimeService runtimeService;

    /**
     * 
     * @param orderDao
     * @return
     * @throws IOException
     */
	public Certificate startCertificateCreationProcess(CSR csr)  {
		
		String status = "Failed";
		String certificateId = "";
		String failureReason = "";
		String processInstanceId = "";

		CAConnectorConfig caConfigDefault = configUtil.getDefaultConfig();
		if(caConfigDefault != null ){
				
			// BPNM call
			try {
				Map<String, Object> variables = new HashMap<String,Object>();
				variables.put("csrId", csr.getId());
				variables.put("caConfigId", caConfigDefault.getId());
				variables.put("status", "Failed");
				variables.put("certificateId", certificateId);
				variables.put("failureReason", failureReason);
				
	            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey("CAInvocationProcess").setVariables(variables).executeWithVariablesInReturn();
	            processInstanceId = processInstance.getId();
	            LOG.info("ProcessInstance: {}", processInstanceId);
	
	            certificateId = processInstance.getVariables().get("certificateId").toString();
	            status = processInstance.getVariables().get("status").toString();
	
	            if( processInstance.getVariables().get("failureReason") != null) {
	            	failureReason = processInstance.getVariables().get("failureReason").toString();
	            }
	
				// catch all (runtime) Exception
			} catch (Exception e) {
				failureReason = e.getLocalizedMessage();
				LOG.warn("execution of CAInvocationProcess failed ", e);
			}
		} else {
			failureReason = "no default and active CA configured";
			LOG.error(failureReason);
		} 
		

		// end of BPMN call
		
		if ("Created".equals(status)) {

			LOG.debug("new certificate id {} created by BPMN process {}", certificateId, processInstanceId);
			Optional<Certificate> certOpt = certificateRepository.findById(Long.parseLong(certificateId));
			if (certOpt.isPresent()) {
				Certificate cert = certOpt.get();
				return cert;
			}else {
				LOG.warn("creation of certificate by BPMN process {} failed, no certificate found with id '{}' ", processInstanceId, certificateId);
			}
			
		} else {
			LOG.warn("creation of certificate by BPMN process {} failed with reason '{}' ", processInstanceId, failureReason);
		}
		return null;
	}


}
