package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CRLExpirationNotification;
import de.trustable.ca3s.core.repository.CRLExpirationNotificationRepository;
import de.trustable.ca3s.core.service.CRLExpirationNotificationService;
import de.trustable.ca3s.core.service.dto.CRLExpirationNotificationView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.PaginationUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link CRLExpirationNotification}.
 */
@RestController
@RequestMapping("/api")
public class CRLExpirationNotificationViewResource {

    private final Logger log = LoggerFactory.getLogger(CRLExpirationNotificationViewResource.class);

    private static final String ENTITY_NAME = "cRLExpirationNotification";


    private final CRLExpirationNotificationService cRLExpirationNotificationService;

    private final CRLExpirationNotificationRepository cRLExpirationNotificationRepository;

    public CRLExpirationNotificationViewResource(
        CRLExpirationNotificationService cRLExpirationNotificationService,
        CRLExpirationNotificationRepository cRLExpirationNotificationRepository
    ) {
        this.cRLExpirationNotificationService = cRLExpirationNotificationService;
        this.cRLExpirationNotificationRepository = cRLExpirationNotificationRepository;
    }

    /**
     * {@code GET  /certificates} : get all the certificates.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/crl-expiration-notification-views")
    public ResponseEntity<List<CRLExpirationNotificationView>> getAllCertificates(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CRLExpirationNotificationView");

        Page<CRLExpirationNotification> page = cRLExpirationNotificationRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);

        List<CRLExpirationNotificationView> cenList = getFullCRLExpirationNotificationViews(page);

        return ResponseEntity.ok().headers(headers).body(cenList);
    }

    private List<CRLExpirationNotificationView> getFullCRLExpirationNotificationViews(Page<CRLExpirationNotification> page) {

        List<CRLExpirationNotificationView> crlExpirationNotificationViewList = new ArrayList<>();
        for(CRLExpirationNotification crlExpirationNotification: page.getContent()){
            crlExpirationNotificationViewList.add( new CRLExpirationNotificationView(crlExpirationNotification));
        }
        return crlExpirationNotificationViewList;
    }

    /**
     * {@code GET  /crl-expiration-notifications/:id} : get the "id" cRLExpirationNotification.
     *
     * @param id the id of the cRLExpirationNotification to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the cRLExpirationNotification, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/crl-expiration-notification-views/{id}")
    public ResponseEntity<CRLExpirationNotificationView> getCRLExpirationNotificationView(@PathVariable Long id) {
        log.debug("REST request to get CRLExpirationNotification : {}", id);
        Optional<CRLExpirationNotification> crlExpirationNotificationOptional = cRLExpirationNotificationService.findOne(id);
        if( crlExpirationNotificationOptional.isPresent()){
            return ResponseEntity.ok( new CRLExpirationNotificationView(crlExpirationNotificationOptional.get()));
        }
        return ResponseEntity.notFound().build();
    }


}
