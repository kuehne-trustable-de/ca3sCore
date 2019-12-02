package de.trustable.ca3s.core.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.repository.CAConnectorConfigRepository;
import de.trustable.ca3s.core.service.CAConnectorConfigService;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link CAConnectorConfig}.
 */
@Service
@Transactional
public class CAConnectorConfigServiceImpl implements CAConnectorConfigService {

    private final Logger log = LoggerFactory.getLogger(CAConnectorConfigServiceImpl.class);

    private final CAConnectorConfigRepository cAConnectorConfigRepository;

    public CAConnectorConfigServiceImpl(CAConnectorConfigRepository cAConnectorConfigRepository) {
        this.cAConnectorConfigRepository = cAConnectorConfigRepository;
    }

    /**
     * Save a cAConnectorConfig.
     *
     * @param cAConnectorConfig the entity to save.
     * @return the persisted entity.
     */
    @Override
    public CAConnectorConfig save(CAConnectorConfig cAConnectorConfig) {
        log.debug("Request to save CAConnectorConfig : {}", cAConnectorConfig);
        return cAConnectorConfigRepository.save(cAConnectorConfig);
    }

    /**
     * Get all the cAConnectorConfigs.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CAConnectorConfig> findAll() {
        log.debug("Request to get all CAConnectorConfigs");
        return cAConnectorConfigRepository.findAll();
    }


    /**
     * Get one cAConnectorConfig by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<CAConnectorConfig> findOne(Long id) {
        log.debug("Request to get CAConnectorConfig : {}", id);
        return cAConnectorConfigRepository.findById(id);
    }

    /**
     * Delete the cAConnectorConfig by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete CAConnectorConfig : {}", id);
        cAConnectorConfigRepository.deleteById(id);
    }
}
