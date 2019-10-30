package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.CSR;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CSR entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CSRRepository extends JpaRepository<CSR, Long> {

}
