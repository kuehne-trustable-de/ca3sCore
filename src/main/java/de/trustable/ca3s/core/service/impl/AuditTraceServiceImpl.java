package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AuditTraceService;
import de.trustable.ca3s.core.domain.AuditTrace;
import de.trustable.ca3s.core.repository.AuditTraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AuditTrace}.
 */
@Service
@Transactional
public class AuditTraceServiceImpl implements AuditTraceService {

    private final Logger log = LoggerFactory.getLogger(AuditTraceServiceImpl.class);

    private final AuditTraceRepository auditTraceRepository;

    public AuditTraceServiceImpl(AuditTraceRepository auditTraceRepository) {
        this.auditTraceRepository = auditTraceRepository;
    }

    @Override
    public AuditTrace save(AuditTrace auditTrace) {
        log.debug("Request to save AuditTrace : {}", auditTrace);
        return auditTraceRepository.save(auditTrace);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditTrace> findAll() {
        log.debug("Request to get all AuditTraces");
        return auditTraceRepository.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<AuditTrace> findOne(Long id) {
        log.debug("Request to get AuditTrace : {}", id);
        return auditTraceRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AuditTrace : {}", id);
        auditTraceRepository.deleteById(id);
    }
}
