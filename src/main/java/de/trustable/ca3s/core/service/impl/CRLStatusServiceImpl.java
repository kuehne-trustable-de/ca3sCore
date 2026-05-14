package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.repository.CertificateRepository;
import de.trustable.ca3s.core.service.CRLStatusService;
import de.trustable.ca3s.core.service.dto.CrlEndpointStatus;
import de.trustable.ca3s.core.service.dto.CrlStatusSet;
import de.trustable.ca3s.core.service.dto.CrlUrlStatus;
import de.trustable.ca3s.core.service.util.CRLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import java.io.IOException;
import java.security.cert.*;
import java.time.Instant;
import java.util.*;

/**
 * Service Implementation for managing {@link CRLExpirationNotification}.
 */
@Service
@Transactional
public class CRLStatusServiceImpl implements CRLStatusService {

    private final Logger LOG = LoggerFactory.getLogger(CRLStatusServiceImpl.class);

    private final CertificateRepository certificateRepository;

    private CrlStatusSet crlStatusSet;

    private final CRLUtil crlUtil;

    public CRLStatusServiceImpl(CertificateRepository certificateRepository, CRLUtil crlUtil) {
        this.certificateRepository = certificateRepository;
        this.crlUtil = crlUtil;
        this.crlStatusSet = new CrlStatusSet(Collections.emptyList(), Instant.now());
    }

    @Override
    public void updateStatus() {

        Instant now = Instant.now();
        long nowMilli = now.toEpochMilli();

        List<String> crlURLList = certificateRepository.findDistinctCrlURLForActiveCertificates();
        LOG.debug("findDistinctCrlURLForActiveCertificates returns #{} distinct CRL endpoints in {} ms", crlURLList.size(), System.currentTimeMillis() - nowMilli);

        List<CrlUrlStatus> newCrlUrlStatusList = new ArrayList<>();

        for( String crlUrl: crlURLList){

            CrlUrlStatus crlUrlStatus = new CrlUrlStatus();
            crlUrlStatus.setCrlUrl(crlUrl);
            crlUrlStatus.setCrlEndpointStatus(CrlEndpointStatus.OK);

            try {
                LOG.debug("downloading CRL '{}'", crlUrl);
                X509CRL crl = crlUtil.downloadCRL(crlUrl);
                if (crl == null) {
                    LOG.debug("downloaded CRL == null ");
                    crlUrlStatus.setCrlEndpointStatus(CrlEndpointStatus.ACCESS_ERROR);
                    continue;
                }

                crlUrlStatus.setCrl(crl);

                if (crl.getNextUpdate() == null) {
                    LOG.warn("nextUpdate missing in CRL '{}'", crlUrl);
                } else {
                    long nextUpdate = crl.getNextUpdate().getTime();
                    if( nextUpdate > nowMilli){
                        LOG.debug("next check for '{}' in {} sec.", crlUrl, (nextUpdate - nowMilli)/1000L);
                    }else{
                        LOG.warn("crl for '{}' expired for {} sec.", crlUrl, (nowMilli - nextUpdate)/1000L);
                        crlUrlStatus.setCrlEndpointStatus(CrlEndpointStatus.EXPIRED);
                    }

                }
            } catch (CertificateException | CRLException | IOException | NamingException e2) {

                if( LOG.isDebugEnabled()) {
                    LOG.debug("CRL retrieval for '" + crlUrl + "' failed", e2);
                }
                LOG.warn("CRL retrieval for '" + crlUrl + "' failed with reason {}", e2.getMessage());
                crlUrlStatus.setCrlEndpointStatus(CrlEndpointStatus.ACCESS_ERROR);
            }

            newCrlUrlStatusList.add(crlUrlStatus);
        }

        LOG.info("#{} CRL endpoints accessible, was #{} on last check", newCrlUrlStatusList.size(), this.crlStatusSet.getCrlUrlStatusList().size());
        this.crlStatusSet = new CrlStatusSet( newCrlUrlStatusList, now);
    }

    @Override
    public CrlStatusSet getCrlStatusSet(){
      return  crlStatusSet;
    }
}
