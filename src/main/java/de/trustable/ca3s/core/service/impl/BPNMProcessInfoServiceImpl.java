package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.BPNMProcessInfoService;
import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.repository.BPNMProcessInfoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link BPNMProcessInfo}.
 */
@Service
@Transactional
public class BPNMProcessInfoServiceImpl implements BPNMProcessInfoService {

    private final Logger log = LoggerFactory.getLogger(BPNMProcessInfoServiceImpl.class);

    private final BPNMProcessInfoRepository bPNMProcessInfoRepository;

    public BPNMProcessInfoServiceImpl(BPNMProcessInfoRepository bPNMProcessInfoRepository) {
        this.bPNMProcessInfoRepository = bPNMProcessInfoRepository;
    }

    /**
     * Save a bPNMProcessInfo.
     *
     * @param bPNMProcessInfo the entity to save.
     * @return the persisted entity.
     */
    @Override
    public BPNMProcessInfo save(BPNMProcessInfo bPNMProcessInfo) {
        log.debug("Request to save BPNMProcessInfo : {}", bPNMProcessInfo);
        return bPNMProcessInfoRepository.save(bPNMProcessInfo);
    }

    /**
     * Get all the bPNMProcessInfos.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<BPNMProcessInfo> findAll() {
        log.debug("Request to get all BPNMProcessInfos");
        return bPNMProcessInfoRepository.findAll();
    }


    /**
     * Get one bPNMProcessInfo by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<BPNMProcessInfo> findOne(Long id) {
        log.debug("Request to get BPNMProcessInfo : {}", id);
        return bPNMProcessInfoRepository.findById(id);
    }

    /**
     * Delete the bPNMProcessInfo by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete BPNMProcessInfo : {}", id);
        bPNMProcessInfoRepository.deleteById(id);
    }
}
