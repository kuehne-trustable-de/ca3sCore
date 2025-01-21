package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.ProtectedContent;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.CsrStatus;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.ProtectedContentRepository;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.util.CSRUtil;
import de.trustable.ca3s.core.service.util.PasswordUtil;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.transaction.Transactional;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ProtectedContentScheduleUtil {

    private final Logger log = LoggerFactory.getLogger(ProtectedContentScheduleUtil.class);

    private final ProtectedContentRepository protContentRepository;
    final private CSRRepository csrRepository;
    final private CSRUtil csrUtil;
    final private AuditService auditService;


	public ProtectedContentScheduleUtil(ProtectedContentRepository protContentRepository,
                                        CSRRepository csrRepository, CSRUtil csrUtil, AuditService auditService ) {

        this.protContentRepository = protContentRepository;
        this.csrRepository = csrRepository;
        this.csrUtil = csrUtil;
        this.auditService = auditService;

	}



    @Transactional
    public void invalidateCSRsWithExpiringPC(int maxRecordsPerTransaction) {
        Page<ProtectedContent> invalidList = protContentRepository.findByValidToPassed(PageRequest.of(0, maxRecordsPerTransaction), Instant.now());
        for (ProtectedContent pc : invalidList) {
            if (ContentRelationType.CSR.equals(pc.getRelationType())) {
                Optional<CSR> optCsr = csrRepository.findById(pc.getRelatedId());
                if (optCsr.isPresent()) {
                    CSR csr = optCsr.get();
                    if (CsrStatus.PENDING.equals(csr.getStatus())) {
                        // Invalidate request
                        csrUtil.setStatusAndRejectionReason(csr, CsrStatus.REJECTED, "created key for csr expired");
                        auditService.saveAuditTrace(
                            auditService.createAuditTraceCsrRejected(csr, "created key for csr expired"));
                    }
                }
            }
        }
    }

    @Transactional
    public void deleteExpiredElements(int maxRecordsPerTransaction) {
        Page<ProtectedContent> expiredList = protContentRepository.findByDeleteAfterPassed(PageRequest.of(0, maxRecordsPerTransaction), Instant.now());
        if (!expiredList.isEmpty()) {
            log.info("deleting #{} expired ProtectedContent objects", expiredList.getNumberOfElements());
            for (ProtectedContent pc : expiredList) {
                log.debug("delete protected content due to passed deletion date: {}", pc);
            }
            protContentRepository.deleteAll(expiredList);
        }
    }

}
