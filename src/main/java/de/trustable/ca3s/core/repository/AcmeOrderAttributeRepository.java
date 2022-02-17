package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrderAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the AcmeOrderAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeOrderAttributeRepository extends JpaRepository<AcmeOrderAttribute, Long> {}
