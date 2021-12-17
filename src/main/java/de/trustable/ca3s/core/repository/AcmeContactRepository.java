package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the AcmeContact entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AcmeContactRepository extends JpaRepository<AcmeContact, Long> {

    @Query(name = "AcmeContact.findByAccountId")
    List<AcmeContact> findByAccountId(@Param("accountId") Long accountId);

}
