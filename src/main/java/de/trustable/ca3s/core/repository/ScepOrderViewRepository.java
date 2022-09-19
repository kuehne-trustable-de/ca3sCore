package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.domain.ScepOrder;
import de.trustable.ca3s.core.service.dto.AcmeOrderView;
import de.trustable.ca3s.core.service.dto.ScepOrderView;
import de.trustable.ca3s.core.service.util.AcmeOrderUtil;
import de.trustable.ca3s.core.service.util.ScepOrderUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


@Service
public class ScepOrderViewRepository {

    final private EntityManager entityManager;
    final private ScepOrderRepository scepOrderRepository;
    final private ScepOrderUtil scepOrderUtil;

    public ScepOrderViewRepository(EntityManager entityManager,
                                   ScepOrderRepository scepOrderRepository,
                                   ScepOrderUtil scepOrderUtil) {
        this.entityManager = entityManager;
        this.scepOrderRepository = scepOrderRepository;
        this.scepOrderUtil = scepOrderUtil;
    }

    public Page<ScepOrderView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return ScepOrderSpecifications.handleQueryParamsScepOrderView(entityManager,
            scepOrderRepository,
            scepOrderUtil,
            cb,
            parameterMap,
            new ArrayList<>());
    }

}
