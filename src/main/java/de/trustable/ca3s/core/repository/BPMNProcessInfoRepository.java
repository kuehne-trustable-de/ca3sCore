package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.BPMNProcessInfo;

import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.domain.enumeration.BPMNProcessType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedQuery;


/**
 * Spring Data  repository for the BPMNProcessInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BPMNProcessInfoRepository extends JpaRepository<BPMNProcessInfo, Long> {

    @Query(name = "BPMNProcessInfo.findByName")
    public Optional<BPMNProcessInfo> findByName(@Param("name")  String name);

    @Query(name = "BPMNProcessInfo.findByNameOrderedBylastChange")
    public List<BPMNProcessInfo> findByNameOrderedBylastChange(@Param("name")  String name);

    @Query(name = "BPMNProcessInfo.findByProcessId")
    public Optional<BPMNProcessInfo> findByProcessId(@Param("processId") String processId);

    @Query(name = "BPMNProcessInfo.findByType")
    public List<BPMNProcessInfo> findByType(@Param("type") BPMNProcessType type);

}
