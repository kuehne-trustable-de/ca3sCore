package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.BPNMProcessInfo;
import de.trustable.ca3s.core.domain.Certificate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BPNMProcessInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BPNMProcessInfoRepository extends JpaRepository<BPNMProcessInfo, Long> {
 

	@Query(name = "BPNMProcessInfo.findByName")
	public Optional<BPNMProcessInfo> findByName(@Param("name")  String name);
	
}
