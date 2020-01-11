package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.AcmeContact;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeContact entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeContactRepository extends JpaRepository<AcmeContact, Long> {

}
