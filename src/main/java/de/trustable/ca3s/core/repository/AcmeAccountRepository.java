package de.trustable.ca3s.core.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.ACMEAccount;


/**
 * Spring Data  repository for the ACMEAccount entity.
 */
@Repository
public interface ACMEAccountRepository extends JpaRepository<ACMEAccount, Long> {

	@Query(name = "Account.findByAccountId")
	List<ACMEAccount> findByAccountId(@Param("accountId") long accountId);

	@Query(name = "Account.findByPublicKeyHash")
	List<ACMEAccount> findByPublicKeyHashBase64(@Param("publicKeyHashBase64") String publicKeyHashBase64);

    @Query(name = "Account.findByCreatedOnIsNull")
    List<ACMEAccount> findByCreatedOnIsNull();

}
