package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.CsrAttribute;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.BPMNUpload;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.web.rest.data.BpmnCheckResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/api")
public class BPMNUpDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNUpDownloadController.class);

    final BPMNUtil bpmnUtil;
    final CSRRepository csrRepository;
    final CAConnectorConfigRepository caConnectorConfigRepository;
    final AuditService auditService;


    public BPMNUpDownloadController(BPMNUtil bpmnUtil, CSRRepository csrRepository, CAConnectorConfigRepository caConnectorConfigRepository, AuditService auditService) {
        this.bpmnUtil = bpmnUtil;
        this.csrRepository = csrRepository;
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
    public ResponseEntity<InputStreamResource> getBPMN(@PathVariable final String processId) throws NotFoundException {

        LOG.info("Received bpmn download request for id {} ", processId);

        for( ProcessDefinition pd : bpmnUtil.getProcessDefinitions()){
            LOG.info("process definition present : id {}, name {} ", pd.getId(), pd.getName());
        }
        InputStreamResource inputStreamResource = new InputStreamResource(bpmnUtil.getProcessContent(processId));

        return new ResponseEntity<>(inputStreamResource, HttpStatus.OK);

    }

    @PostMapping("/bpmn")
    @Transactional
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

    /**
     * check results a given process id when processing a given CSR
     *
     * @param processId the internal process id
     * @return the process's response
     */
    @RequestMapping(value = "/bpmn/check/csr/{processId}/{csrId}",
        method = POST)
    public ResponseEntity<Map<String, String>> postBPMNForCSR(@PathVariable final String processId, @PathVariable final String csrId){

        LOG.info("Received bpmn check request for process id {} and csr id {}", processId, csrId);

        Optional<CSR> csrOpt = csrRepository.findById(Long.parseLong(csrId));

        CAConnectorConfig caConfig = caConnectorConfigRepository.getOne(1L);

        ProcessInstanceWithVariables processInstanceWithVariables = bpmnUtil.checkCertificateCreationProcess(csrOpt.get(), caConfig, processId);

        if( processInstanceWithVariables != null) {
            BpmnCheckResult result = new BpmnCheckResult();
            Map<String, Object> variables = processInstanceWithVariables.getVariables();
            for(String key: variables.keySet()){
                if( "csrAttributes".equals(key) ){
                    for( CsrAttribute csrAtt: (Set<CsrAttribute>)variables.get(key)){
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
                    result.getCsrAttributes().add(new ImmutablePair<>(key, value));
                }
            }
            return new ResponseEntity(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
