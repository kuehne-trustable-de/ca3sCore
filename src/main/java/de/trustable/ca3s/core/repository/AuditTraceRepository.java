package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AuditTrace;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Pipeline;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the AuditTrace entity.
 */
@Repository
public interface AuditTraceRepository extends JpaRepository<AuditTrace, Long> {

    @Query(name = "AuditTrace.findByCsrAndCert")
    List<AuditTrace> findByCsrAndCert(@Param("certificate") Certificate certificate,
                                        @Param("csr") CSR csr);

    @Query(name = "AuditTrace.findByCsr")
    List<AuditTrace> findByCsr(@Param("csr") CSR csr);

    @Query(name = "AuditTrace.findByPipeline")
    List<AuditTrace> findByPipeline(@Param("pipeline") Pipeline pipeline);

}
