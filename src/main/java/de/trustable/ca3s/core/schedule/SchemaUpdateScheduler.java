package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.*;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.MailService;
import de.trustable.ca3s.core.service.util.CRLUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.CryptoService;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import de.trustable.util.CryptoUtil;
import org.bouncycastle.asn1.x509.CRLReason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;

import javax.naming.NamingException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.cert.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class SchemaUpdateScheduler {

	transient Logger LOG = LoggerFactory.getLogger(SchemaUpdateScheduler.class);

	final static int MAX_RECORDS_PER_TRANSACTION = 10000;

	@Autowired
	private CertificateRepository certificateRepo;

	@Autowired
	private CertificateUtil certUtil;

    @Autowired
    private AuditService auditService;


//    @Scheduled(fixedDelay = 3600000)
    @Scheduled(fixedDelay = 60000)
	public void updateCertificateAttributes() {

		Instant now = Instant.now();

		List<Certificate> updateCertificateList = certificateRepo.findByAttributeValueLowerThan(CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION,
            "" + CertificateUtil.CURRENT_ATTRIBUTES_VERSION);

		int count = 0;
		for (Certificate cert : updateCertificateList) {

            X509Certificate x509Cert;
            try {
                int currentVersion = Integer.parseInt( certUtil.getCertAttribute(cert, CertificateAttribute.ATTRIBUTE_ATTRIBUTES_VERSION));

                x509Cert = CryptoService.convertPemToCertificate(cert.getContent());

                if( currentVersion < 4 ){
                    certUtil.interpretBasicConstraint(x509Cert, cert);
                }
                certUtil.addAdditionalCertificateAttributes(x509Cert, cert);

                certificateRepo.save(cert);
                LOG.info("attribute schema updated for certificate id {} ", cert.getId());
            } catch (GeneralSecurityException | IOException e) {
                LOG.error("problem with attribute schema update for certificate id " + cert.getId(), e);
            }

			if( count++ > MAX_RECORDS_PER_TRANSACTION) {
				LOG.info("limited certificate validity processing to {} per call", MAX_RECORDS_PER_TRANSACTION);
				break;
			}
		}
		if( count > 0){
            auditService.saveAuditTrace(auditService.createAuditTraceCertificateSchemaUpdated(count, CertificateUtil.CURRENT_ATTRIBUTES_VERSION ));
        }

	}
}
