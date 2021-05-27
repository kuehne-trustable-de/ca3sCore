package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CSRComment;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data SQL repository for the CSRComment entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CSRCommentRepository extends JpaRepository<CSRComment, Long> {}
