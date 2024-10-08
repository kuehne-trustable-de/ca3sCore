package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.TimedElementNotification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TimedElementNotification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TimedElementNotificationRepository extends JpaRepository<TimedElementNotification, Long> {}
