package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.service.dto.ACMEAccountView;
import de.trustable.ca3s.core.service.util.ACMEAccountUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


@Service
public class ACMEAccountViewRepository {

    final private EntityManager entityManager;
    final private AcmeOrderRepository acmeOrderRepository;
    final private ACMEAccountRepository acmeAccountRepository;
    final private AcmeContactRepository acmeContactRepository;
    final private ACMEAccountUtil acmeAccountUtil;

    public ACMEAccountViewRepository(EntityManager entityManager,
                                     AcmeOrderRepository acmeOrderRepository, ACMEAccountRepository acmeAccountRepository,
                                     AcmeContactRepository acmeContactRepository, ACMEAccountUtil acmeAccountUtil) {
        this.entityManager = entityManager;
        this.acmeOrderRepository = acmeOrderRepository;
        this.acmeAccountRepository = acmeAccountRepository;
        this.acmeContactRepository = acmeContactRepository;
        this.acmeAccountUtil = acmeAccountUtil;
    }

    public Page<ACMEAccountView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return ACMEAccountSpecifications.handleQueryParamsACMEAccountView(entityManager,
            acmeOrderRepository,
            acmeContactRepository,
            cb,
            parameterMap,
            new ArrayList<>());
    }

    public Optional<ACMEAccountView> findbyCertificateId(final Long acmeAccountId) {

        Optional<ACMEAccount> optCert = acmeAccountRepository.findById(acmeAccountId);
        return optCert.map(acmeAccountUtil::from);
    }
}
