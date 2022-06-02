package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.repository.AuditTraceRepository;
import de.trustable.ca3s.core.repository.PipelineAttributeRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.PipelineService;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.exception.IntegrityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Pipeline}.
 */
@Service
@Transactional
public class PipelineServiceImpl implements PipelineService {

    private final Logger log = LoggerFactory.getLogger(PipelineServiceImpl.class);

    private final PipelineRepository pipelineRepository;
    private final PipelineAttributeRepository pipelineAttributeRepository;

    private final AuditService auditService;
    private final AuditTraceRepository auditTraceRepository;

    public PipelineServiceImpl(PipelineRepository pipelineRepository, PipelineAttributeRepository pipelineAttributeRepository, AuditService auditService, AuditTraceRepository auditTraceRepository) {
        this.pipelineRepository = pipelineRepository;
        this.pipelineAttributeRepository = pipelineAttributeRepository;
        this.auditService = auditService;
        this.auditTraceRepository = auditTraceRepository;
    }

    /**
     * Save a pipeline.
     *
     * @param pipeline the entity to save.
     * @return the persisted entity.
     */
    @Override
    public Pipeline save(Pipeline pipeline) {
        log.debug("Request to save Pipeline : {}", pipeline);
        return pipelineRepository.save(pipeline);
    }

    /**
     * Get all the pipelines.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Pipeline> findAll() {
        log.debug("Request to get all Pipelines");
        return pipelineRepository.findAll();
    }


    /**
     * Get one pipeline by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Pipeline> findOne(Long id) {
        log.debug("Request to get Pipeline : {}", id);
        return pipelineRepository.findById(id);
    }

    /**
     * Delete the pipeline by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Pipeline : {}", id);

        Optional<Pipeline> pipelineOpt = pipelineRepository.findById(id);
        if(pipelineOpt.isPresent()) {

            Pipeline pipeline = pipelineOpt.get();

            List<AuditTrace> auditTraceList = auditTraceRepository.findByPipeline(pipeline);
            boolean bWasUsed = false;
            for( AuditTrace auditTrace: auditTraceList){
                if( auditTrace.getCertificate() != null ){
                    log.debug("Pipeline : {} has certificate. Delete prohibited", id);
                    bWasUsed = true;
                    break;
                }
                if( auditTrace.getCsr() != null ){
                    log.debug("Pipeline : {} has CSR. Delete prohibited", id);
                    bWasUsed = true;
                    break;
                }
            }
            if(bWasUsed){
                throw new IntegrityException("Pipeline already used");
            }

            auditTraceRepository.deleteAll(auditTraceList);
            auditService.saveAuditTrace(auditService.createAuditTracePipeline(AuditService.AUDIT_PIPELINE_DELETED, null));
            pipelineAttributeRepository.deleteAll(pipeline.getPipelineAttributes());
            pipelineRepository.deleteById(id);
        }
    }
}
