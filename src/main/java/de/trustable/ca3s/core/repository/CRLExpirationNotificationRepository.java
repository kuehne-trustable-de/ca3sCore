package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.dto.CertificateView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;

/**
 * Spring Data JPA repository for the CRLExpirationNotification entity.
 */
@Repository
public interface CRLExpirationNotificationRepository extends JpaRepository<CRLExpirationNotification, Long> {

    List<CRLExpirationNotification> findByCrlUrl(String crl_url);

    Page<CRLExpirationNotification> findAll(Pageable pageable );

}
