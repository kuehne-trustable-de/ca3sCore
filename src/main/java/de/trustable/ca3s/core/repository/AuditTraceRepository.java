package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the AuditTrace entity.
 */
@Repository
public interface AuditTraceRepository extends PagingAndSortingRepository<AuditTrace, Long> {

    @Query(name = "AuditTrace.findByCsrAndCert")
    List<AuditTrace> findByCsrAndCert(@Param("certificate") Certificate certificate,
                                      @Param("csr") CSR csr);

    @Query(name = "AuditTrace.findByCsrAndCert")
    Page<AuditTrace> findByCsrAndCert(Pageable pageable,
                                      @Param("certificate") Certificate certificate,
                                      @Param("csr") CSR csr);

    @Query(name = "AuditTrace.findByCsr")
    Page<AuditTrace> findByCsr(Pageable pageable,
                               @Param("csr") CSR csr);

    @Query(name = "AuditTrace.findByPipeline")
    Page<AuditTrace> findByPipeline(Pageable pageable,
                                    @Param("pipeline") Pipeline pipeline);

    @Query(name = "AuditTrace.findByPipeline")
    List<AuditTrace> findByPipeline(@Param("pipeline") Pipeline pipeline);

    @Query(name = "AuditTrace.findByCaConnector")
    Page<AuditTrace> findByCaConnector(Pageable pageable,
                                       @Param("caConnector") CAConnectorConfig caConnector);

    @Query(name = "AuditTrace.findByProcessInfo")
    Page<AuditTrace> findByProcessInfo(Pageable pageable,
                                       @Param("processInfo") BPMNProcessInfo processInfo);

}
