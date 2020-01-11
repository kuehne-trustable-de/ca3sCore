package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.RDN;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the RDN entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RDNRepository extends JpaRepository<RDN, Long> {

}
