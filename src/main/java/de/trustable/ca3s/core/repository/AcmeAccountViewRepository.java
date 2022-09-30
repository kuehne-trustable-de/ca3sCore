package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.service.dto.AcmeAccountView;
import de.trustable.ca3s.core.service.util.AcmeAccountUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


@Service
public class AcmeAccountViewRepository {

    final private EntityManager entityManager;
    final private AcmeOrderRepository acmeOrderRepository;
    final private AcmeAccountRepository acmeAccountRepository;
    final private AcmeContactRepository acmeContactRepository;
    final private AcmeAccountUtil acmeAccountUtil;

    public AcmeAccountViewRepository(EntityManager entityManager,
                                     AcmeOrderRepository acmeOrderRepository, AcmeAccountRepository acmeAccountRepository,
                                     AcmeContactRepository acmeContactRepository, AcmeAccountUtil acmeAccountUtil) {
        this.entityManager = entityManager;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeAccountRepository = acmeAccountRepository;
        this.acmeContactRepository = acmeContactRepository;
        this.acmeAccountUtil = acmeAccountUtil;
    }

    public Page<AcmeAccountView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return AcmeAccountSpecifications.handleQueryParamsAcmeAccountView(entityManager,
            acmeOrderRepository,
            acmeContactRepository,
            cb,
            parameterMap,
            new ArrayList<>());
    }

    public Optional<AcmeAccountView> findbyCertificateId(final Long acmeAccountId) {

        Optional<AcmeAccount> optCert = acmeAccountRepository.findById(acmeAccountId);
        return optCert.map(acmeAccountUtil::from);
    }
}
