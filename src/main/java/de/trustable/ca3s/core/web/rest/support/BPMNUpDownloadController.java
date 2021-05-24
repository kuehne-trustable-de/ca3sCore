package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.BPMNUpload;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
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

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/api")
public class BPMNUpDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNUpDownloadController.class);

    final BPMNUtil bpmnUtil;
    final AuditService auditService;


    public BPMNUpDownloadController(BPMNUtil bpmnUtil, AuditService auditService) {
        this.bpmnUtil = bpmnUtil;
        this.auditService = auditService;
    }

    /**
     * Public certificate download endpoint providing DER format
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

        return new ResponseEntity(inputStreamResource, HttpStatus.OK);

    }

    @PostMapping("/bpmn")
    @Transactional
    public ResponseEntity<InputStreamResource> postBPMN(@Valid @RequestBody final BPMNUpload bpmnUpload) throws NotFoundException {

        LOG.info("Received bpmn upload request with name {} ", bpmnUpload.getName());

        if( bpmnUpload.getContentXML().trim().isEmpty() ){
            LOG.warn("Received bpmn upload request with name {} has no XML content!", bpmnUpload.getName());
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        String modelId = bpmnUtil.addModel(bpmnUpload.getContentXML(), bpmnUpload.getName());
        LOG.debug("Deployed bpmn document with modelName {} successfully", modelId);

        BPMNProcessInfo bpmnProcessInfo = bpmnUtil.buildBPMNProcessInfo(modelId, bpmnUpload.getName(), bpmnUpload.getType());

        for( ProcessDefinition pd : bpmnUtil.getProcessDefinitions()){
            LOG.info("process definition present : id {}, name {} ", pd.getId(), pd.getName());
        }

        return new ResponseEntity(HttpStatus.NOT_FOUND);

    }

}
