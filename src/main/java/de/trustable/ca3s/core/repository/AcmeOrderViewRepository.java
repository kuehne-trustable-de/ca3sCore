package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.dto.AcmeOrderView;
import de.trustable.ca3s.core.service.util.AcmeOrderUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


@Service
public class AcmeOrderViewRepository {

    final private EntityManager entityManager;
    final private AcmeOrderRepository acmeOrderRepository;
    final private AcmeOrderUtil acmeOrderUtil;

    public AcmeOrderViewRepository(EntityManager entityManager,
                                   AcmeOrderRepository acmeOrderRepository,
                                   AcmeOrderUtil acmeOrderUtil) {
        this.entityManager = entityManager;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeOrderUtil = acmeOrderUtil;
    }

    public Page<AcmeOrderView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return AcmeOrderSpecifications.handleQueryParamsAcmeOrderView(entityManager,
            acmeOrderRepository,
            acmeOrderUtil,
            cb,
            parameterMap,
            new ArrayList<>());
    }

    public Optional<AcmeOrderView> findbyAcmeOrderId(final Long acmeOrderId) {

        Optional<AcmeOrder> optCert = acmeOrderRepository.findById(acmeOrderId);
        return optCert.map(acmeOrderUtil::from);
    }
}
