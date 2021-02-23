package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the Pipeline entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PipelineRepository extends JpaRepository<Pipeline, Long> {

	  @Query(name = "Pipeline.findByTypeUrl")
	  List<Pipeline> findByTypeUrl(@Param("type") PipelineType type,
		      @Param("urlPart") String urlPart);

    @Query(name = "Pipeline.findByType")
    List<Pipeline> findByType(@Param("type") PipelineType type);

    @Query(name = "Pipeline.findActiveByType")
    List<Pipeline> findActiveByType(@Param("type") PipelineType type);

}
