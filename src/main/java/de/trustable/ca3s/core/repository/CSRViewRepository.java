package de.trustable.ca3s.core.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.CertificateSelectionUtil;
import de.trustable.ca3s.core.web.rest.util.UserUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.service.dto.CSRView;


@Service
public class CSRViewRepository {

    final private EntityManager entityManager;
    final private CertificateSelectionUtil certificateSelectionAttributeList;
    final private CSRRepository csrRepository;
    final private CSRUtil csrUtil;
    final private UserUtil userUtil;
    private final String certificateStoreIsolation;


    public CSRViewRepository(EntityManager entityManager,
                             CertificateSelectionUtil certificateSelectionAttributeList,
                             CSRRepository csrRepository,
                             CSRUtil csrUtil,
                             UserUtil userUtil,
                             @Value("${ca3s.ui.certificate-store.isolation:none}")String certificateStoreIsolation){

        this.entityManager = entityManager;
        this.certificateSelectionAttributeList = certificateSelectionAttributeList;
        this.csrRepository = csrRepository;
        this.csrUtil = csrUtil;
        this.userUtil = userUtil;
        this.certificateStoreIsolation = certificateStoreIsolation;
    }

    public Page<CSRView> findSelection(Map<String, String[]> parameterMap, List<Long> pipelineIds){

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();

		return CSRSpecifications.handleQueryParamsCSRView(entityManager,
				cb,
				parameterMap,
            certificateSelectionAttributeList.getCertificateSelectionAttributes(),
            pipelineIds,
            userUtil.getCurrentUser(),
            certificateStoreIsolation);
	}

    public Optional<CSRView> findbyCSRId(final Long csrId) {

        Optional<CSR> optCSR = csrRepository.findById(csrId);
        if (optCSR.isPresent()) {
            return Optional.of(new CSRView(csrUtil, optCSR.get(), false));
        }
        return Optional.empty();
    }

}
