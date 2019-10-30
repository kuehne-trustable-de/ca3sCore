package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.RequestAttribute;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the RequestAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RequestAttributeRepository extends JpaRepository<RequestAttribute, Long> {

}
