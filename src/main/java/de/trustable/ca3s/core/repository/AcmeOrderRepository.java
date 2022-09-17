package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedQuery;


/**
 * Spring Data  repository for the AcmeOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeOrderRepository extends JpaRepository<AcmeOrder, Long> {


    @Query(name = "AcmeOrder.findByOrderId")
    List<AcmeOrder> findByOrderId(@Param("orderId") long orderId);

    @Query(name = "AcmeOrder.findPipelineIsNull")
    List<AcmeOrder> findPipelineIsNull();

    @Query(name = "AcmeOrder.countByAccountId")
    long countByAccountId(@Param("accountId") long accountId);

    @Query(name = "AcmeOrder.findByPendingExpiryBefore")
    List<AcmeOrder> findByPendingExpiryBefore(@Param("expiresBefore") Instant expiresBefore);

}
