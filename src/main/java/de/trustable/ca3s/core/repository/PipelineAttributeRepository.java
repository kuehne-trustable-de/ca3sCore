package de.trustable.ca3s.core.repository;
import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.PipelineAttribute;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the PipelineAttribute entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PipelineAttributeRepository extends JpaRepository<PipelineAttribute, Long> {

    @Query(name = "PipelineAttribute.findDistinctByName")
    List<String> findDistinctByName(@Param("name") String name);

}
