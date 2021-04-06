package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.service.dto.AuditTraceView;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Map;


@Service
public class AuditTraceViewRepository {

	private final EntityManager entityManager;

    public AuditTraceViewRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Page<AuditTraceView> findSelection(Map<String, String[]> parameterMap){

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		return AuditTraceSpecifications.handleQueryParamsCertificateView(entityManager,
				cb,
				parameterMap);

	}

}
