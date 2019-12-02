package de.trustable.ca3s.core.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.CertificateAttribute;


/**
 * Spring Data  repository for the CertificateAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CertificateAttributeRepository extends JpaRepository<CertificateAttribute, Long> {

}
