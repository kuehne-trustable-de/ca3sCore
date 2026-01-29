package de.trustable.ca3s.core.service;

import de.trustable.ca3s.core.domain.AuditTrace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link AuditTrace}.
 */
public interface AuditTraceService {

    /**
     * Save a auditTrace.
     *
     * @param auditTrace the entity to save.
     * @return the persisted entity.
     */
    AuditTrace save(AuditTrace auditTrace);

    /**
     * Get all the auditTraces.
     *
     * @return the list of entities.
     */
    Page<AuditTrace> findAll(Pageable pageable);


    /**
     * Get the "id" auditTrace.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<AuditTrace> findOne(Long id);

    /**
     * Delete the "id" auditTrace.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);

    Page<AuditTrace> findBy(Pageable pageable,
                            Long certificateId,
                            Long csrId,
                            Long pipelineId,
                            Long caConnectorId,
                            Long processInfoId,
                            Long acmeOrderId,
                            Long scepOrderId);
}
