package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.CertificateAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the CertificateAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CertificateAttributeRepository extends JpaRepository<CertificateAttribute, Long> {

}
