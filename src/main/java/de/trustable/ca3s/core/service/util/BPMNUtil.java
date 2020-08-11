package de.trustable.ca3s.core.service.util;

import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.enumeration.BPNMProcessType;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.BPNMProcessInfoRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
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

	@Autowired
    private RepositoryService repoService;

	@Autowired
    private BPNMProcessInfoRepository bpnmInfoRepo;

	@Autowired
	private CSRRepository csrRepository;
	
	@Autowired
	private CertificateRepository certRepository;
	
	
	public List<ProcessDefinition> getProcessDefinitions(){
		
		return repoService.createProcessDefinitionQuery().latestVersion().list();
	}

	public void updateProcessDefinitions(){
		
		List<ProcessDefinition> pdList = getProcessDefinitions();
		for(ProcessDefinition pd: pdList ) {
			Optional<BPNMProcessInfo> optBI = bpnmInfoRepo.findByName(pd.getKey());
			if( !optBI.isPresent() ) {
				BPNMProcessInfo newBI = new BPNMProcessInfo();
				newBI.setAuthor("system");
				newBI.setLastChange(Instant.now());
				newBI.setName(pd.getKey());
				
				String version = pd.getVersionTag();
				if( version == null) {
					version = "0.0.1";
				}
				
				newBI.setVersion(version);
				
				// @todo determine the type properly
				newBI.setType(BPNMProcessType.CA_INVOCATION);
				
				// @todo calculate a signature
				newBI.setSignatureBase64("");
				
				LOG.info("added new BPNMProcessInfo from camunda database: {}", newBI);

				bpnmInfoRepo.save(newBI);
								
			}else {
				// @todo check for updates
				LOG.debug("BPNMProcessInfo {} already exists", pd.getKey());
			}
		}
	}


	/**
	 *
	 * @param csr
	 * @return
	 */
	public Certificate startCertificateCreationProcess(CSR csr)  {

		CAConnectorConfig caConfig = null;
		BPNMProcessInfo pi = null;

		if( csr.getPipeline() == null) {
			LOG.warn("No pipeline information in CSR #{}", csr.getId());
			caConfig = configUtil.getDefaultConfig();
		}else {
			caConfig = csr.getPipeline().getCaConnector();
			pi = csr.getPipeline().getProcessInfo();
		}

		return startCertificateCreationProcess(csr, caConfig, pi);
	}
	
	/**
	 *
	 * @param csr
	 * @return
	 */
	public Certificate startCertificateCreationProcess(CSR csr, CAConnectorConfig caConfig, BPNMProcessInfo processInfo)  {
		
		String status = "Failed";
		String certificateId = "";
		Certificate certificate = null;
		String failureReason = "";
		String processInstanceId = "";

		String processName = "CAInvocationProcess";

		if( processInfo != null) {
			processName = processInfo.getName();
		}
		
		if(caConfig != null ){
			
			if(processName != null && (processName.trim().length() > 0 )) {
				// BPNM call
				try {
					Map<String, Object> variables = new HashMap<String,Object>();
					variables.put("csrId", csr.getId());
					variables.put("csr", csr);
					variables.put("caConfigId", caConfig.getId());
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
					certificate = caConnAdapter.signCertificateRequest(csr, caConfig);
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
				
				certificate.setCsr(csr);
				certRepository.save(certificate);
				
				csr.setCertificate(certificate);
				csr.setStatus(CsrStatus.ISSUED);
				csrRepository.save(csr);
				
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
	 *
	 * @throws GeneralSecurityException 
	 */
	public void startCertificateRevoctionProcess(Certificate certificate, final CRLReason crlReason, final Date revocationDate) throws GeneralSecurityException  {
		
		String status = "Failed";
		String failureReason = "";
		String processInstanceId = "";

//		String processName = null;
		String processName = "CAInvocationProcess";
		
		if( certificate == null) {
			throw new GeneralSecurityException("certificate to be revoked MUST be provided");
		}
		
		if( crlReason == null) {
			throw new GeneralSecurityException("revocation reason for certificate "+certificate.getId()+" MUST be provided" );
		}
		
		if( revocationDate == null) {
			throw new GeneralSecurityException("revocation date for certificate "+certificate.getId()+" MUST be provided" );
		}
		
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
			LOG.debug("certificate id {} revoked by BPMN process {}", certificate.getId(), processInstanceId);
		} else {
			LOG.warn("revocation of certificate by BPMN process {} failed with reason '{}' ", processInstanceId, failureReason);
			throw new GeneralSecurityException(failureReason);
		}

	}

}
