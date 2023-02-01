package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CRLExpirationNotificationRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.CRLExpirationNotificationService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.trustable.ca3s.core.service.util.CertificateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CRLExpirationNotification}.
 */
@Service
@Transactional
public class CRLExpirationNotificationServiceImpl implements CRLExpirationNotificationService {

    private final Logger log = LoggerFactory.getLogger(CRLExpirationNotificationServiceImpl.class);

    private final CRLExpirationNotificationRepository cRLExpirationNotificationRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateUtil certUtil;

    public CRLExpirationNotificationServiceImpl(CRLExpirationNotificationRepository cRLExpirationNotificationRepository, CertificateRepository certificateRepository, CertificateUtil certUtil) {
        this.cRLExpirationNotificationRepository = cRLExpirationNotificationRepository;
        this.certificateRepository = certificateRepository;
        this.certUtil = certUtil;
    }

    @Override
    public List<CRLExpirationNotification> createByCertificateId(Long certificateId) {
        log.debug("Request to create a CRLExpirationNotification from certificate id : {}", certificateId);

        List<CRLExpirationNotification> crlExpirationNotificationList = new ArrayList<>();
        Optional<Certificate> certificateOptional = certificateRepository.findById(certificateId);
        if( certificateOptional.isPresent()){
            Certificate certificate = certificateOptional.get();
            if( certificate.getIssuingCertificate() != null) {
                List<String> crlUrls = certUtil.getCertAttributes(certificate, CertificateAttribute.ATTRIBUTE_CRL_URL);
                for( String crlUrl: crlUrls) {
                    CRLExpirationNotification crlExpirationNotification = new CRLExpirationNotification();

                    crlExpirationNotification.setCrlUrl(crlUrl);
                    crlExpirationNotification.setNotifyUntil(certificate.getIssuingCertificate().getValidTo());
                    crlExpirationNotification.setNotifyBefore(Duration.ofDays(7));
                    crlExpirationNotificationList.add(crlExpirationNotification);
                }
            }
        }
        cRLExpirationNotificationRepository.saveAll(crlExpirationNotificationList);
        return crlExpirationNotificationList;
    }

    @Override
    public CRLExpirationNotification save(CRLExpirationNotification cRLExpirationNotification) {
        log.debug("Request to save CRLExpirationNotification : {}", cRLExpirationNotification);
        return cRLExpirationNotificationRepository.save(cRLExpirationNotification);
    }

    @Override
    public CRLExpirationNotification update(CRLExpirationNotification cRLExpirationNotification) {
        log.debug("Request to update CRLExpirationNotification : {}", cRLExpirationNotification);
        return cRLExpirationNotificationRepository.save(cRLExpirationNotification);
    }

    @Override
    public Optional<CRLExpirationNotification> partialUpdate(CRLExpirationNotification cRLExpirationNotification) {
        log.debug("Request to partially update CRLExpirationNotification : {}", cRLExpirationNotification);

        return cRLExpirationNotificationRepository
            .findById(cRLExpirationNotification.getId())
            .map(existingCRLExpirationNotification -> {
                if (cRLExpirationNotification.getCrlUrl() != null) {
                    existingCRLExpirationNotification.setCrlUrl(cRLExpirationNotification.getCrlUrl());
                }
                if (cRLExpirationNotification.getNotifyBefore() != null) {
                    existingCRLExpirationNotification.setNotifyBefore(cRLExpirationNotification.getNotifyBefore());
                }

                return existingCRLExpirationNotification;
            })
            .map(cRLExpirationNotificationRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CRLExpirationNotification> findAll() {
        log.debug("Request to get all CRLExpirationNotifications");
        return cRLExpirationNotificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CRLExpirationNotification> findOne(Long id) {
        log.debug("Request to get CRLExpirationNotification : {}", id);
        return cRLExpirationNotificationRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete CRLExpirationNotification : {}", id);
        cRLExpirationNotificationRepository.deleteById(id);
    }
}
