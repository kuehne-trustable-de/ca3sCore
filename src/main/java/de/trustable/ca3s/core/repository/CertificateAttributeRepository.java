package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data  repository for the CertificateAttribute entity.
 */
@Repository
public interface CertificateAttributeRepository extends JpaRepository<CertificateAttribute, Long> {

    @Query(name = "CertificateAttribute.findDistinctValues")
    List<String> findDistinctValues(@Param("attName") String attName);

}
