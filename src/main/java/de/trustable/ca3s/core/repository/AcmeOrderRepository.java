package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.domain.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeOrderRepository extends JpaRepository<AcmeOrder, Long> {


    @Query(name = "AcmeOrder.findByOrderId")
    List<AcmeOrder> findByOrderId(@Param("orderId") long orderId);

    @Query(name = "AcmeOrder.findByCertificate")
    Optional<AcmeOrder> findByCertificate(@Param("certificate") Certificate certificate);

    @Query(name = "AcmeOrder.findPipelineIsNull")
    Page<AcmeOrder> findPipelineIsNull(Pageable pageable);

    @Query(name = "AcmeOrder.countByAccountId")
    long countByAccountId(@Param("accountId") long accountId);

    @Query(name = "AcmeOrder.findByPendingExpiryBefore")
    Page<AcmeOrder> findByPendingExpiryBefore(Pageable pageable, @Param("expiresBefore") Instant expiresBefore);

}
