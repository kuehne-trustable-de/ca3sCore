package de.trustable.ca3s.core.web.rest;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.trustable.ca3s.core.domain.RequestAttribute;
import de.trustable.ca3s.core.service.RequestAttributeService;
import de.trustable.ca3s.core.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link de.trustable.ca3s.core.domain.RequestAttribute}.
 */
@RestController
@RequestMapping("/api")
public class RequestAttributeResource {

    private final Logger log = LoggerFactory.getLogger(RequestAttributeResource.class);

    private static final String ENTITY_NAME = "requestAttribute";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RequestAttributeService requestAttributeService;

    public RequestAttributeResource(RequestAttributeService requestAttributeService) {
        this.requestAttributeService = requestAttributeService;
    }

    /**
     * {@code POST  /request-attributes} : Create a new requestAttribute.
     *
     * @param requestAttribute the requestAttribute to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new requestAttribute, or with status {@code 400 (Bad Request)} if the requestAttribute has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/request-attributes")
    public ResponseEntity<RequestAttribute> createRequestAttribute(@Valid @RequestBody RequestAttribute requestAttribute) throws URISyntaxException {
        log.debug("REST request to save RequestAttribute : {}", requestAttribute);
        if (requestAttribute.getId() != null) {
            throw new BadRequestAlertException("A new requestAttribute cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RequestAttribute result = requestAttributeService.save(requestAttribute);
        return ResponseEntity.created(new URI("/api/request-attributes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /request-attributes} : Updates an existing requestAttribute.
     *
     * @param requestAttribute the requestAttribute to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated requestAttribute,
     * or with status {@code 400 (Bad Request)} if the requestAttribute is not valid,
     * or with status {@code 500 (Internal Server Error)} if the requestAttribute couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/request-attributes")
    public ResponseEntity<RequestAttribute> updateRequestAttribute(@Valid @RequestBody RequestAttribute requestAttribute) throws URISyntaxException {
        log.debug("REST request to update RequestAttribute : {}", requestAttribute);
        if (requestAttribute.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        RequestAttribute result = requestAttributeService.save(requestAttribute);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, requestAttribute.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /request-attributes} : get all the requestAttributes.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of requestAttributes in body.
     */
    @GetMapping("/request-attributes")
    public List<RequestAttribute> getAllRequestAttributes() {
        log.debug("REST request to get all RequestAttributes");
        return requestAttributeService.findAll();
    }

    /**
     * {@code GET  /request-attributes/:id} : get the "id" requestAttribute.
     *
     * @param id the id of the requestAttribute to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the requestAttribute, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/request-attributes/{id}")
    public ResponseEntity<RequestAttribute> getRequestAttribute(@PathVariable Long id) {
        log.debug("REST request to get RequestAttribute : {}", id);
        Optional<RequestAttribute> requestAttribute = requestAttributeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(requestAttribute);
    }

    /**
     * {@code DELETE  /request-attributes/:id} : delete the "id" requestAttribute.
     *
     * @param id the id of the requestAttribute to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/request-attributes/{id}")
    public ResponseEntity<Void> deleteRequestAttribute(@PathVariable Long id) {
        log.debug("REST request to delete RequestAttribute : {}", id);
        requestAttributeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
