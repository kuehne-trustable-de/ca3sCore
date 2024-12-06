package de.trustable.ca3s.core.web.rest;

import java.util.Objects;
import java.util.Optional;

import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.CertificateViewRepository;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.trustable.ca3s.core.service.dto.CertificateView;
import org.springframework.web.client.HttpClientErrorException;

/**
 * REST controller for reading {@link de.trustable.ca3s.core.domain.Certificate} using the convenient CertificateView object.
 * Just read-only access to this resource.
 *
 */
@RestController
@RequestMapping("/api")
public class CertificateViewResource {

    private final Logger LOG = LoggerFactory.getLogger(CertificateViewResource.class);

    private final CertificateViewRepository certificateViewRepository;

    private final UserUtil userUtil;

    private final String certificateStoreIsolation;

    public CertificateViewResource(CertificateViewRepository certificateViewRepository,
                                   UserUtil userUtil,
                                   @Value("${ca3s.ui.certificate-store.isolation:none}")String certificateStoreIsolation
                                   ) {
        this.certificateViewRepository = certificateViewRepository;
        this.userUtil = userUtil;
        this.certificateStoreIsolation = certificateStoreIsolation;
    }

    /**
     * {@code GET  /certificates/:id} : get the "id" certificate.
     *
     * @param id the id of the certificate to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the certificate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/certificateViews/{id}")
    @Transactional
    public ResponseEntity<CertificateView> getCertificate(@PathVariable Long id) {
        LOG.debug("REST request to get CertificateView : {}", id);

        Optional<CertificateView> optCert = certificateViewRepository.findbyCertificateId(id);

        if( optCert.isPresent() ) {
            CertificateView certView = optCert.get();
            checkTenant(certView);
            userUtil.addUserDetails(certView);
    		return new ResponseEntity<>(certView, HttpStatus.OK);
        }

		return ResponseEntity.notFound().build();
    }

    private void checkTenant(CertificateView certView) {
        if( "none".equalsIgnoreCase(this.certificateStoreIsolation)){
            return;
        }

        if( !userUtil.isAdministrativeUser() ){
            User currentUser = userUtil.getCurrentUser();
            Tenant tenant = currentUser.getTenant();
            if( tenant == null ) {
                // null == default tenant
            } else if(!Objects.equals(tenant.getId(), certView.getTenantId())){
                if( Boolean.TRUE.equals(certView.getEndEntity())) {
                    LOG.info("user [{}] tried to download EE certificate [{}] of tenant [{}]",
                        currentUser.getLogin(), certView.getId(), tenant.getLongname());
                    throw new HttpClientErrorException(HttpStatus.NOT_FOUND);
                }
            }
        }
    }


}
