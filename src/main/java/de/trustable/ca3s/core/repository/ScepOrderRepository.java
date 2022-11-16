package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ScepOrder;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the ScepOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ScepOrderRepository extends JpaRepository<ScepOrder, Long> {

}
