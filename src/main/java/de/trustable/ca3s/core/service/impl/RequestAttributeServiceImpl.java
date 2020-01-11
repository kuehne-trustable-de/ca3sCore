package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.RequestAttributeService;
import de.trustable.ca3s.core.domain.RequestAttribute;
import de.trustable.ca3s.core.repository.RequestAttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link RequestAttribute}.
 */
@Service
@Transactional
public class RequestAttributeServiceImpl implements RequestAttributeService {

    private final Logger log = LoggerFactory.getLogger(RequestAttributeServiceImpl.class);

    private final RequestAttributeRepository requestAttributeRepository;

    public RequestAttributeServiceImpl(RequestAttributeRepository requestAttributeRepository) {
        this.requestAttributeRepository = requestAttributeRepository;
    }

    /**
     * Save a requestAttribute.
     *
     * @param requestAttribute the entity to save.
     * @return the persisted entity.
     */
    @Override
    public RequestAttribute save(RequestAttribute requestAttribute) {
        log.debug("Request to save RequestAttribute : {}", requestAttribute);
        return requestAttributeRepository.save(requestAttribute);
    }

    /**
     * Get all the requestAttributes.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<RequestAttribute> findAll() {
        log.debug("Request to get all RequestAttributes");
        return requestAttributeRepository.findAll();
    }


    /**
     * Get one requestAttribute by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<RequestAttribute> findOne(Long id) {
        log.debug("Request to get RequestAttribute : {}", id);
        return requestAttributeRepository.findById(id);
    }

    /**
     * Delete the requestAttribute by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete RequestAttribute : {}", id);
        requestAttributeRepository.deleteById(id);
    }
}
