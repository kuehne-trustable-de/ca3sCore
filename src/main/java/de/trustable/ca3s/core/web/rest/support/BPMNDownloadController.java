package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.service.util.BPMNUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/api")
public class BPMNDownloadController {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNDownloadController.class);

    final BPMNUtil bpmnUtil;

    public BPMNDownloadController(BPMNUtil bpmnUtil) {
        this.bpmnUtil = bpmnUtil;
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

}
