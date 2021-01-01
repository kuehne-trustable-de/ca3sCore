package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.RequestProxyConfigService;
import de.trustable.ca3s.core.domain.RequestProxyConfig;
import de.trustable.ca3s.core.repository.RequestProxyConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link RequestProxyConfig}.
 */
@Service
@Transactional
public class RequestProxyConfigServiceImpl implements RequestProxyConfigService {

    private final Logger log = LoggerFactory.getLogger(RequestProxyConfigServiceImpl.class);

    private final RequestProxyConfigRepository requestProxyConfigRepository;

    public RequestProxyConfigServiceImpl(RequestProxyConfigRepository requestProxyConfigRepository) {
        this.requestProxyConfigRepository = requestProxyConfigRepository;
    }

    /**
     * Save a requestProxyConfig.
     *
     * @param requestProxyConfig the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RequestProxyConfig save(RequestProxyConfig requestProxyConfig) {
        log.debug("Request to save RequestProxyConfig : {}", requestProxyConfig);
        return requestProxyConfigRepository.save(requestProxyConfig);
    }

    /**
     * Get all the requestProxyConfigs.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RequestProxyConfig> findAll() {
        log.debug("Request to get all RequestProxyConfigs");
        return requestProxyConfigRepository.findAll();
    }

    /**
     * Get one requestProxyConfig by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RequestProxyConfig> findOne(Long id) {
        log.debug("Request to get RequestProxyConfig : {}", id);
        return requestProxyConfigRepository.findById(id);
    }

    /**
     * Delete the requestProxyConfig by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete RequestProxyConfig : {}", id);
        requestProxyConfigRepository.deleteById(id);
    }
}
