package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.CertificateAttribute;
import de.trustable.ca3s.core.repository.CertificateAttributeRepository;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional; 
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.CertificateAttribute}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class CertificateAttributeResource {

    private final Logger log = LoggerFactory.getLogger(CertificateAttributeResource.class);

    private static final String ENTITY_NAME = "certificateAttribute";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CertificateAttributeRepository certificateAttributeRepository;

    public CertificateAttributeResource(CertificateAttributeRepository certificateAttributeRepository) {
        this.certificateAttributeRepository = certificateAttributeRepository;
    }

    /**
     * {@code POST  /certificate-attributes} : Create a new certificateAttribute.
     *
     * @param certificateAttribute the certificateAttribute to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new certificateAttribute, or with status {@code 400 (Bad Request)} if the certificateAttribute has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/certificate-attributes")
    public ResponseEntity<CertificateAttribute> createCertificateAttribute(@Valid @RequestBody CertificateAttribute certificateAttribute) throws URISyntaxException {
        log.debug("REST request to save CertificateAttribute : {}", certificateAttribute);
        if (certificateAttribute.getId() != null) {
            throw new BadRequestAlertException("A new certificateAttribute cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CertificateAttribute result = certificateAttributeRepository.save(certificateAttribute);
        return ResponseEntity.created(new URI("/api/certificate-attributes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /certificate-attributes} : Updates an existing certificateAttribute.
     *
     * @param certificateAttribute the certificateAttribute to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated certificateAttribute,
     * or with status {@code 400 (Bad Request)} if the certificateAttribute is not valid,
     * or with status {@code 500 (Internal Server Error)} if the certificateAttribute couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/certificate-attributes")
    public ResponseEntity<CertificateAttribute> updateCertificateAttribute(@Valid @RequestBody CertificateAttribute certificateAttribute) throws URISyntaxException {
        log.debug("REST request to update CertificateAttribute : {}", certificateAttribute);
        if (certificateAttribute.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        CertificateAttribute result = certificateAttributeRepository.save(certificateAttribute);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, certificateAttribute.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /certificate-attributes} : get all the certificateAttributes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of certificateAttributes in body.
     */
    @GetMapping("/certificate-attributes")
    public List<CertificateAttribute> getAllCertificateAttributes() {
        log.debug("REST request to get all CertificateAttributes");
        return certificateAttributeRepository.findAll();
    }

    /**
     * {@code GET  /certificate-attributes/:id} : get the "id" certificateAttribute.
     *
     * @param id the id of the certificateAttribute to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the certificateAttribute, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/certificate-attributes/{id}")
    public ResponseEntity<CertificateAttribute> getCertificateAttribute(@PathVariable Long id) {
        log.debug("REST request to get CertificateAttribute : {}", id);
        Optional<CertificateAttribute> certificateAttribute = certificateAttributeRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(certificateAttribute);
    }

    /**
     * {@code DELETE  /certificate-attributes/:id} : delete the "id" certificateAttribute.
     *
     * @param id the id of the certificateAttribute to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/certificate-attributes/{id}")
    public ResponseEntity<Void> deleteCertificateAttribute(@PathVariable Long id) {
        log.debug("REST request to delete CertificateAttribute : {}", id);
        certificateAttributeRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
