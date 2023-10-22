package de.trustable.ca3s.core.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CertificateSelectionUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import de.trustable.ca3s.core.service.dto.CertificateView;


@Service
public class CertificateViewRepository {

    final private EntityManager entityManager;

    final private CertificateSelectionUtil certificateSelectionAttributeList;

    final private CertificateRepository certificateRepository;

    final private CRLExpirationNotificationRepository crlExpirationNotificationRepository;

    private final  AuditTraceRepository auditTraceRepository;

    private final CertificateUtil certificateUtil;

    public CertificateViewRepository(EntityManager entityManager, CertificateSelectionUtil certificateSelectionAttributeList, CertificateRepository certificateRepository, CRLExpirationNotificationRepository crlExpirationNotificationRepository, AuditTraceRepository auditTraceRepository, CertificateUtil certificateUtil) {
        this.entityManager = entityManager;
        this.certificateSelectionAttributeList = certificateSelectionAttributeList;
        this.certificateRepository = certificateRepository;
        this.crlExpirationNotificationRepository = crlExpirationNotificationRepository;
        this.auditTraceRepository = auditTraceRepository;
        this.certificateUtil = certificateUtil;
    }

    public Page<CertificateView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        return CertificateSpecifications.handleQueryParamsCertificateView(entityManager,
            cb,
            parameterMap,
            certificateSelectionAttributeList.getCertificateSelectionAttributes());

    }

    public Optional<CertificateView> findbyCertificateId(final Long certificateId) {

        Optional<Certificate> optCert = certificateRepository.findById(certificateId);
        if (optCert.isPresent()) {
            Certificate cert = optCert.get();
            CertificateView certificateView = new CertificateView(cert);

            if( !cert.isEndEntity()) {
                certificateView.setIssuingActiveCertificates(certificateUtil.hasIssuedActiveCertificates(cert));
            }
            certificateView.setAuditPresent( !auditTraceRepository.findByCsrAndCert(cert, cert.getCsr()).isEmpty());

            List<CRLExpirationNotification> crlExpirationNotificationList = crlExpirationNotificationRepository.findByCrlUrl(certificateView.getCrlUrl());
            if( !crlExpirationNotificationList.isEmpty() ){
                certificateView.setCrlExpirationNotificationId(crlExpirationNotificationList.get(0).getId());
            }
            return Optional.of(certificateView);
        }
        return Optional.empty();
    }
}
