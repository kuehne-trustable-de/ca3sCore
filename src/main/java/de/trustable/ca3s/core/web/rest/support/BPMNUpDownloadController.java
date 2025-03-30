package de.trustable.ca3s.core.web.rest.support;

import com.vdurmont.semver4j.Semver;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.BPMNUpload;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.web.rest.data.BpmnCheckResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.Instant;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/api")
public class BPMNUpDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNUpDownloadController.class);

    private static final String ENTITY_NAME = "bPMNProcessInfo";

    final BPMNUtil bpmnUtil;

    final BPMNProcessInfoRepository bpmnProcessInfoRepository;
    final CSRRepository csrRepository;
    final CertificateRepository certificateRepository;
    final CAConnectorConfigRepository caConnectorConfigRepository;
    final AuditService auditService;


    public BPMNUpDownloadController(BPMNUtil bpmnUtil, BPMNProcessInfoRepository bpmnProcessInfoRepository, CSRRepository csrRepository, CertificateRepository certificateRepository, CAConnectorConfigRepository caConnectorConfigRepository, AuditService auditService) {
        this.bpmnUtil = bpmnUtil;
        this.bpmnProcessInfoRepository = bpmnProcessInfoRepository;
        this.csrRepository = csrRepository;
        this.certificateRepository = certificateRepository;
        this.caConnectorConfigRepository = caConnectorConfigRepository;
        this.auditService = auditService;
    }

    /**
     * retrieve bpmn XML content for a given process id
     *
     * @param processId the internal process id
     * @return the process's XML
     */
    @RequestMapping(value = "/bpmn/{processId}",
        method = GET,
        produces = "application/xml")
    public ResponseEntity<InputStreamResource> getBPMN(@PathVariable final String processId) {

        LOG.info("Received bpmn download request for id {} ", processId);

        for( ProcessDefinition pd : bpmnUtil.getProcessDefinitions()){
            LOG.info("process definition present : id {}, name {} ", pd.getId(), pd.getName());
        }
        InputStreamResource inputStreamResource = new InputStreamResource(bpmnUtil.getProcessContent(processId));

        return new ResponseEntity<>(inputStreamResource, HttpStatus.OK);

    }

    @PostMapping("/bpmn")
    @Transactional
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<BPMNProcessInfo> postBPMN(@Valid @RequestBody final BPMNUpload bpmnUpload) {

        LOG.info("Received bpmn upload request with name {} ", bpmnUpload.getName());

        if( bpmnUpload.getContentXML().trim().isEmpty() ){
            LOG.warn("Received bpmn upload request with name {} has no XML content!", bpmnUpload.getName());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        String processDefinitionId = bpmnUtil.addModel(bpmnUpload.getContentXML(), bpmnUpload.getName());
        LOG.debug("Deployed bpmn document with processDefinitionId {} successfully", processDefinitionId);

        BPMNProcessInfo bpmnProcessInfo = bpmnUtil.buildBPMNProcessInfoByProcessId(processDefinitionId, bpmnUpload.getName(), bpmnUpload.getType());

        return new ResponseEntity<>(bpmnProcessInfo, HttpStatus.OK);

    }

    @PutMapping("/bpmn")
    @Transactional
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<BPMNProcessInfo> putBPMNProcessInfo(@Valid @RequestBody final BPMNUpload bpmnUpload)  {

        LOG.info("Received bpmn upload request with name {} ", bpmnUpload.getName());

        LOG.debug("REST request to update BPMNProcessInfo : {}", bpmnUpload);
        if (bpmnUpload.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        List<AuditTrace> auditList = new ArrayList<>();

        Optional<BPMNProcessInfo> optionalBpmnProcessInfo = bpmnProcessInfoRepository.findById(bpmnUpload.getId());
        if( optionalBpmnProcessInfo.isPresent()){

            BPMNProcessInfo bpmnProcessInfo = optionalBpmnProcessInfo.get();
            if(!Objects.equals(bpmnUpload.getName(), bpmnProcessInfo.getName())) {
                auditList.add(auditService.createAuditTraceBPMNProcessInfo( AuditService.AUDIT_BPMN_NAME_CHANGED, bpmnProcessInfo.getName(), bpmnUpload.getName(), bpmnProcessInfo));
                bpmnProcessInfo.setName(bpmnUpload.getName());
            }

            if(!Objects.equals(bpmnUpload.getType(), bpmnProcessInfo.getType())) {
                auditList.add(auditService.createAuditTraceBPMNProcessInfo( AuditService.AUDIT_BPMN_TYPE_CHANGED, bpmnProcessInfo.getType().name(), bpmnUpload.getName(), bpmnProcessInfo));
                bpmnProcessInfo.setType(bpmnUpload.getType());
            }

            LOG.debug("REST request to update BPMNProcessInfo contains #{} attributes", bpmnUpload.getBpmnProcessAttributes().length );

            if( bpmnUpload.getContentXML() == null || bpmnUpload.getContentXML().trim().isEmpty() ){
                LOG.debug("Received bpmn upload request with name {} without new XML content!", bpmnUpload.getName());
            }else {

                Semver currentVersion = new Semver(bpmnProcessInfo.getVersion());
                Semver newVersion;
                if( bpmnUpload.getVersion() == null || bpmnUpload.getVersion().isEmpty()) {
                    LOG.info("unexpected format of version field! Ignoring value.");
                    newVersion = currentVersion.nextPatch();
                }else {
                    newVersion = new Semver(bpmnUpload.getVersion());
                    if( newVersion.isGreaterThan(currentVersion)){
                        newVersion = currentVersion.nextPatch();
                    }
                }

                bpmnProcessInfo.setVersion(newVersion.toString());

                bpmnProcessInfo.setLastChange(Instant.now());

                String oldProcessDefinitionId = bpmnProcessInfo.getProcessId();
                String processDefinitionId = bpmnUtil.addModel(bpmnUpload.getContentXML(), bpmnUpload.getName());
                LOG.debug("Deployed bpmn document with processDefinitionId {} successfully", processDefinitionId);

                auditList.add(auditService.createAuditTraceBPMNProcessInfo( AuditService.AUDIT_BPMN_PROCESS_ID_CHANGED, oldProcessDefinitionId, processDefinitionId, bpmnProcessInfo));

                bpmnProcessInfo.setProcessId(processDefinitionId);
                bpmnUtil.deleteProcessDefinitions(oldProcessDefinitionId);
            }

            bpmnUtil.handleAttributes(bpmnUpload.getBpmnProcessAttributes(),
                auditList,
                bpmnProcessInfo);

            bpmnProcessInfoRepository.save(bpmnProcessInfo);
            auditService.saveAuditTrace(auditList);

            return new ResponseEntity<>(bpmnProcessInfo, HttpStatus.OK);
        }else{
            return  ResponseEntity.noContent().build();
        }

    }

    /**
     * check results a given process id when processing a given CSR
     *
     * @param processId the internal process id
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/csr/{processId}/{csrId}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNForCSR(@PathVariable final String processId, @PathVariable final String csrId){

        LOG.info("Received bpmn check request for process id {} and csr id {}", processId, csrId);

        Optional<CSR> csrOpt = csrRepository.findById(Long.parseLong(csrId));

        CAConnectorConfig caConfig = caConnectorConfigRepository.getOne(1L);

        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkCertificateCreationProcess(csrOpt.get(), caConfig, processId);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                if( "csrAttributes".equals(key) ) {
                    for (CsrAttribute csrAtt : (Set<CsrAttribute>) variables.get(key)) {
                        LOG.info("bpmn process returns CsrAttribute {} with value {}", csrAtt.getName(), csrAtt.getValue());
                        result.getCsrAttributes().add(new ImmutablePair<>(csrAtt.getName(), csrAtt.getValue()));
                    }

                }else if( "failureReason".equals(key) ){
                    result.setFailureReason(variables.get(key).toString());
                }else if( "status".equals(key) ){
                    result.setStatus(variables.get(key).toString());
                }else if( "isActive".equals(key) ){
                    result.setActive(Boolean.parseBoolean(variables.get(key).toString()));
                }else {
                    String value = variables.get(key).toString();
                    LOG.info("bpmn process returns variable {} with value {}", key, value);
                    result.getResponseAttributes().add(new ImmutablePair<>(key, value));
                }
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * check results a given process id when performing certificate notification
     *
     * @param processId the internal process id
     * @param processId the certificateId
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/certificateNotify/{processId}/{certificateId}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNForCertificateNotify(@PathVariable final String processId, @PathVariable final String certificateId){

        LOG.info("Received bpmn check certificate notification request for process id {} and certificate id {}", processId, certificateId);

        Optional<Certificate> certificateOptional = certificateRepository.findById(Long.parseLong(certificateId));

        CAConnectorConfig caConfig = caConnectorConfigRepository.getOne(1L);

        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkCertificateNotificationProcess(certificateOptional.get(), caConfig, processId);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                 if( "failureReason".equals(key) ){
                    result.setFailureReason(variables.get(key).toString());
                }else if( "status".equals(key) ){
                    result.setStatus(variables.get(key).toString());
                }else if( "isActive".equals(key) ){
                    result.setActive(Boolean.parseBoolean(variables.get(key).toString()));
                }else {
                    String value = variables.get(key).toString();
                    LOG.info("bpmn process returns variable {} with value {}", key, value);
                    result.getResponseAttributes().add(new ImmutablePair<>(key, value));
                }
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * check results a given batch process id when started
     *
     * @param processId the internal process id
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/accountRequest/{processId}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNAccountRequest(@PathVariable final String processId){

        LOG.info("Call bpmn request for process id {}", processId);
        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkAccountRequest(processId);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                String value = variables.get(key).toString();
                LOG.info("bpmn process returns variable {} with value {}", key, value);
                result.getResponseAttributes().add(new ImmutablePair<>(key, value));
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * check a SMS message sending process
     *
     * @param processId the internal process id
     * @param phone the target phone number
     * @param msg the message
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/acmeAccountAuthorization/{processId}/{mailto}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNAcmeAccountAuthorization(@PathVariable final String processId,
                                                                      @PathVariable final String mailto){

        LOG.info("Call bpmn request for process id {}", processId);
        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkAcmeAccountAuthorizationProzess(processId, mailto);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                String value = variables.get(key).toString();
                LOG.info("bpmn process returns variable {} with value {}", key, value);
                result.getResponseAttributes().add(new ImmutablePair<>(key, value));
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * check a SMS message sending process
     *
     * @param processId the internal process id
     * @param phone the target phone number
     * @param msg the message
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/sendSMS/{processId}/{phone}/{msg}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNSendSMS(@PathVariable final String processId,
                                                                      @PathVariable final String phone,
                                                                      @PathVariable final String msg){

        LOG.info("Call bpmn request for process id {}", processId);
        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkSMSProcess(processId, phone, msg);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                String value = variables.get(key).toString();
                LOG.info("bpmn process returns variable {} with value {}", key, value);
                result.getResponseAttributes().add(new ImmutablePair<>(key, value));
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * check results a given batch process id when started
     *
     * @param processId the internal process id
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/batch/{processId}",
        method = POST)
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    @Transactional
    public ResponseEntity<Map<String, String>> postBPMNBatch(@PathVariable final String processId){

        LOG.info("Call bpmn request for process id {}", processId);
        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkBatchProcess(processId);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                String value = variables.get(key).toString();
                LOG.info("bpmn process returns variable {} with value {}", key, value);
                result.getResponseAttributes().add(new ImmutablePair<>(key, value));
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
