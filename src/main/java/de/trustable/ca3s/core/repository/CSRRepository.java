package de.trustable.ca3s.core.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.CSR;


/**
 * Spring Data  repository for the CSR entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CSRRepository extends JpaRepository<CSR, Long> {

}
