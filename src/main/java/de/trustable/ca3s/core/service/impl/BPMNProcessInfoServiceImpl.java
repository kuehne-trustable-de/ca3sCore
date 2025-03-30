package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.dto.BPMNProcessInfoView;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.BPMNProcessInfoService;
import de.trustable.ca3s.core.domain.BPMNProcessInfo;
import de.trustable.ca3s.core.repository.BPMNProcessInfoRepository;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link BPMNProcessInfo}.
 */
@Service
@Transactional
public class BPMNProcessInfoServiceImpl implements BPMNProcessInfoService {

    private final Logger log = LoggerFactory.getLogger(BPMNProcessInfoServiceImpl.class);

    private final BPMNProcessInfoRepository bPMNProcessInfoRepository;
    private final BPMNUtil bpmnUtil;

    private final PlatformTransactionManager transactionManager;

    public BPMNProcessInfoServiceImpl(BPMNProcessInfoRepository bPMNProcessInfoRepository, BPMNUtil bpmnUtil, PlatformTransactionManager transactionManager) {
        this.bPMNProcessInfoRepository = bPMNProcessInfoRepository;
        this.bpmnUtil = bpmnUtil;
        this.transactionManager = transactionManager;
    }

    /**
     * Save a bPMNProcessInfo.
     *
     * @param bPMNProcessInfo the entity to save.
     * @return the persisted entity.
     */
    @Override
    public BPMNProcessInfo save(BPMNProcessInfo bPMNProcessInfo) {
        log.debug("Request to save BPMNProcessInfo : {}", bPMNProcessInfo);
        return bPMNProcessInfoRepository.save(bPMNProcessInfo);
    }

    /**
     * Save a BPMNProcessInfoView.
     *
     * @param bpmnProcessInfoView the entity to save.
     * @return the persisted entity.
     */
    @Override
    public BPMNProcessInfo save(BPMNProcessInfoView bpmnProcessInfoView) {
        log.debug("Request to save BPMNProcessInfoView : {}", bpmnProcessInfoView);
        return bpmnUtil.toBPMNProcessInfo(bpmnProcessInfoView);
    }


    /**
     * Get all the bPMNProcessInfos.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BPMNProcessInfo> findAll() {
        log.debug("Request to get all BPMNProcessInfos");
        return bPMNProcessInfoRepository.findAll();
    }


    /**
     * Get one bPMNProcessInfo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BPMNProcessInfo> findOne(Long id) {
        log.debug("Request to get BPMNProcessInfo : {}", id);
        return bPMNProcessInfoRepository.findById(id);
    }

    /**
     * Delete the bPMNProcessInfo by id.
     *
     * @param id the id of the entity.
     */
    @Override
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete BPMNProcessInfo : {}", id);
        Optional<BPMNProcessInfo> optionalBPMNProcessInfo = bPMNProcessInfoRepository.findById(id);
        if(optionalBPMNProcessInfo.isPresent()) {
            String processId = optionalBPMNProcessInfo.get().getProcessId();
            if( processId != null ) {
                // handle separate transaction
                TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
                txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

                try {
                    txTemplate.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(TransactionStatus status) {

                            try {
                                bpmnUtil.deleteProcessDefinitions(processId);
                            } catch (NotFoundException nfe) {
                                log.warn("No valid process found for process if '{}' while deleting BPMNProcessInfo : {}", processId, id);
                            }
                        }
                    });
                }catch (TransactionException transactionException){
                    log.warn("Ignoring outcome of the deletion of camunda process definition: {}", transactionException.getMessage());
                }
            }else{
                log.warn("No valid process id found while deleting BPMNProcessInfo : {}", id);
            }
            bPMNProcessInfoRepository.deleteById(id);
        }
    }

}
