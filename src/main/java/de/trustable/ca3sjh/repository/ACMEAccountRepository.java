package de.trustable.ca3sjh.repository;

import de.trustable.ca3sjh.domain.ACMEAccount;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the ACMEAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ACMEAccountRepository extends JpaRepository<ACMEAccount, Long> {

	@Query(name = "Account.findByAccountId")
	List<ACMEAccount> findByAccountId(@Param("accountId") long accountId);

	@Query(name = "Account.findByPublicKeyHash")
	List<ACMEAccount> findByPublicKeyHashBase64(@Param("publicKeyHashBase64") String publicKeyHashBase64);

}
