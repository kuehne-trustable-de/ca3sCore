package de.trustable.ca3s.core.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.AcmeAccount;


/**
 * Spring Data  repository for the AcmeAccount entity.
 */
@Repository
public interface AcmeAccountRepository extends JpaRepository<AcmeAccount, Long> {

	@Query(name = "Account.findByAccountId")
    Optional<AcmeAccount> findByAccountId(@Param("accountId") long accountId);

	@Query(name = "Account.findByPublicKeyHash")
	List<AcmeAccount> findByPublicKeyHashBase64(@Param("publicKeyHashBase64") String publicKeyHashBase64);

  @Query(name = "Account.findByCreatedOnIsNull")
  Page<AcmeAccount> findByCreatedOnIsNull(Pageable pageable);

}
