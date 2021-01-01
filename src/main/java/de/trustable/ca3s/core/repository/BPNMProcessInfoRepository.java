package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.BPNMProcessInfo;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the BPNMProcessInfo entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BPNMProcessInfoRepository extends JpaRepository<BPNMProcessInfo, Long> {
}
