package de.trustable.ca3s.core.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.CsrAttribute;


/**
 * Spring Data  repository for the CsrAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CsrAttributeRepository extends JpaRepository<CsrAttribute, Long> {

}
