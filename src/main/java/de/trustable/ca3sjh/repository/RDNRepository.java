package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.RDN;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the RDN entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RDNRepository extends JpaRepository<RDN, Long> {

}
