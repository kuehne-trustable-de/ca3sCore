package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.RequestAttributeValue;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.RequestAttributeValueService;
import de.trustable.ca3s.core.exception.BadRequestAlertException;

import org.springframework.security.access.prepost.PreAuthorize;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.RequestAttributeValue}.
 */
@RestController
@RequestMapping("/api")
public class RequestAttributeValueResource {

    private final Logger log = LoggerFactory.getLogger(RequestAttributeValueResource.class);

    private static final String ENTITY_NAME = "requestAttributeValue";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RequestAttributeValueService requestAttributeValueService;

    public RequestAttributeValueResource(RequestAttributeValueService requestAttributeValueService) {
        this.requestAttributeValueService = requestAttributeValueService;
    }

    /**
     * {@code POST  /request-attribute-values} : Create a new requestAttributeValue.
     *
     * @param requestAttributeValue the requestAttributeValue to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new requestAttributeValue, or with status {@code 400 (Bad Request)} if the requestAttributeValue has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/request-attribute-values")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestAttributeValue> createRequestAttributeValue(@Valid @RequestBody RequestAttributeValue requestAttributeValue) throws URISyntaxException {
        log.debug("REST request to save RequestAttributeValue : {}", requestAttributeValue);
        if (requestAttributeValue.getId() != null) {
            throw new BadRequestAlertException("A new requestAttributeValue cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RequestAttributeValue result = requestAttributeValueService.save(requestAttributeValue);
        return ResponseEntity.created(new URI("/api/request-attribute-values/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /request-attribute-values} : Updates an existing requestAttributeValue.
     *
     * @param requestAttributeValue the requestAttributeValue to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated requestAttributeValue,
     * or with status {@code 400 (Bad Request)} if the requestAttributeValue is not valid,
     * or with status {@code 500 (Internal Server Error)} if the requestAttributeValue couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/request-attribute-values")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestAttributeValue> updateRequestAttributeValue(@Valid @RequestBody RequestAttributeValue requestAttributeValue) throws URISyntaxException {
        log.debug("REST request to update RequestAttributeValue : {}", requestAttributeValue);
        if (requestAttributeValue.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RequestAttributeValue result = requestAttributeValueService.save(requestAttributeValue);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, requestAttributeValue.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /request-attribute-values} : get all the requestAttributeValues.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of requestAttributeValues in body.
     */
    @GetMapping("/request-attribute-values")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public List<RequestAttributeValue> getAllRequestAttributeValues() {
        log.debug("REST request to get all RequestAttributeValues");
        return requestAttributeValueService.findAll();
    }

    /**
     * {@code GET  /request-attribute-values/:id} : get the "id" requestAttributeValue.
     *
     * @param id the id of the requestAttributeValue to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the requestAttributeValue, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/request-attribute-values/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<RequestAttributeValue> getRequestAttributeValue(@PathVariable Long id) {
        log.debug("REST request to get RequestAttributeValue : {}", id);
        Optional<RequestAttributeValue> requestAttributeValue = requestAttributeValueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(requestAttributeValue);
    }

    /**
     * {@code DELETE  /request-attribute-values/:id} : delete the "id" requestAttributeValue.
     *
     * @param id the id of the requestAttributeValue to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/request-attribute-values/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteRequestAttributeValue(@PathVariable Long id) {
        log.debug("REST request to delete RequestAttributeValue : {}", id);
        requestAttributeValueService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
