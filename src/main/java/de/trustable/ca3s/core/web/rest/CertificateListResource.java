package de.trustable.ca3s.core.web.rest;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import de.trustable.ca3s.core.repository.CertificatePageRepository;
import de.trustable.ca3s.core.repository.CertificateViewRepository;
import de.trustable.ca3s.core.service.dto.CertificateView;
import io.github.jhipster.web.util.PaginationUtil;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.Certificate}.
 */
@RestController
@RequestMapping("/publicapi")
public class CertificateListResource {

	@Autowired
	CertificatePageRepository certPageRepository;
	
	@Autowired
	CertificateViewRepository certificateViewRepository;
	
	
    private final Logger log = LoggerFactory.getLogger(CertificateListResource.class);


    /**
     * {@code GET  /certificates} : get all the certificates.
     *

     * @param pageable the pagination information.

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificates in body.
     */
    @GetMapping("/certificateList")
    public ResponseEntity<List<CertificateView>> getAllCertificates(Pageable pageable, HttpServletRequest request) {
        log.debug("REST request to get a page of CertificateViews");
        Page<CertificateView> page = certificateViewRepository.findSelection(request.getParameterMap());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

}
