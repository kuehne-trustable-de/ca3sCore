package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.schedule.CertExpiryScheduler;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.transaction.Transactional;

/**
 * REST controller for starting scheduled tasks.
 */
@RestController
@RequestMapping("/api")
public class ScheduleSupport {

    private final Logger LOG = LoggerFactory.getLogger(ScheduleSupport.class);

    final private CertExpiryScheduler certExpiryScheduler;

    public ScheduleSupport(CertExpiryScheduler certExpiryScheduler) {
        this.certExpiryScheduler = certExpiryScheduler;
    }


    /**
     * {@code POST  api/schedule/retrieveCertificates} : retrieve certificates.
     *
     * @return the number of expiring certificates .
     */
    @Transactional
    @PostMapping("schedule/retrieveCertificates")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int retrieveCertificates() {

        long startTime = System.currentTimeMillis();
        certExpiryScheduler.retrieveCertificates();
        LOG.debug("calling certExpiryScheduler.retrieveCertificates() took {} ms", (System.currentTimeMillis()-startTime) );
        return 0;
    }

    /**
     * {@code POST  api/schedule/updateRevocationStatus} : get revocation status of all certificates.
     *
     * @return the number of expiring certificates .
     */
    @Transactional
    @PostMapping("schedule/updateRevocationStatus")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public int updateRevocationStatus() {

        long startTime = System.currentTimeMillis();
        certExpiryScheduler.updateRevocationStatus();
        LOG.debug("calling certExpiryScheduler.updateRevocationStatus() took {} ms", (System.currentTimeMillis()-startTime) );
        return 0;
    }

}
