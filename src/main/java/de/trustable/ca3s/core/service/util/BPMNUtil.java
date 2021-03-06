package de.trustable.ca3s.core.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
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
    private CAConnectorConfigRepository caConnConRepo;

	@Autowired
	private CryptoUtil cryptoUtil;

	@Autowired
    private RuntimeService runtimeService;

	@Autowired
    private RepositoryService repoService;

	@Autowired
    private BPMNProcessInfoRepository bpnmInfoRepo;

	@Autowired
	private CSRRepository csrRepository;

	@Autowired
	private CertificateRepository certRepository;

    @Autowired
    private CertificateUtil certUtil;

    private final NameAndRoleUtil nameAndRoleUtil;

    public BPMNUtil(NameAndRoleUtil nameAndRoleUtil) {
        this.nameAndRoleUtil = nameAndRoleUtil;
    }

    public String addModel(final String bpmnString, final String name){

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
            new ByteArrayInputStream(bpmnString.getBytes(StandardCharsets.UTF_8)));

        return repoService.createDeployment().addModelInstance(name + ".bpmn20.xml", modelInstance).deploy().getId();
    }

    public List<ProcessDefinition> getProcessDefinitions(){

		return repoService.createProcessDefinitionQuery().latestVersion().list();
	}

    public InputStream getProcessContent(final String processId){

        return repoService.getProcessModel(processId);
    }

    public void updateProcessDefinitions(){

		List<ProcessDefinition> pdList = getProcessDefinitions();
		for(ProcessDefinition pd: pdList ) {
			Optional<BPMNProcessInfo> optBI = bpnmInfoRepo.findByName(pd.getKey());
			if( !optBI.isPresent() ) {
                buildBPMNProcessInfo(pd, pd.getKey(), BPMNProcessType.CA_INVOCATION);

            }else {
				// @todo check for updates
				LOG.debug("BPNMProcessInfo {} already exists", pd.getKey());
			}
		}
	}

    public BPMNProcessInfo buildBPMNProcessInfo(final String id,
                                                final String name,
                                                final BPMNProcessType bpmnProcessType) {
        return buildBPMNProcessInfo(
            repoService.getProcessDefinition(id), name, bpmnProcessType);
    }

    public BPMNProcessInfo buildBPMNProcessInfo(final ProcessDefinition pd,
                                                 final String name,
                                                 final BPMNProcessType bpmnProcessType) {
        BPMNProcessInfo newBI = new BPMNProcessInfo();

        newBI.setAuthor(nameAndRoleUtil.getNameAndRole().getName());

        newBI.setLastChange(Instant.now());
        newBI.setName(name);

        String version = pd.getVersionTag();
        if( version == null) {
            version = "0.0.1";
        }
        newBI.setVersion(version);

        newBI.setType(bpmnProcessType);

        // @todo calculate a signature
        newBI.setSignatureBase64("1234");

        newBI.setBpmnHashBase64("bpmnHash");

        newBI.setProcessId(pd.getId());
        LOG.info("added new BPNMProcessInfo from camunda database: {}", newBI);

        bpnmInfoRepo.save(newBI);

        return newBI;
    }

    public void deleteProcessDefinitions(String processId) {
        repoService.deleteProcessDefinitions().byIds(processId).delete();
    }


    /**
	 * Build a certificate object from a CSR
	 * @param csr the given CSR object
	 * @return the created certificate
	 */
	public Certificate startCertificateCreationProcess(CSR csr)  {

		CAConnectorConfig caConfig;
		BPMNProcessInfo pi = null;

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
     * @param csr the given CSR object
     * @param caConfig the ca and its configuration
     * @param processInfo the BPMN process to be excecuted
     * @return the created certificate
     */
	public Certificate startCertificateCreationProcess(CSR csr, CAConnectorConfig caConfig, BPMNProcessInfo processInfo)  {

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
					Map<String, Object> variables = new HashMap<>();
					variables.put("csrId", csr.getId());
					variables.put("csr", csr);
					variables.put("caConfigId", caConfig.getId());
					variables.put("status", "Failed");
					variables.put("certificateId", certificateId);
					variables.put("failureReason", failureReason);

		            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceByKey(processName).setVariables(variables).executeWithVariablesInReturn();
		            processInstanceId = processInstance.getId();
		            LOG.info("ProcessInstance: {}", processInstanceId);

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

			    // connect the certificate and its request
				certificate.setCsr(csr);

				// track which CA issued this certificate. Useful e.g. in case of revocation
                certUtil.setCertAttribute(certificate, CertificateAttribute.ATTRIBUTE_CA_CONNECTOR_ID, caConfig.getId());
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

        String caConnectorId = certUtil.getCertAttribute(certificate, CertificateAttribute.ATTRIBUTE_CA_CONNECTOR_ID);
        if( caConnectorId == null){
            CAConnectorConfig caConfigDefault = configUtil.getDefaultConfig();
            if(caConfigDefault != null ){
                caConnectorId = caConfigDefault.getId().toString();
            }
        }

		if(caConnectorId != null ){

            LOG.debug("revoke certificate '{}' at CA with config '{}' ", certificate.getId(), caConnectorId);

			if(processName != null && (processName.trim().length() > 0 )) {
				// BPNM call
				try {
					Map<String, Object> variables = new HashMap<>();
					variables.put("action", "Revoke");
					variables.put("caConfigId", caConnectorId);
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
                    Optional<CAConnectorConfig> caConfigOpt = caConnConRepo.findById(Long.parseLong(caConnectorId));
                    if( caConfigOpt.isPresent()) {
                        caConnAdapter.revokeCertificate(certificate, crlReason, revocationDate, caConfigOpt.get());
                        status = "Revoked";
                    }else{
                        failureReason = "caConnectorId '" + caConnectorId + "' is unknown";
                        LOG.error(failureReason);
                    }
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
