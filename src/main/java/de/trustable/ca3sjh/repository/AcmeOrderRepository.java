package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.AcmeOrder;
import de.trustable.ca3sjh.domain.Authorization;

import java.util.List;

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


}
