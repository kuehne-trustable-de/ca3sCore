package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.Nonce;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Nonce entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NonceRepository extends JpaRepository<Nonce, Long> {

}
