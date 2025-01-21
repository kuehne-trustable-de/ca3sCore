package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;

import java.time.Instant;
import java.util.List;

import de.trustable.ca3s.core.schedule.ProtectedContentCleanupScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.NamedQuery;


/**
 * Spring Data  repository for the ProtectedContent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ProtectedContentRepository extends JpaRepository<ProtectedContent, Long> {

    Logger LOG = LoggerFactory.getLogger(ProtectedContentRepository.class);

    @Query(name = "ProtectedContent.findByCertificateId")
    List<ProtectedContent> findByCertificateId(@Param("certId") Long certId);

    @Query(name = "ProtectedContent.findByCSRId")
    List<ProtectedContent> findByCSRId(@Param("csrId") Long csrId);

    @Query(name = "ProtectedContent.findByTypeRelationId")
    List<ProtectedContent> findByTypeRelationId(@Param("type") ProtectedContentType type, @Param("relationType") ContentRelationType relationType, @Param("id") Long id);

    @Query(name = "ProtectedContent.findByTypeRelationContentB64")
    List<ProtectedContent> findByTypeRelationContentB64(@Param("type") ProtectedContentType type,
                                                        @Param("relationType") ContentRelationType relationType,
                                                        @Param("contentB64") String contentB64);

    @Query(name = "ProtectedContent.findByValidToPassed")
    Page<ProtectedContent> findByValidToPassed(Pageable pageable, @Param("validTo") Instant validTo);

    @Query(name = "ProtectedContent.findByDeleteAfterPassed")
    Page<ProtectedContent> findByDeleteAfterPassed(Pageable pageable, @Param("deleteAfter") Instant deleteAfter);



}
