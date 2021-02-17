package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;

import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BPMNProcessInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BPMNProcessInfoRepository extends JpaRepository<BPMNProcessInfo, Long> {


	@Query(name = "BPMNProcessInfo.findByName")
	public Optional<BPMNProcessInfo> findByName(@Param("name")  String name);

}
