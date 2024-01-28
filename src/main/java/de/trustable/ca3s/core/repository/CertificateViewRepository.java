package de.trustable.ca3s.core.repository;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.service.dto.CertificateView;
import de.trustable.ca3s.core.service.util.CertificateSelectionUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class CertificateViewRepository {

    private final Logger LOG = LoggerFactory.getLogger(CertificateViewRepository.class);

    final private EntityManager entityManager;

    final private CertificateSelectionUtil certificateSelectionAttributeList;

    final private CertificateRepository certificateRepository;

    final private CRLExpirationNotificationRepository crlExpirationNotificationRepository;

    final private UserRepository userRepository;

    private final  AuditTraceRepository auditTraceRepository;

    private final CertificateUtil certificateUtil;
    private final String certificateStoreIsolation;

    public CertificateViewRepository(EntityManager entityManager,
                                     CertificateSelectionUtil certificateSelectionAttributeList,
                                     CertificateRepository certificateRepository,
                                     CRLExpirationNotificationRepository crlExpirationNotificationRepository,
                                     UserRepository userRepository,
                                     AuditTraceRepository auditTraceRepository,
                                     CertificateUtil certificateUtil,
                                     @Value("${ca3s.ui.certificate-store.isolation:none}")String certificateStoreIsolation
                                     ) {
        this.entityManager = entityManager;
        this.certificateSelectionAttributeList = certificateSelectionAttributeList;
        this.certificateRepository = certificateRepository;
        this.crlExpirationNotificationRepository = crlExpirationNotificationRepository;
        this.userRepository = userRepository;
        this.auditTraceRepository = auditTraceRepository;
        this.certificateUtil = certificateUtil;
        this.certificateStoreIsolation = certificateStoreIsolation;

    }

    public Page<CertificateView> findSelection(Map<String, String[]> parameterMap) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userName = auth.getName();
        if( userName == null) {
            LOG.warn("Current user == null!");
            throw new UsernameNotFoundException("Current user == null!");
        }

        Optional<User> optCurrentUser = userRepository.findOneByLogin(userName);
        if(!optCurrentUser.isPresent()) {
            LOG.warn("Name of ra officer '{}' not found as user", userName);
            throw new UsernameNotFoundException("Current user == null!");
        }

        return CertificateSpecifications.handleQueryParamsCertificateView(entityManager,
            cb,
            parameterMap,
            certificateSelectionAttributeList.getCertificateSelectionAttributes(),
            certificateRepository,
            optCurrentUser.get(),
            certificateStoreIsolation);

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
