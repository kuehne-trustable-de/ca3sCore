package de.trustable.ca3s.core.service.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.domain.enumeration.*;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.exception.SMSSendingFailedException;
import de.trustable.ca3s.core.repository.*;
import de.trustable.ca3s.core.exception.CAFailureException;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.BPMNProcessInfoView;
import de.trustable.ca3s.core.service.dto.acme.AccountRequest;
import de.trustable.ca3s.core.service.dto.bpmn.AcmeAccountValidationInput;
import de.trustable.ca3s.core.service.dto.bpmn.BpmnInput;
import de.trustable.ca3s.core.service.dto.bpmn.BpmnOutput;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.CRLReason;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import de.trustable.util.CryptoUtil;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class BPMNUtil{

    private static final Logger LOG = LoggerFactory.getLogger(BPMNUtil.class);
    public static final String HISTORIC_PROCESS_DELETION_REASON = "processOutdated";

    private final ConfigUtil configUtil;

    private final CaConnectorAdapter caConnAdapter;

    private final CAConnectorConfigRepository caConnConRepo;

	private final CryptoUtil cryptoUtil;

    private final RepositoryService repoService;

    private final HistoryService historyService;

    private final BPMNProcessInfoRepository bpnmInfoRepo;
    private final BPMNProcessAttributeRepository bpnmAttributeRepo;

    private final ProtectedContentUtil protectedContentUtil;

    private final CSRRepository csrRepository;

	private final CertificateRepository certRepository;

    private final CertificateUtil certUtil;

    private final NameAndRoleUtil nameAndRoleUtil;

    final private AuditService auditService;

    final private BPMNAsyncUtil bpmnAsyncUtil;
    private final BPMNExecutor bpmnExecutor;

    final private AcmeAccountRepository acmeAccountRepository;

    final private boolean useDefaultProcess;

    @Autowired
    public BPMNUtil(ConfigUtil configUtil,
                    CaConnectorAdapter caConnAdapter,
                    CAConnectorConfigRepository caConnConRepo,
                    CryptoUtil cryptoUtil,
                    RepositoryService repoService,
                    HistoryService historyService,
                    BPMNProcessInfoRepository bpnmInfoRepo,
                    BPMNProcessAttributeRepository bpnmAttributeRepo,
                    ProtectedContentUtil protectedContentUtil,
                    CSRRepository csrRepository,
                    CertificateRepository certRepository,
                    CertificateUtil certUtil,
                    NameAndRoleUtil nameAndRoleUtil,
                    AuditService auditService,
                    BPMNAsyncUtil bpmnAsyncUtil,
                    BPMNExecutor bpmnExecutor,
                    AcmeAccountRepository acmeAccountRepository, @Value("${ca3s.bpmn.use-default-process:false}") boolean useDefaultProcess) {

        this.configUtil = configUtil;
        this.caConnAdapter = caConnAdapter;
        this.caConnConRepo = caConnConRepo;
        this.cryptoUtil = cryptoUtil;
        this.repoService = repoService;
        this.historyService = historyService;
        this.bpnmInfoRepo = bpnmInfoRepo;
        this.bpnmAttributeRepo = bpnmAttributeRepo;
        this.protectedContentUtil = protectedContentUtil;
        this.csrRepository = csrRepository;
        this.certRepository = certRepository;
        this.certUtil = certUtil;
        this.nameAndRoleUtil = nameAndRoleUtil;
        this.auditService = auditService;
        this.bpmnAsyncUtil = bpmnAsyncUtil;
        this.bpmnExecutor = bpmnExecutor;
        this.acmeAccountRepository = acmeAccountRepository;
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

    public void deleteHistoricProcesses(int historicProcessRetentionPeriodDays) {

        Date finishedBeforeLimit = Date.from(Instant.now().minus(historicProcessRetentionPeriodDays, ChronoUnit.DAYS));

        LOG.info("Update removal time for historic instances finished before {} ", finishedBeforeLimit);
        HistoricProcessInstanceQuery query =
            historyService.createHistoricProcessInstanceQuery().finishedBefore(finishedBeforeLimit);

        try {
            historyService.setRemovalTimeToHistoricProcessInstances()
                .absoluteRemovalTime(new Date()) // sets an absolute removal time
                // .clearedRemovalTime()        // resets the removal time to null
                // .calculatedRemovalTime()     // calculation based on the engine's configuration
                .byQuery(query)
                .hierarchical()              // sets a removal time across the hierarchy
                .executeAsync();
            LOG.debug("Update removal time for historic instances scheduled ...");

        } catch (BadUserRequestException badUserRequestException) {
            LOG.info("Problem setting removal time: " + badUserRequestException.getMessage());
        }
        try {
            historyService.deleteHistoricProcessInstancesAsync(query, HISTORIC_PROCESS_DELETION_REASON);
            LOG.debug("starting to delete historic instances ...");
        } catch (BadUserRequestException bure) {
            LOG.info("Problem starting 'historyService.deleteHistoricProcessInstancesAsync': {}", bure.getMessage());
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

                    ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfo, variables);

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
        return bpmnExecutor.executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkCertificateCreationProcess(final CSR csr, final CAConnectorConfig caConfig, final String processName)  {

        Map<String, Object> variables = buildVariableMapFromCSR(csr, caConfig);
        return bpmnExecutor.executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkCsrRequestAuthorization(String processName, CSR csr) {
        Map<String, Object> variables = buildVariableMapFromCSR(csr, null);
        return bpmnExecutor.executeBPMNProcessByName(processName, variables);
    }

    public ProcessInstanceWithVariables checkBatchProcess(final String processName)  {

        Map<String, Object> variables = new HashMap<>();
        variables.put("now", new Date());
//        variables.put("certRepository", certRepository);

        return bpmnExecutor.executeBPMNProcessByName(processName, variables);
    }

    public void startSMSProcess(final String phone, final String msg)  {

        List<BPMNProcessInfo> bpmnProcessInfoList = bpnmInfoRepo.findByType(BPMNProcessType.SEND_SMS);
        if( bpmnProcessInfoList.isEmpty()){
            throw new SMSSendingFailedException("No BPMN Process Info with type BPMNProcessType.SEND_SMS not present!");
        }else if( bpmnProcessInfoList.size() > 1) {
            throw new SMSSendingFailedException("Too many BPMN Process Info with type BPMNProcessType.SEND_SMS present!");
        }else {

            BPMNProcessInfo smsProcessInfo = bpmnProcessInfoList.get(0);

            ProcessInstanceWithVariables processInstanceWithVariables = checkSMSProcess(smsProcessInfo.getProcessId(), phone, msg);

            Object reason = processInstanceWithVariables.getVariables().get("failureReason");
            if( reason != null && !reason.toString().isEmpty()) {
                throw new SMSSendingFailedException(reason.toString());
            }
        }
    }

    public ProcessInstanceWithVariables checkAcmeAccountAuthorizationProzess(final String processName, final String accountId) {

        Map<String, Object> variables = new HashMap<>();
        Optional<AcmeAccount> acmeAccountOptional = acmeAccountRepository.findById(Long.parseLong(accountId));
        if (acmeAccountOptional.isPresent()) {

            AcmeAccount acmeAccount = acmeAccountOptional.get();

            AccountRequest accountRequest = new AccountRequest();
            accountRequest.setStatus(AccountStatus.PENDING);
            accountRequest.setTermsAgreed(true);
            for( AcmeContact acmeContact : acmeAccount.getContacts()){
                accountRequest.getContacts().add(acmeContact.getContactUrl());
            }
            variables.put("accountRequest", accountRequest);
            return bpmnExecutor.executeBPMNProcessByName(processName, variables);
        } else {
            throw new RuntimeException( "accountId '" + accountId + "' is unknown");
        }
    }

    public ProcessInstanceWithVariables checkSMSProcess(final String processName, final String phone, final String msg)  {

        Map<String, Object> variables = new HashMap<>();
        variables.put("phone", phone);
        variables.put("msg", msg);

        return bpmnExecutor.executeBPMNProcessByName(processName, variables);
    }

    @NotNull
    private Map<String, Object> buildVariableMapFromCSR(CSR csr, CAConnectorConfig caConfig) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("csrId", csr.getId());
        variables.put("csr", csr);
        variables.put("csrAttributes", csr.getCsrAttributes());
        if( caConfig != null) {
            variables.put("caConfigId", caConfig.getId());
        }
        return variables;
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

                    ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfo, variables);
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
     * @param pipeline
     * @param acmeAccountValidationInput
     * @throws GeneralSecurityException
     */
    public void startACMEAccountAuthorizationProcess(final Pipeline pipeline, final AcmeAccountValidationInput acmeAccountValidationInput) throws GeneralSecurityException  {

        if( acmeAccountValidationInput == null) {
            throw new GeneralSecurityException("acmeAccountValidationInput for ACME account authorization MUST be provided");
        }
        if( acmeAccountValidationInput.getAccountRequest() == null) {
            throw new GeneralSecurityException("accountRequest for ACME account authorization MUST be provided");
        }

        if( pipeline == null) {
            throw new GeneralSecurityException("pipeline for ACME account authorization MUST be provided" );
        }

        BpmnOutput bpmnOutput = new BpmnOutput();

        BPMNProcessInfo bpmnProcessInfoAccountAuthorization = pipeline.getProcessInfoAccountAuthorization();
        if( bpmnProcessInfoAccountAuthorization == null){
            return;
        }

        // BPMN call
        try {
            ProcessInstanceWithVariables processInstance = bpmnExecutor.executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfoAccountAuthorization,
                acmeAccountValidationInput.getVariables());

            bpmnOutput = new BpmnOutput(processInstance);
            LOG.info("startACMEAccountAuthorizationProcess ProcessInstance: {} exited with status {}",
                bpmnOutput.getProcessInstanceId(),
                bpmnOutput.getStatus());

            // catch all (runtime) Exception
        } catch (Exception e) {
            bpmnOutput = new BpmnOutput(e);
            LOG.warn("execution of '" + bpmnProcessInfoAccountAuthorization.getName() + "' failed ", e);
        }

        if (BpmnInput.SUCCESS.equals(bpmnOutput.getStatus())) {
            LOG.debug("ACME account creation confirmed by BPMN process {}", bpmnOutput.getProcessInstanceId());
        } else {
            LOG.warn("ACME account creation check by BPMN process {} failed with reason '{}' ", bpmnOutput.getProcessInstanceId(), bpmnOutput.getFailureReason());
            throw new GeneralSecurityException(bpmnOutput.getFailureReason());
        }
    }


    private void notifyOnCertificate(BPMNProcessInfo processInfo, long certificateId) {
        if (processInfo != null && processInfo.getProcessId() != null) {
            if( !TransactionSynchronizationManager.isActualTransactionActive()){
                LOG.warn("notifyOnCertificate: no transaction active !");
            }

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            LOG.info("notifyOnCertificate: '{}', {} for auth {}", processInfo.getProcessId(), certificateId, auth);

            executeAfterTransactionCommits(() -> {
                bpmnAsyncUtil.onChange(processInfo.getProcessId(), certificateId, auth);
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
    public BPMNProcessInfoView toBPMNProcessInfoView( BPMNProcessInfo bpmnProcessInfo) {
        BPMNProcessInfoView bpmnProcessInfoView = new BPMNProcessInfoView();
        bpmnProcessInfoView.setId(bpmnProcessInfo.getId());
        bpmnProcessInfoView.setName(bpmnProcessInfo.getName());
        bpmnProcessInfoView.setType(bpmnProcessInfo.getType());
        bpmnProcessInfoView.setVersion(bpmnProcessInfo.getVersion());
        bpmnProcessInfoView.setAuthor(bpmnProcessInfo.getAuthor());
        bpmnProcessInfoView.setLastChange(bpmnProcessInfo.getLastChange());
        bpmnProcessInfoView.setBpmnHashBase64(bpmnProcessInfo.getBpmnHashBase64());
        bpmnProcessInfoView.setProcessId(bpmnProcessInfo.getProcessId());

        List<BPMNProcessAttribute> bpmnProcessAttributelist = new ArrayList<>();
        for(BPMNProcessAttribute bpmnProcessAttribute: bpmnProcessInfo.getBpmnProcessAttributes()){
            BPMNProcessAttribute bpmnProcessAttributeNew = new BPMNProcessAttribute();
            bpmnProcessAttributeNew.setId(bpmnProcessAttribute.getId());
            bpmnProcessAttributeNew.setName(bpmnProcessAttribute.getName());
            bpmnProcessAttributeNew.setValue(bpmnProcessAttribute.getValue());
            bpmnProcessAttributeNew.setProtectedContent(bpmnProcessAttribute.getProtectedContent());
            bpmnProcessAttributelist.add(bpmnProcessAttributeNew);
        }
        bpmnProcessInfoView.setBpmnProcessAttributes(
            bpmnProcessAttributelist.toArray(new BPMNProcessAttribute[0]));

        return bpmnProcessInfoView;
    }

    public BPMNProcessInfo toBPMNProcessInfo(BPMNProcessInfoView bpmnProcessInfoView) {

        List<AuditTrace> auditList = new ArrayList<>();
        BPMNProcessInfo bpmnProcessInfo;
        Optional<BPMNProcessInfo> bpmnProcessInfoOptByName = bpnmInfoRepo.findByName(bpmnProcessInfoView.getName());
        if (bpmnProcessInfoView.getId() != null) {
            // handle existing entity

            Optional<BPMNProcessInfo> optP = bpnmInfoRepo.findById(bpmnProcessInfoView.getId());
            if (optP.isPresent()) {
                // given id is present
                bpmnProcessInfo = optP.get();
                if (bpmnProcessInfoOptByName.isPresent() &&
                    !bpmnProcessInfoOptByName.get().getId().equals(bpmnProcessInfo.getId())) {
                    throw new BadRequestAlertException("Name '" + bpmnProcessInfoView.getName() + "' already assigned", "BPMN", "name already used");
                }
            } else {
                // given id is not present
                if (bpmnProcessInfoOptByName.isPresent()) {
                    throw new BadRequestAlertException("Name '" + bpmnProcessInfoView.getName() + "' already assigned", "BPMN", "name already used");
                }
                bpmnProcessInfo = createBPMNProcessInfo(bpmnProcessInfoView, auditList);
            }
        } else {
            // handle new entity
            if (bpmnProcessInfoOptByName.isPresent()) {
                throw new BadRequestAlertException("Name '" + bpmnProcessInfoOptByName.get().getName() + "' already assigned", "pipeline", "name already used");
            }
            bpmnProcessInfo = createBPMNProcessInfo(bpmnProcessInfoView, auditList);
        }

        if (!Objects.equals(bpmnProcessInfoView.getName(), bpmnProcessInfo.getName())) {
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_NAME_CHANGED, bpmnProcessInfo.getName(), bpmnProcessInfoView.getName(), bpmnProcessInfo));
            bpmnProcessInfo.setName(bpmnProcessInfoView.getName());
        }

        if (!Objects.equals(bpmnProcessInfoView.getName(), bpmnProcessInfo.getName())) {
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_TYPE_CHANGED, bpmnProcessInfo.getType().toString(), bpmnProcessInfoView.getType().toString(), bpmnProcessInfo));
            bpmnProcessInfo.setType(bpmnProcessInfoView.getType());
        }

        if (!Objects.equals(bpmnProcessInfoView.getAuthor(), bpmnProcessInfo.getAuthor())) {
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_AUTHOR_CHANGED, bpmnProcessInfo.getAuthor(), bpmnProcessInfoView.getAuthor(), bpmnProcessInfo));
            bpmnProcessInfo.setAuthor(bpmnProcessInfoView.getAuthor());
        }

        if (!Objects.equals(bpmnProcessInfoView.getVersion(), bpmnProcessInfo.getVersion())) {
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_VERSION_CHANGED, bpmnProcessInfo.getVersion(), bpmnProcessInfoView.getVersion(), bpmnProcessInfo));
            bpmnProcessInfo.setVersion(bpmnProcessInfoView.getVersion());
        }

        if (!Objects.equals(bpmnProcessInfoView.getProcessId(), bpmnProcessInfo.getProcessId())) {
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_PROCESS_ID_CHANGED, bpmnProcessInfo.getProcessId(), bpmnProcessInfoView.getProcessId(), bpmnProcessInfo));
            bpmnProcessInfo.setProcessId(bpmnProcessInfoView.getProcessId());
        }

        handleAttributes(bpmnProcessInfoView.getBpmnProcessAttributes(), auditList, bpmnProcessInfo);

        bpnmInfoRepo.save(bpmnProcessInfo);

        return bpmnProcessInfo;
    }

    public void handleAttributes( BPMNProcessAttribute[] bpmnProcessAttributes, List<AuditTrace> auditList, BPMNProcessInfo bpmnProcessInfo) {
        if(bpmnProcessAttributes ==null) {
            bpmnProcessAttributes = new BPMNProcessAttribute[0];
        }

        for (BPMNProcessAttribute attributeNew : bpmnProcessAttributes) {
            if (StringUtils.isBlank(attributeNew.getName())) {
                continue;
            }
            boolean isNewAttribute = true;
            for (BPMNProcessAttribute attributeOld : bpmnProcessInfo.getBpmnProcessAttributes()) {
                if (attributeNew.getName().equals(attributeOld.getName())) {
                    // update
                    if( Boolean.TRUE.equals(attributeNew.getProtectedContent())){
                        changeProtectedAttribute(attributeNew, attributeOld, bpmnProcessInfo, auditList);
                    }else {
                        changeAttribute(attributeNew, attributeOld, bpmnProcessInfo, auditList);
                    }
                    isNewAttribute = false;
                }
            }
            if (isNewAttribute) {
                if( Boolean.TRUE.equals(attributeNew.getProtectedContent())){
                    changeProtectedAttribute(attributeNew, null, bpmnProcessInfo, auditList);
                }else {
                    changeAttribute(attributeNew, null, bpmnProcessInfo, auditList);
                }
            }
        }

        LOG.debug( "in toBPMNProcessInfo, process attributes");
        for( BPMNProcessAttribute attributeOld: bpmnProcessInfo.getBpmnProcessAttributes()){
            boolean isDeletedAttribute = true;
            for( BPMNProcessAttribute attributeNew: bpmnProcessAttributes){
                if( StringUtils.isBlank(attributeNew.getName()) ){
                    continue;
                }
                if( attributeNew.getName().equals(attributeOld.getName()) ){
                    // update
                    isDeletedAttribute = false;
                }
            }
            if( isDeletedAttribute ){

                if( Boolean.TRUE.equals(attributeOld.getProtectedContent())){
                    changeProtectedAttribute(null, attributeOld, bpmnProcessInfo, auditList);
                }else {
                    changeAttribute(null, attributeOld, bpmnProcessInfo, auditList);
                }
            }
        }
    }


    private void changeAttribute(BPMNProcessAttribute attributeNew, BPMNProcessAttribute attributeOld, BPMNProcessInfo bpmnProcessInfo,
                                 List<AuditTrace> auditList) {

        if (attributeNew == null) {
            LOG.debug("in changeAttribute, delete {}", attributeOld.getName());
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeOld.getName(), attributeOld.getValue(), null, bpmnProcessInfo));
            bpnmAttributeRepo.delete(attributeOld);
            bpmnProcessInfo.getBpmnProcessAttributes().remove(attributeOld);
        } else if (attributeOld == null) {
            LOG.debug("in changeAttribute, new attribute {}", attributeNew.getName());
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeNew.getName(), null, attributeNew.getValue(), bpmnProcessInfo));
            attributeNew.setBpmnProcessInfo(bpmnProcessInfo);
            bpnmAttributeRepo.save(attributeNew);
            bpmnProcessInfo.getBpmnProcessAttributes().add(attributeNew);
        } else if (attributeNew.getValue().equals(attributeOld.getValue()) &&
            (attributeNew.getProtectedContent() == attributeOld.getProtectedContent())) {
            // no change
        } else {
            if (!attributeNew.getValue().equals(attributeOld.getValue())) {
                LOG.debug("in changeAttribute, change {} from '{}' to  '{}'", attributeNew.getName(), attributeOld.getValue(), attributeNew.getValue());
                attributeOld.setValue(attributeNew.getValue());
            }
            if (attributeNew.getProtectedContent() != attributeOld.getProtectedContent()) {
                LOG.debug("in changeAttribute, change {} protection status from '{}' to  '{}'", attributeNew.getName(), attributeOld.getProtectedContent(), attributeNew.getProtectedContent());
                attributeOld.setProtectedContent(attributeNew.getProtectedContent());
            }
            bpnmAttributeRepo.save(attributeOld);
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeNew.getName(), attributeOld.getValue(), attributeNew.getValue(), bpmnProcessInfo));
        }

        if (attributeOld != null){
            protectedContentUtil.deleteProtectedContent(
                ProtectedContentType.SECRET,
                ContentRelationType.BPMN_ATTRIBUTE,
                attributeOld.getId());
        }
    }


    private void changeProtectedAttribute(BPMNProcessAttribute attributeNew, BPMNProcessAttribute attributeOld, BPMNProcessInfo bpmnProcessInfo,
                                 List<AuditTrace> auditList){


        if( attributeNew == null) {
            LOG.debug( "in changeAttribute, delete {}", attributeOld.getName());
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeOld.getName(), attributeOld.getValue(),null, bpmnProcessInfo));
            protectedContentUtil.deleteProtectedContent(
                    ProtectedContentType.SECRET,
                    ContentRelationType.BPMN_ATTRIBUTE,
                    attributeOld.getId());
            bpmnProcessInfo.getBpmnProcessAttributes().remove(attributeOld);


        } else if( attributeOld == null){
            LOG.debug( "in changeAttribute, new attribute {}", attributeNew.getName());
            String value = attributeNew.getValue();
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(
                AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeNew.getName(),
                null,
                ProtectedContentUtil.PLAIN_SECRET_PLACEHOLDER,
                bpmnProcessInfo));

            attributeNew.setValue(ProtectedContentUtil.PLAIN_SECRET_PLACEHOLDER);
            attributeNew.setBpmnProcessInfo(bpmnProcessInfo);
            attributeNew = bpnmAttributeRepo.save(attributeNew);
            bpmnProcessInfo.getBpmnProcessAttributes().add(attributeNew);

            protectedContentUtil.createProtectedContent(
                value,
                ProtectedContentType.SECRET,
                ContentRelationType.BPMN_ATTRIBUTE,
                attributeNew.getId());

        }else if( attributeNew.getValue().equals(ProtectedContentUtil.PLAIN_SECRET_PLACEHOLDER)){
            // no change
        }else {

            List<ProtectedContent> oldProtectedContents = protectedContentUtil.retrieveProtectedContent(
                ProtectedContentType.SECRET,
                ContentRelationType.BPMN_ATTRIBUTE,
                attributeOld.getId());

            if( oldProtectedContents.size() < 1 ){

                protectedContentUtil.createProtectedContent(
                    attributeNew.getValue(),
                    ProtectedContentType.SECRET,
                    ContentRelationType.BPMN_ATTRIBUTE,
                    attributeNew.getId());
            }else  if( oldProtectedContents.size() > 1 ) {
                LOG.warn( "in changeProtectedAttribute, too many old protected contents present ({})!", oldProtectedContents.size() );
                for( int i = 1; i < oldProtectedContents.size(); i++ ){
                    protectedContentUtil.deleteProtectedContent(oldProtectedContents.get(i));
                }
            }

            ProtectedContent protectedContent = oldProtectedContents.get(0);
            String value = protectedContentUtil.unprotectString( protectedContent.getContentBase64());

            if(!attributeNew.getValue().equals(value)) {
                LOG.debug("in changeAttribute, change {} from '{}' to  '{}'", attributeNew.getName(), value, attributeNew.getValue());
                protectedContent.setContentBase64(protectedContentUtil.protectString(attributeNew.getValue()));
            }
            auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_ATTRIBUTE_CHANGED, attributeNew.getName(), attributeOld.getValue(), attributeNew.getValue(), bpmnProcessInfo));
        }
    }

    private BPMNProcessInfo createBPMNProcessInfo(BPMNProcessInfoView bpmnProcessInfoView,
                                                  List<AuditTrace> auditList){
        LOG.debug( "in createBPMNProcessInfo");
        BPMNProcessInfo bpmnProcessInfo = new BPMNProcessInfo();

        bpmnProcessInfo.setId(bpmnProcessInfoView.getId());
        bpmnProcessInfo.setName(bpmnProcessInfoView.getName());
        bpmnProcessInfo.setType(bpmnProcessInfoView.getType());
        bpmnProcessInfo.setAuthor(bpmnProcessInfoView.getAuthor());
        bpmnProcessInfo.setVersion(bpmnProcessInfoView.getVersion());
        bpmnProcessInfo.setProcessId(bpmnProcessInfoView.getProcessId());
        bpmnProcessInfo.setLastChange(bpmnProcessInfoView.getLastChange());
        bpmnProcessInfo.setBpmnHashBase64(bpmnProcessInfoView.getBpmnHashBase64());
        bpmnProcessInfo.setSignatureBase64(bpmnProcessInfoView.getSignatureBase64());

        if( bpmnProcessInfoView.getBpmnProcessAttributes() == null){
            bpmnProcessInfoView.setBpmnProcessAttributes(new BPMNProcessAttribute[0]);
        }
        HashSet<BPMNProcessAttribute> bpmnProcessAttributeSet = new HashSet<>();
        for( BPMNProcessAttribute attribute: bpmnProcessInfoView.getBpmnProcessAttributes()){
            bpmnProcessAttributeSet.add(attribute);
            bpnmAttributeRepo.save(attribute);
        }
        bpmnProcessInfo.setBpmnProcessAttributes(bpmnProcessAttributeSet);
        bpnmInfoRepo.save(bpmnProcessInfo);

        auditList.add(auditService.createAuditTraceBPMNProcessInfo(AuditService.AUDIT_BPMN_CREATED, null, null, bpmnProcessInfo));

        return bpmnProcessInfo;
    }

}
