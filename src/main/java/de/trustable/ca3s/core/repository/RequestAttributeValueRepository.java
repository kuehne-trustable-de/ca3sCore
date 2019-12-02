package de.trustable.ca3s.core.repository;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import de.trustable.ca3s.core.domain.RequestAttributeValue;


/**
 * Spring Data  repository for the RequestAttributeValue entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RequestAttributeValueRepository extends JpaRepository<RequestAttributeValue, Long> {

}
