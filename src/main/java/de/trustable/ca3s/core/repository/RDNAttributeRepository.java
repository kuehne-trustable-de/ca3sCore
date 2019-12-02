package de.trustable.ca3s.core.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.RDNAttribute;


/**
 * Spring Data  repository for the RDNAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RDNAttributeRepository extends JpaRepository<RDNAttribute, Long> {

}
