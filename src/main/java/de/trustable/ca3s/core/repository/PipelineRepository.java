package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.Pipeline;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Pipeline entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

}
