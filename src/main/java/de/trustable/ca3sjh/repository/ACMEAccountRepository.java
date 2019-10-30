package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.ACMEAccount;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the ACMEAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ACMEAccountRepository extends JpaRepository<ACMEAccount, Long> {

}
