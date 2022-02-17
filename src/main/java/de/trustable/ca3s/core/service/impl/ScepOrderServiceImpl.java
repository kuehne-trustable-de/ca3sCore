package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.ScepOrder;
import de.trustable.ca3s.core.repository.ScepOrderRepository;
import de.trustable.ca3s.core.service.ScepOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link ScepOrder}.
 */
@Service
@Transactional
public class ScepOrderServiceImpl implements ScepOrderService {

    private final Logger log = LoggerFactory.getLogger(ScepOrderServiceImpl.class);

    private final ScepOrderRepository scepOrderRepository;

    public ScepOrderServiceImpl(ScepOrderRepository scepOrderRepository) {
        this.scepOrderRepository = scepOrderRepository;
    }

    /**
     * Save a scepOrder.
     *
     * @param scepOrder the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ScepOrder save(ScepOrder scepOrder) {
        log.debug("Request to save ScepOrder : {}", scepOrder);
        return scepOrderRepository.save(scepOrder);
    }

    /**
     * Get all the scepOrders.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ScepOrder> findAll() {
        log.debug("Request to get all ScepOrders");
        return scepOrderRepository.findAll();
    }


    /**
     * Get one scepOrder by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ScepOrder> findOne(Long id) {
        log.debug("Request to get ScepOrder : {}", id);
        return scepOrderRepository.findById(id);
    }

    /**
     * Delete the scepOrder by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ScepOrder : {}", id);
        scepOrderRepository.deleteById(id);
    }
}
