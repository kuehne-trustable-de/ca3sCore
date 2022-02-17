package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CSR entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CSRRepository extends JpaRepository<CSR, Long> {

    @Query(name = "CSR.findByPublicKeyHash")
    List<CSR> findByPublicKeyHash(@Param("hash") String hashB64);

    @Query(name = "CSR.findNonRejectedByPublicKeyHash")
    List<CSR> findNonRejectedByPublicKeyHash(@Param("hash") String hashB64);

    @Query(name = "CSR.findPendingByDay")
    List<CSR> findPendingByDay(@Param("after") Instant after, @Param("before") Instant before);

    @Query(name = "CSR.findPendingGroupedByDay")
    List<CSR> findPendingGroupedByDay(@Param("after") Instant after, @Param("before") Instant before);
}
