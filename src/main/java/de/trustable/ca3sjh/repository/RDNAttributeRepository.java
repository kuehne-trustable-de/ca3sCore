package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.RDNAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the RDNAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RDNAttributeRepository extends JpaRepository<RDNAttribute, Long> {

}
