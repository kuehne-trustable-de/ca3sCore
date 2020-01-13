package de.trustable.ca3s.core.service.util;

import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.util.CryptoUtil;

@Service
public class BPMNUtil{

	private static final Logger LOG = LoggerFactory.getLogger(BPMNUtil.class);

	@Autowired
	private ConfigUtil configUtil;

	@Autowired
	private CaConnectorAdapter caConnAdapter;

	@Autowired
	private CryptoUtil cryptoUtil;
	
	@Autowired
    private RuntimeService runtimeService;

	/**
	 *
	 * @param csr
	 * @return
	 */
	public Certificate startCertificateCreationProcess(CSR csr)  {
		
		String status = "Failed";
		String certificateId = "";
		Certificate certificate = null;
		String failureReason = "";
		String processInstanceId = "";

//		String processName = null;
		String processName = "CAInvocationProcess";
		
		
		CAConnectorConfig caConfigDefault = configUtil.getDefaultConfig();
		if(caConfigDefault != null ){
			
			if(processName != null && (processName.trim().length() > 0 )) {
				// BPNM call
				try {
					Map<String, Object> variables = new HashMap<String,Object>();
					variables.put("csrId", csr.getId());
					variables.put("csr", csr);
					variables.put("caConfigId", caConfigDefault.getId());
					variables.put("status", "Failed");
					variables.put("certificateId", certificateId);
					variables.put("failureReason", failureReason);
					
		            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey(processName).setVariables(variables).executeWithVariablesInReturn();
		            processInstanceId = processInstance.getId();
		            LOG.info("ProcessInstance: {}", processInstanceId);
		
		            certificateId = processInstance.getVariables().get("certificateId").toString();
		            certificate = (Certificate)processInstance.getVariables().get("certificate");
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
				// direct call
				try {
					certificate = caConnAdapter.signCertificateRequest(csr, caConfigDefault);
					status = "Created";
				} catch (GeneralSecurityException e) {
					
					failureReason = e.getLocalizedMessage();
					LOG.error(failureReason);
				}

			}
				
		} else {
			failureReason = "no default and active CA configured";
			LOG.error(failureReason);
		} 
		

		// end of BPMN call
		
		if ("Created".equals(status)) {

			if( certificate != null) {
				LOG.debug("new certificate id {} created by BPMN process {}", certificate.getId(), processInstanceId);
				return certificate;
			}else {
				LOG.warn("creation of certificate by BPMN process {} failed, no certificate returned ", processInstanceId);
			}
			
		} else {
			LOG.warn("creation of certificate by BPMN process {} failed with reason '{}' ", processInstanceId, failureReason);
		}
		return null;
	}

	/**
	 *
	 * @param certificate
	 * @return
	 */
	public void startCertificateRevoctionProcess(Certificate certificate, final CRLReason crlReason, final Date revocationDate)  {
		
		String status = "Failed";
		String failureReason = "";
		String processInstanceId = "";

//		String processName = null;
		String processName = "CAInvocationProcess";
		
		
		CAConnectorConfig caConfigDefault = configUtil.getDefaultConfig();
		if(caConfigDefault != null ){
			
			if(processName != null && (processName.trim().length() > 0 )) {
				// BPNM call
				try {
					Map<String, Object> variables = new HashMap<String,Object>();
					variables.put("action", "Revoke");
					variables.put("caConfigId", caConfigDefault.getId());
					variables.put("status", "Failed");
					variables.put("certificateId", certificate.getId());
					variables.put("certificate", certificate);
					variables.put("revocationReason", cryptoUtil.crlReasonAsString(crlReason));
					variables.put("revocationDate", revocationDate.getTime());
					
					variables.put("failureReason", failureReason);
					
		            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey(processName).setVariables(variables).executeWithVariablesInReturn();
		            processInstanceId = processInstance.getId();
		            LOG.info("ProcessInstance: {}", processInstanceId);
		
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
				// direct call
				try {
					caConnAdapter.revokeCertificate(certificate, crlReason, revocationDate, caConfigDefault);
					status = "Revoked";
				} catch (GeneralSecurityException e) {
					failureReason = e.getLocalizedMessage();
					LOG.error(failureReason);
				}

			}
				
		} else {
			failureReason = "no default and active CA configured";
			LOG.error(failureReason);
		} 
		

		// end of BPMN call
		
		if ("Revoked".equals(status)) {

			if( certificate != null) {
				LOG.debug("certificate id {} revoked by BPMN process {}", certificate.getId(), processInstanceId);
			}else {
				LOG.warn("revocation of certificate by BPMN process {} failed", processInstanceId);
			}
			
		} else {
			LOG.warn("revocation of certificate by BPMN process {} failed with reason '{}' ", processInstanceId, failureReason);
		}

	}


}
