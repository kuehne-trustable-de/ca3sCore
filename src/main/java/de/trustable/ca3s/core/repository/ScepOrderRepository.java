package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ScepOrder;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Spring Data SQL repository for the ScepOrder entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ScepOrderRepository extends JpaRepository<ScepOrder, Long> {

}
