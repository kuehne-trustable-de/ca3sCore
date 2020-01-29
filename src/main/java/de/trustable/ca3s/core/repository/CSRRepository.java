package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CSR entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CSRRepository extends JpaRepository<CSR, Long> {

	  @Query(name = "CSR.findByPublicKeyHash")
	  List<CSR> findByPublicKeyHash(@Param("hash") String hashB64);

}
