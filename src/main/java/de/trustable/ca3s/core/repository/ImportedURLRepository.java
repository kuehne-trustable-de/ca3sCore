package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ImportedURL;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the ImportedURL entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ImportedURLRepository extends JpaRepository<ImportedURL, Long> {
}
