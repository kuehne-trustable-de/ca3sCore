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
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.util.CryptoUtil;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BPMNUtil{

    private static final Logger LOG = LoggerFactory.getLogger(BPMNUtil.class);
    public static final String CAINVOCATION_PROCESS = "CAInvocationProcess";

    private final ConfigUtil configUtil;

    private final CaConnectorAdapter caConnAdapter;

    private final CAConnectorConfigRepository caConnConRepo;

	private final CryptoUtil cryptoUtil;

    private final RuntimeService runtimeService;

    private final RepositoryService repoService;

    private final BPMNProcessInfoRepository bpnmInfoRepo;

	private final CSRRepository csrRepository;

	private final CertificateRepository certRepository;

    private final CertificateUtil certUtil;

    private final NameAndRoleUtil nameAndRoleUtil;

    final private AuditService auditService;

    final private BPMNAsyncUtil bpmnAsyncUtil;
    final private boolean useDefaultProcess;

    @Autowired
    public BPMNUtil(ConfigUtil configUtil,
                    CaConnectorAdapter caConnAdapter,
                    CAConnectorConfigRepository caConnConRepo,
                    CryptoUtil cryptoUtil,
                    RuntimeService runtimeService,
                    RepositoryService repoService,
                    BPMNProcessInfoRepository bpnmInfoRepo,
                    CSRRepository csrRepository,
                    CertificateRepository certRepository,
                    CertificateUtil certUtil,
                    NameAndRoleUtil nameAndRoleUtil,
                    AuditService auditService,
                    BPMNAsyncUtil bpmnAsyncUtil,
    @Value("${ca3s.bpmn.use-default-process:false}") boolean useDefaultProcess) {

        this.configUtil = configUtil;
        this.caConnAdapter = caConnAdapter;
        this.caConnConRepo = caConnConRepo;
        this.cryptoUtil = cryptoUtil;
        this.runtimeService = runtimeService;
        this.repoService = repoService;
        this.bpnmInfoRepo = bpnmInfoRepo;
        this.csrRepository = csrRepository;
        this.certRepository = certRepository;
        this.certUtil = certUtil;
        this.nameAndRoleUtil = nameAndRoleUtil;
        this.auditService = auditService;
        this.bpmnAsyncUtil = bpmnAsyncUtil;
        this.useDefaultProcess = useDefaultProcess;
    }

    public String addModel(final String bpmnString, final String name){

        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
            new ByteArrayInputStream(bpmnString.getBytes(StandardCharsets.UTF_8)));

        String modelName = name + ".bpmn20.xml";
        Deployment deployment = repoService.createDeployment().addModelInstance(modelName, modelInstance).deploy();
        LOG.debug( "deployment with name {} and if {} is a {}", modelName, deployment.getId(), deployment.getClass().getName() );

        for(ProcessDefinition pd: getProcessDefinitions()){
            LOG.debug( "process definition with name {}, id {}, versionTag {}, deploymentId {}, key {} found", pd.getName(), pd.getId(), pd.getVersionTag(), pd.getDeploymentId(),
                pd.getKey());
        }

        ProcessDefinition pdNew = repoService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list().get(0);
        LOG.debug( "New process definition Id '{}'", pdNew.getId() );
        return pdNew.getId();
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
			List<BPMNProcessInfo> bpmnProcessInfoList = bpnmInfoRepo.findByNameOrderedBylastChange(pd.getKey());
			if( bpmnProcessInfoList.isEmpty() ) {
                buildBPMNProcessInfoByProcess(pd, pd.getKey(), BPMNProcessType.CERTIFICATE_CREATION);
            }else {
				// @todo check for updates
				LOG.debug("BPNMProcessInfo {} already exists", pd.getKey());
			}
		}
	}

    public BPMNProcessInfo buildBPMNProcessInfoByProcessId(final String processId,
                                                           final String name,
                                                           final BPMNProcessType bpmnProcessType) {

        List<ProcessDefinition> pdList = repoService.createProcessDefinitionQuery().processDefinitionId(processId).list();
        if( pdList.isEmpty()){
            LOG.debug("retrieving ProcessDefinition for id '{}' failed ...", processId);
        }

        return buildBPMNProcessInfoByProcess(pdList.get(0), name, bpmnProcessType);
    }

    public BPMNProcessInfo buildBPMNProcessInfoByProcess(final ProcessDefinition pd,
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
        Pipeline pipeline = null;

		if( csr.getPipeline() == null) {
			LOG.warn("No pipeline information in CSR #{}", csr.getId());
			caConfig = configUtil.getDefaultConfig();
		}else {
			caConfig = csr.getPipeline().getCaConnector();
            pipeline = csr.getPipeline();
		}

		return startCertificateCreationProcess(csr, caConfig, pipeline);
	}

    /**
     *
     * @param csr the given CSR object
     * @param caConfig the ca and its configuration
     * @param pipeline the pipeline defining the BPMN processes to be excecuted
     * @return the created certificate
     */
	public Certificate startCertificateCreationProcess(CSR csr, CAConnectorConfig caConfig, Pipeline pipeline)  {

		String status = "Failed";
		String certificateId = "";
		Certificate certificate = null;
		String failureReason = "";

        BPMNProcessInfo bpmnProcessInfo = null;
        if( pipeline != null){
            bpmnProcessInfo = pipeline.getProcessInfoCreate();
        }

		if(caConfig != null ){

			if(bpmnProcessInfo != null ) {
				// BPNM call
				try {
                    Map<String, Object> variables = buildVariableMapFromCSR(csr, caConfig);

                    variables.put("certificateId", certificateId);

                    ProcessInstanceWithVariables processInstance = executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfo, variables);

                    certificate = (Certificate)processInstance.getVariables().get("certificate");
		            status = processInstance.getVariables().get("status").toString();

                    Object reason = processInstance.getVariables().get("failureReason");
		            if( reason != null && !reason.toString().isEmpty()) {
		            	failureReason = processInstance.getVariables().get("failureReason").toString();
		            }else{
                        notifyOnCertificate(pipeline.getProcessInfoNotify(), certificate.getId());
                    }

					// catch all (runtime) Exception
				} catch (Exception e) {
					failureReason = e.getLocalizedMessage();
					LOG.warn("execution of '"+bpmnProcessInfo.getName()+"' failed ", e);
				}
			} else {
				// direct call
				try {
                    certificate = caConnAdapter.signCertificateRequest(csr, caConfig);
                    status = "Created";

                    if(pipeline != null && certificate != null){
                        notifyOnCertificate(pipeline.getProcessInfoNotify(), certificate.getId());
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

				LOG.debug("new certificate id {} created", certificate.getId());
				return certificate;
			}else {
				LOG.warn("creation of certificate failed, no certificate returned");
                throw new CAFailureException();
			}

		} else {
			LOG.warn("creation of certificate by BPMN process failed with reason '{}'", failureReason);
            auditService.saveAuditTrace(auditService.createAuditTraceCsrRejected(csr, failureReason));
            throw new CAFailureException(failureReason);
		}
	}


    public ProcessInstanceWithVariables checkCertificateNotificationProcess(final Certificate certificate, final CAConnectorConfig caConfig, final String processName)  {

        Map<String, Object> variables = new HashMap<>();
        variables.put("certificateId", certificate.getId());
        return executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkCertificateCreationProcess(final CSR csr, final CAConnectorConfig caConfig, final String processName)  {

        Map<String, Object> variables = buildVariableMapFromCSR(csr, caConfig);
        return executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkBatchProcess(final String processName)  {

        Map<String, Object> variables = new HashMap<>();
        variables.put("now", new Date());
//        variables.put("certRepository", certRepository);

        return executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkAccountRequest(final String processName)  {

        Map<String, Object> variables = new HashMap<>();
        variables.put("action", "CreateAccount");
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setStatus(AccountStatus.PENDING);
        accountRequest.setTermsAgreed(true);
        accountRequest.getContacts().add("foo@bar.baz");

        variables.put("accountRequest", accountRequest);
        variables.put("status", "Failed");
        variables.put("failureReason", "");

        return executeBPMNProcessByName(processName, variables);
    }

    @NotNull
    private Map<String, Object> buildVariableMapFromCSR(CSR csr, CAConnectorConfig caConfig) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("csrId", csr.getId());
        variables.put("csr", csr);
        variables.put("csrAttributes", csr.getCsrAttributes());
        variables.put("caConfigId", caConfig.getId());
        return variables;
    }


    private ProcessInstanceWithVariables executeBPMNProcessByName(final String processNameId, Map<String, Object> variables){

        LOG.debug("execute BPMN Process with id {} ", processNameId);

        variables.put("status", "Failed");
        variables.put("failureReason", "");

        try {
            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceById(processNameId).setVariables(variables).executeWithVariablesInReturn();
            String processInstanceId = processInstance.getId();
            LOG.info("ProcessInstance: {}", processInstanceId);
            return processInstance;
        }catch(RuntimeException processException){
            if(LOG.isDebugEnabled()){
                LOG.debug("Exception while calling bpmn process '"+processNameId+"'", processException);
            }
            throw processException;
        }
    }

    private ProcessInstanceWithVariables executeBPMNProcessByBPMNProcessInfo(final BPMNProcessInfo bpmnProcessInfo, Map<String, Object> variables){

        LOG.debug("execute BPMN Process Info ''{}' ", bpmnProcessInfo.getName());

        variables.put("status", "Failed");
        variables.put("failureReason", "");

        try {
            ProcessInstanceWithVariables processInstance = runtimeService.createProcessInstanceById(bpmnProcessInfo.getProcessId()).setVariables(variables).executeWithVariablesInReturn();
            String processInstanceId = processInstance.getId();
            LOG.info("ProcessInstance: {}", processInstanceId);
            return processInstance;
        }catch(RuntimeException processException){
            if(LOG.isDebugEnabled()){
                LOG.debug("Exception while calling bpmn process '"+bpmnProcessInfo.getProcessId()+"'", processException);
            }
            throw processException;
        }
    }

    /**
     *
     * @param certificate
     *
     * @throws GeneralSecurityException
     */
    public void startCertificateRevocationProcess(Certificate certificate, final CRLReason crlReason, final Date revocationDate) throws GeneralSecurityException  {

        if( certificate == null) {
            throw new GeneralSecurityException("certificate to be revoked MUST be provided");
        }

        if( crlReason == null) {
            throw new GeneralSecurityException("revocation reason for certificate "+certificate.getId()+" MUST be provided" );
        }

        if( revocationDate == null) {
            throw new GeneralSecurityException("revocation date for certificate "+certificate.getId()+" MUST be provided" );
        }

        String status = "Failed";
        String failureReason = "";
        String processInstanceId = "";
        Pipeline pipeline = null;
        BPMNProcessInfo bpmnProcessInfo = null;
        BPMNProcessInfo bpmnProcessInfoNotify = null;


        if((certificate.getCsr() != null) &&
            (certificate.getCsr().getPipeline() != null )){
            pipeline = certificate.getCsr().getPipeline();

            bpmnProcessInfo = pipeline.getProcessInfoRevoke();
            if( bpmnProcessInfo != null) {
                LOG.debug("ProcessInfoRevoke '{}' defined by pipeline '{}' ", bpmnProcessInfo.getName(), pipeline.getName());
            }

            bpmnProcessInfoNotify = pipeline.getProcessInfoNotify();
            if( bpmnProcessInfoNotify != null) {
                LOG.debug("ProcessInfoNotify '{}' defined by pipeline '{}' ", bpmnProcessInfoNotify.getName(), pipeline.getName());
            }
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

            if(bpmnProcessInfo != null) {
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

                    ProcessInstanceWithVariables processInstance = executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfo, variables);
                    processInstanceId = processInstance.getId();
                    LOG.info("ProcessInstance: {}", processInstanceId);

                    status = processInstance.getVariables().get("status").toString();

                    if( processInstance.getVariables().get("failureReason") != null) {
                        failureReason = processInstance.getVariables().get("failureReason").toString();
                    }

                    notifyOnCertificate(bpmnProcessInfoNotify, certificate.getId());

                    // catch all (runtime) Exception
                } catch (Exception e) {
                    failureReason = e.getLocalizedMessage();
                    LOG.warn("execution of '" + bpmnProcessInfo.getName() + "' failed ", e);
                }
            } else {
                // direct call
                try {
                    Optional<CAConnectorConfig> caConfigOpt = caConnConRepo.findById(Long.parseLong(caConnectorId));
                    if( caConfigOpt.isPresent()) {
                        caConnAdapter.revokeCertificate(certificate, crlReason, revocationDate, caConfigOpt.get());
                        notifyOnCertificate(bpmnProcessInfoNotify, certificate.getId());
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

    /**
     *
     * @param accountRequest
     *
     * @throws GeneralSecurityException
     */
    public void startACMEAccountCreationProcess(final AccountRequest accountRequest, final Pipeline pipeline) throws GeneralSecurityException  {

        if( accountRequest == null) {
            throw new GeneralSecurityException("accountRequest for ACME account creation MUST be provided");
        }

        if( pipeline == null) {
            throw new GeneralSecurityException("pipeline for ACME account creation MUST be provided" );
        }

        String status = "Failed";
        String failureReason = "";
        String processInstanceId = "";

        BPMNProcessInfo bpmnProcessInfoAccountCreate = pipeline.getProcessInfoRequestAuthorization();

        if( bpmnProcessInfoAccountCreate == null){
            return;
        }

        // BPNM call
        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("action", "CreateAccount");
            variables.put("accountRequest", accountRequest);
            variables.put("status", status);
            variables.put("failureReason", failureReason);

            ProcessInstanceWithVariables processInstance = executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfoAccountCreate, variables);
            processInstanceId = processInstance.getId();
            LOG.info("startACMEAccountCreationProcess ProcessInstance: {}", processInstanceId);

            status = processInstance.getVariables().get("status").toString();

            if( processInstance.getVariables().get("failureReason") != null) {
                failureReason = processInstance.getVariables().get("failureReason").toString();
            }

            // catch all (runtime) Exception
        } catch (Exception e) {
            failureReason = e.getLocalizedMessage();
            LOG.warn("execution of '" + bpmnProcessInfoAccountCreate.getName() + "' failed ", e);
        }


        if ("Success".equals(status)) {
            LOG.debug("ACME account creation confirmed by BPMN process {}", processInstanceId);
        } else {
            LOG.warn("ACME account creation check by BPMN process {} failed with reason '{}' ", processInstanceId, failureReason);
            throw new GeneralSecurityException(failureReason);
        }
    }


    private void notifyOnCertificate(BPMNProcessInfo processInfo, long certificateId) {
        if (processInfo != null && processInfo.getProcessId() != null) {
            LOG.info("notifyOnCertificate: '{}', {}", processInfo.getProcessId(), certificateId);

            executeAfterTransactionCommits(() -> {
                bpmnAsyncUtil.onChange(processInfo.getProcessId(), certificateId);

            });
        }else{
            LOG.info("notifyOnCertificate: no notify process defined ");
        }
    }

    private void executeAfterTransactionCommits(Runnable task) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            public void afterCommit() {
                LOG.info( "in afterCommit ");
                task.run();
            }
        });
    }
}
