package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.AcmeContact;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the AcmeContact entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeContactRepository extends JpaRepository<AcmeContact, Long> {

}
