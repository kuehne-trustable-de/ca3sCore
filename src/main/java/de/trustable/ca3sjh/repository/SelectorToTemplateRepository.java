package de.trustable.ca3sjh.repository;
import de.trustable.ca3sjh.domain.SelectorToTemplate;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the SelectorToTemplate entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SelectorToTemplateRepository extends JpaRepository<SelectorToTemplate, Long> {

}
