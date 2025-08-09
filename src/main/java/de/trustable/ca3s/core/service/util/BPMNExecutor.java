package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.BPMNProcessAttribute;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BPMNExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(BPMNExecutor.class);

    private final RuntimeService runtimeService;
    private final BPMNProcessInfoRepository bpnmInfoRepo;
    private final ProtectedContentUtil protectedContentUtil;

    public BPMNExecutor(RuntimeService runtimeService, BPMNProcessInfoRepository bpnmInfoRepo, ProtectedContentUtil protectedContentUtil) {
        this.runtimeService = runtimeService;
        this.bpnmInfoRepo = bpnmInfoRepo;
        this.protectedContentUtil = protectedContentUtil;

    }


    public ProcessInstanceWithVariables executeBPMNProcessByName(final String processNameId, Map<String, Object> variables) {

        Optional<BPMNProcessInfo> bpmnProcessInfoOpt = bpnmInfoRepo.findByProcessId(processNameId);
        if( bpmnProcessInfoOpt.isPresent()){
            return executeBPMNProcessByBPMNProcessInfo(bpmnProcessInfoOpt.get(), variables);
        }else{
            throw new RuntimeException("processNameId '" + processNameId + "' unknown");
        }
    }

    public ProcessInstanceWithVariables executeBPMNProcessByBPMNProcessInfo(final BPMNProcessInfo bpmnProcessInfo, Map<String, Object> variables){

        LOG.debug("execute BPMN Process Info ''{}' ", bpmnProcessInfo.getName());

         Set<BPMNProcessAttribute> variableSet = bpmnProcessInfo.getBpmnProcessAttributes();
         HashSet<String> protectedNameSet = new HashSet<>();

        for( BPMNProcessAttribute bpmnProcessAttribute : variableSet){

            String value = bpmnProcessAttribute.getValue();

            if( Boolean.TRUE.equals(bpmnProcessAttribute.getProtectedContent())) {

                protectedNameSet.add("processAttribute_" + bpmnProcessAttribute.getName());
                List<ProtectedContent> protectedContents = protectedContentUtil.retrieveProtectedContent(
                    ProtectedContentType.SECRET,
                    ContentRelationType.BPMN_ATTRIBUTE,
                    bpmnProcessAttribute.getId());

                if (protectedContents.isEmpty()) {
                    LOG.warn("executeBPMNProcessByBPMNProcessInfo: no protected value found for BPMNProcessAttribute #{}", bpmnProcessAttribute.getId());

                } else if (protectedContents.size() > 1) {
                    LOG.warn("executeBPMNProcessByBPMNProcessInfo: more than one ({}) protected values found for BPMNProcessAttribute #{}!", protectedContents.size(), bpmnProcessAttribute.getId());
                }

                ProtectedContent protectedContent = protectedContents.get(0);
                value = protectedContentUtil.unprotectString(protectedContent.getContentBase64());
            }

            variables.put("processAttribute_" + bpmnProcessAttribute.getName(), value);
        }

        variables.put("status", "Failed");
        variables.put("failureReason", "");

        ProcessInstanceWithVariables processInstance = null;
        try {
            processInstance = runtimeService.createProcessInstanceById(bpmnProcessInfo.getProcessId()).setVariables(variables).executeWithVariablesInReturn();
            String processInstanceId = processInstance.getId();
            LOG.info("ProcessInstance: {}", processInstanceId);

            Map<String, Object> resultvariables = processInstance.getVariables();
            for(String key: resultvariables.keySet()){
                LOG.debug("bpmn process resultvariables: {}", key);
                if(protectedNameSet.contains(key)){
                    resultvariables.replace(key, "**********");
                }
            }
        }catch(Exception processException){
            if(LOG.isDebugEnabled()){
                LOG.debug("Exception while calling bpmn process '"+bpmnProcessInfo.getProcessId()+"'", processException);
            }
        }
        return processInstance;
    }

}
