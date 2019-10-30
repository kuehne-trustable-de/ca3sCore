package de.trustable.ca3sjh.web.rest;

import de.trustable.ca3sjh.domain.SelectorToTemplate;
import de.trustable.ca3sjh.service.SelectorToTemplateService;
import de.trustable.ca3sjh.web.rest.errors.BadRequestAlertException;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
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
 * REST controller for managing {@link de.trustable.ca3sjh.domain.SelectorToTemplate}.
 */
@RestController
@RequestMapping("/api")
public class SelectorToTemplateResource {

    private final Logger log = LoggerFactory.getLogger(SelectorToTemplateResource.class);

    private static final String ENTITY_NAME = "selectorToTemplate";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SelectorToTemplateService selectorToTemplateService;

    public SelectorToTemplateResource(SelectorToTemplateService selectorToTemplateService) {
        this.selectorToTemplateService = selectorToTemplateService;
    }

    /**
     * {@code POST  /selector-to-templates} : Create a new selectorToTemplate.
     *
     * @param selectorToTemplate the selectorToTemplate to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new selectorToTemplate, or with status {@code 400 (Bad Request)} if the selectorToTemplate has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/selector-to-templates")
    public ResponseEntity<SelectorToTemplate> createSelectorToTemplate(@Valid @RequestBody SelectorToTemplate selectorToTemplate) throws URISyntaxException {
        log.debug("REST request to save SelectorToTemplate : {}", selectorToTemplate);
        if (selectorToTemplate.getId() != null) {
            throw new BadRequestAlertException("A new selectorToTemplate cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SelectorToTemplate result = selectorToTemplateService.save(selectorToTemplate);
        return ResponseEntity.created(new URI("/api/selector-to-templates/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /selector-to-templates} : Updates an existing selectorToTemplate.
     *
     * @param selectorToTemplate the selectorToTemplate to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated selectorToTemplate,
     * or with status {@code 400 (Bad Request)} if the selectorToTemplate is not valid,
     * or with status {@code 500 (Internal Server Error)} if the selectorToTemplate couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/selector-to-templates")
    public ResponseEntity<SelectorToTemplate> updateSelectorToTemplate(@Valid @RequestBody SelectorToTemplate selectorToTemplate) throws URISyntaxException {
        log.debug("REST request to update SelectorToTemplate : {}", selectorToTemplate);
        if (selectorToTemplate.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        SelectorToTemplate result = selectorToTemplateService.save(selectorToTemplate);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, selectorToTemplate.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /selector-to-templates} : get all the selectorToTemplates.
     *

     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of selectorToTemplates in body.
     */
    @GetMapping("/selector-to-templates")
    public List<SelectorToTemplate> getAllSelectorToTemplates() {
        log.debug("REST request to get all SelectorToTemplates");
        return selectorToTemplateService.findAll();
    }

    /**
     * {@code GET  /selector-to-templates/:id} : get the "id" selectorToTemplate.
     *
     * @param id the id of the selectorToTemplate to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the selectorToTemplate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/selector-to-templates/{id}")
    public ResponseEntity<SelectorToTemplate> getSelectorToTemplate(@PathVariable Long id) {
        log.debug("REST request to get SelectorToTemplate : {}", id);
        Optional<SelectorToTemplate> selectorToTemplate = selectorToTemplateService.findOne(id);
        return ResponseUtil.wrapOrNotFound(selectorToTemplate);
    }

    /**
     * {@code DELETE  /selector-to-templates/:id} : delete the "id" selectorToTemplate.
     *
     * @param id the id of the selectorToTemplate to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/selector-to-templates/{id}")
    public ResponseEntity<Void> deleteSelectorToTemplate(@PathVariable Long id) {
        log.debug("REST request to delete SelectorToTemplate : {}", id);
        selectorToTemplateService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
