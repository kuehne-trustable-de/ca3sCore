package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.CsrAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CsrAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CsrAttributeRepository extends JpaRepository<CsrAttribute, Long> {

}
