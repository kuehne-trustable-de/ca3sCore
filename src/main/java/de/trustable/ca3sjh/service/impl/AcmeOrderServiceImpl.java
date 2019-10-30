package de.trustable.ca3sjh.service.impl;

import de.trustable.ca3sjh.service.AcmeOrderService;
import de.trustable.ca3sjh.domain.AcmeOrder;
import de.trustable.ca3sjh.repository.AcmeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeOrder}.
 */
@Service
@Transactional
public class AcmeOrderServiceImpl implements AcmeOrderService {

    private final Logger log = LoggerFactory.getLogger(AcmeOrderServiceImpl.class);

    private final AcmeOrderRepository acmeOrderRepository;

    public AcmeOrderServiceImpl(AcmeOrderRepository acmeOrderRepository) {
        this.acmeOrderRepository = acmeOrderRepository;
    }

    /**
     * Save a acmeOrder.
     *
     * @param acmeOrder the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeOrder save(AcmeOrder acmeOrder) {
        log.debug("Request to save AcmeOrder : {}", acmeOrder);
        return acmeOrderRepository.save(acmeOrder);
    }

    /**
     * Get all the acmeOrders.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeOrder> findAll() {
        log.debug("Request to get all AcmeOrders");
        return acmeOrderRepository.findAll();
    }


    /**
     * Get one acmeOrder by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AcmeOrder> findOne(Long id) {
        log.debug("Request to get AcmeOrder : {}", id);
        return acmeOrderRepository.findById(id);
    }

    /**
     * Delete the acmeOrder by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeOrder : {}", id);
        acmeOrderRepository.deleteById(id);
    }
}
