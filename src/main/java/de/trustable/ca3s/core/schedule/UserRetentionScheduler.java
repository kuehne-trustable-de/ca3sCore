package de.trustable.ca3s.core.schedule;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.CSR;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CSRRepository;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 *
 * @author kuehn
 *
 */
@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class UserRetentionScheduler {

	transient Logger LOG = LoggerFactory.getLogger(UserRetentionScheduler.class);

	private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;
    private final CSRRepository csrRepository;
    private final int defaultRetentionPeriod;
    private final int csrOwnerRetentionPeriod;
    private final int certificateOwnerRetentionPeriod;

    public UserRetentionScheduler(UserRepository userRepository,
                                  CertificateRepository certificateRepository,
                                  CSRRepository csrRepository,
                                  @Value("${ca3s.user.retention.default:5}") int defaultRetentionPeriod,
                                  @Value("${ca3s.user.retention.default:90}") int csrOwnerRetentionPeriod,
                                  @Value("${ca3s.user.retention.default:3600}") int certificateOwnerRetentionPeriod) {
        this.userRepository = userRepository;
        this.certificateRepository = certificateRepository;
        this.csrRepository = csrRepository;
        this.defaultRetentionPeriod = defaultRetentionPeriod;
        this.csrOwnerRetentionPeriod = csrOwnerRetentionPeriod;
        this.certificateOwnerRetentionPeriod = certificateOwnerRetentionPeriod;
    }

    @Scheduled(cron="${ca3s.schedule.cron.dropUnrelatedUsersCron:0 20 02 * * ?}")
//    @Scheduled(fixedDelay = 60000)
    public void retrieveUnrelatedUsers() {

        Instant oldestRelevantCertificateExpiry = Instant.now().minus(certificateOwnerRetentionPeriod, ChronoUnit.DAYS);
        Instant oldestRelevantCSR = Instant.now().minus(csrOwnerRetentionPeriod, ChronoUnit.DAYS);
        Instant oldestRelevantLogin = Instant.now().minus(defaultRetentionPeriod, ChronoUnit.DAYS);

		for (User user : userRepository.findAll()) {

            // start with the easiest check ...
            if( hasRecentActivity(user, oldestRelevantLogin)){
                LOG.debug("user {} has recent login", user.getLogin());
                continue;
            }

            if(hasAuthority(user, AuthoritiesConstants.ADMIN) ){
                LOG.debug("user {} ignored from retention check. Has role {}", user.getLogin(), AuthoritiesConstants.ADMIN);
                continue;
            }
            if(hasAuthority(user, AuthoritiesConstants.RA_OFFICER) ){
                LOG.debug("user {} ignored from retention check. Has role {}", user.getLogin(), AuthoritiesConstants.RA_OFFICER);
                continue;
            }
            if(hasAuthority(user, AuthoritiesConstants.DOMAIN_RA_OFFICER) ){
                LOG.debug("user {} ignored from retention check. Has role {}", user.getLogin(), AuthoritiesConstants.DOMAIN_RA_OFFICER);
                continue;
            }

            if( hasRelevantCertificate(user, oldestRelevantCertificateExpiry)){
                LOG.debug("user {} has relevant certificate", user.getLogin());
                continue;
            }

            if( hasRelevantCSR(user, oldestRelevantCSR)){
                LOG.debug("user {} has relevant csr", user.getLogin());
                continue;
            }

            LOG.info("user {} has no relevant data left, deleting ...", user.getLogin());

            // just log the users for now ...
            //            userRepository.delete(user);

        }
	}

    private boolean hasAuthority(final User user, final String authorityName) {
        for( Authority authority : user.getAuthorities()) {
            if (authorityName.equalsIgnoreCase(authority.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRecentActivity(User user, Instant oldestRelevantLogin) {
        if( user.getCreatedDate() != null &&
            user.getCreatedDate().isAfter(oldestRelevantLogin)){
            return true;
        }
        if( user.getLastModifiedDate() != null &&
            user.getLastModifiedDate().isAfter(oldestRelevantLogin)){
            return true;
        }
        if( user.getLastUserDetailsUpdate() != null &&
            user.getLastUserDetailsUpdate().isAfter(oldestRelevantLogin)){
            return true;
        }
        return false;
    }

    private boolean hasRelevantCSR(User user, Instant oldestRelevantCSR) {
        for(CSR csr : csrRepository.findByRequestor(user.getLogin())){
            if( csr.getRequestedOn().isAfter(oldestRelevantCSR) ){
                LOG.debug("user {} has relevant csr {} requested on {} ", user.getLogin(),
                    csr.getId(), csr.getRequestedOn());
                return true;
            }
        }
        return true;
    }

    private boolean hasRelevantCertificate(User user, Instant oldestRelevantCertificateExpiry) {
        for(Certificate certificate : certificateRepository.findByRequestor(user.getLogin())){
            if( certificate.getValidTo().isAfter(oldestRelevantCertificateExpiry) ){
                LOG.debug("user {} has relevant certificate {} valid until {} ", user.getLogin(),
                    certificate.getId(), certificate.getValidTo());
                return true;
            }
        }
        return true;
    }
}
