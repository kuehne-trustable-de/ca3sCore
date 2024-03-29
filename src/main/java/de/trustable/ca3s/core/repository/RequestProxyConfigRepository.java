package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.RequestProxyConfig;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the RequestProxyConfig entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RequestProxyConfigRepository extends JpaRepository<RequestProxyConfig, Long> {

}
