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
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;

@Service
public class BPMNUtil{

	private static final Logger LOG = LoggerFactory.getLogger(BPMNUtil.class);

	@Autowired
	private CAConnectorConfigRepository caccRepo;

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
		
/*		
		ProcessDefinitionData pdd = processHandler.getProcessDefinitionDataByKey("ImmediateCertificateRequest");
		StartFormDataVO sfdVO = processHandler.getFormData(pdd.getId());
		for (FormPropertyVO formPropertyVO : sfdVO.getListFormProperty()) {
			if ("csr".equalsIgnoreCase(formPropertyVO.getName())) {
				formPropertyVO.setValue(csrAsPem);
			}
		}
		
		ProcessInstance pi = processHandler.startProcessInstance(sfdVO);

		java.util.Map<String, Object> procVars = pi.getProcessVariables();
*/

//		String status = (String) procVars.get("status");
//		String certificateId = "" + procVars.get("certificateId");
//		String failureReason = (String) procVars.get("failureReason");
		String status = "Failed";
		String certificateId = "";
		String failureReason = "";
		String processInstanceId = "";

		CAConnectorConfig caConfig = caccRepo.findDefaultCA().get(0);
		
		// BPNM call
		try {
			Map<String, Object> variables = new HashMap<String,Object>();
			variables.put("csrId", csr.getId());
			variables.put("caConfigId", caConfig.getId());
			variables.put("status", "Failed");
			variables.put("certificateId", certificateId);
			variables.put("failureReason", failureReason);
			
            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey("CAInvocationProcess").setVariables(variables).executeWithVariablesInReturn();
            processInstanceId = processInstance.getId();
            LOG.info("ProcessInstance: {}", processInstanceId);

            certificateId = processInstance.getVariables().get("certificateId").toString();
            status = processInstance.getVariables().get("status").toString();
            failureReason = processInstance.getVariables().get("failureReason").toString();

			// catch all (runtime) Exception
		} catch (Exception e) {
			failureReason = e.getLocalizedMessage();
			LOG.warn("execution of CAInvocationProcess failed ", e);
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
