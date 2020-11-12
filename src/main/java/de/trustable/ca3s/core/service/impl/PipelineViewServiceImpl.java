package de.trustable.ca3s.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.repository.PipelineRepository;
import de.trustable.ca3s.core.service.PipelineViewService;
import de.trustable.ca3s.core.service.dto.PipelineView;
import de.trustable.ca3s.core.service.util.PipelineUtil;

/**
 * Service Implementation for managing {@link PipelineView}.
 */
@Service
@Transactional
public class PipelineViewServiceImpl implements PipelineViewService {

    private final Logger log = LoggerFactory.getLogger(PipelineViewServiceImpl.class);

    private final PipelineRepository pipelineRepository;
    private final PipelineUtil pvUtil;

    public PipelineViewServiceImpl(PipelineRepository pipelineRepository, PipelineUtil pvUtil) {
        this.pipelineRepository = pipelineRepository;
        this.pvUtil = pvUtil;
    }

    /**
     * Save a pipeline.
     *
     * @param pipelineView the entity to save.
     * @return the persisted entity.
     */
    @Override
    public PipelineView save(PipelineView pipelineView) {
        log.debug("Request to save Pipeline : {}", pipelineView);

        Pipeline p = pvUtil.toPipeline(pipelineView);
/*
        log.debug("Saving #{} pipeline attributes", p.getPipelineAttributes().size());
        for( PipelineAttribute pa: p.getPipelineAttributes()) {
            log.debug("Request to save PipelineAttribute: {}", pa);
        }
*/
        pipelineRepository.save(p);

        // set id for new pipelines, redundant for existing ones
        pipelineView.setId(p.getId());

        return pipelineView;
    }

    /**
     * Get all the pipelines.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<PipelineView> findAll() {
        log.debug("Request to get all Pipelines");

        ArrayList<PipelineView> pvList = new ArrayList<PipelineView>();
        for(Pipeline p: pipelineRepository.findAll()) {
        	PipelineView pv = pvUtil.from(p);
        	pvList.add(pv);
        }
        return pvList;
    }


    /**
     * Get one pipeline by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<PipelineView> findOne(Long id) {
        log.debug("Request to get Pipeline : {}", id);

       	Optional<Pipeline> optP = pipelineRepository.findById(id);
        if(optP.isPresent()) {
        	return Optional.of(pvUtil.from(optP.get()));
        }
        return Optional.empty();

    }

    /**
     * Delete the pipeline by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Pipeline : {}", id);
        pipelineRepository.deleteById(id);
    }
}
