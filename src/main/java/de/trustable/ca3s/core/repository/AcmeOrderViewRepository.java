package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeOrder;
import de.trustable.ca3s.core.service.dto.ACMEOrderView;
import de.trustable.ca3s.core.service.util.ACMEOrderUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


@Service
public class ACMEOrderViewRepository {

    final private EntityManager entityManager;
    final private AcmeOrderRepository acmeOrderRepository;
    final private ACMEOrderUtil acmeOrderUtil;

    public ACMEOrderViewRepository(EntityManager entityManager,
                                   AcmeOrderRepository acmeOrderRepository,
                                   ACMEOrderUtil acmeOrderUtil) {
        this.entityManager = entityManager;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeOrderUtil = acmeOrderUtil;
    }

    public Page<ACMEOrderView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return ACMEOrderSpecifications.handleQueryParamsACMEOrderView(entityManager,
            acmeOrderRepository,
            acmeOrderUtil,
            cb,
            parameterMap,
            new ArrayList<>());
    }

    public Optional<ACMEOrderView> findbyACMEOrderId(final Long acmeOrderId) {

        Optional<AcmeOrder> optCert = acmeOrderRepository.findById(acmeOrderId);
        return optCert.map(acmeOrderUtil::from);
    }
}
