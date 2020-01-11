package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.RequestAttributeValueService;
import de.trustable.ca3s.core.domain.RequestAttributeValue;
import de.trustable.ca3s.core.repository.RequestAttributeValueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link RequestAttributeValue}.
 */
@Service
@Transactional
public class RequestAttributeValueServiceImpl implements RequestAttributeValueService {

    private final Logger log = LoggerFactory.getLogger(RequestAttributeValueServiceImpl.class);

    private final RequestAttributeValueRepository requestAttributeValueRepository;

    public RequestAttributeValueServiceImpl(RequestAttributeValueRepository requestAttributeValueRepository) {
        this.requestAttributeValueRepository = requestAttributeValueRepository;
    }

    /**
     * Save a requestAttributeValue.
     *
     * @param requestAttributeValue the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RequestAttributeValue save(RequestAttributeValue requestAttributeValue) {
        log.debug("Request to save RequestAttributeValue : {}", requestAttributeValue);
        return requestAttributeValueRepository.save(requestAttributeValue);
    }

    /**
     * Get all the requestAttributeValues.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RequestAttributeValue> findAll() {
        log.debug("Request to get all RequestAttributeValues");
        return requestAttributeValueRepository.findAll();
    }


    /**
     * Get one requestAttributeValue by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RequestAttributeValue> findOne(Long id) {
        log.debug("Request to get RequestAttributeValue : {}", id);
        return requestAttributeValueRepository.findById(id);
    }

    /**
     * Delete the requestAttributeValue by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete RequestAttributeValue : {}", id);
        requestAttributeValueRepository.deleteById(id);
    }
}
