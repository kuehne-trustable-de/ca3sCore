package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CertificateComment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the CertificateComment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CertificateCommentRepository extends JpaRepository<CertificateComment, Long> {}
