package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.RDNAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the RDNAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RDNAttributeRepository extends JpaRepository<RDNAttribute, Long> {

}
