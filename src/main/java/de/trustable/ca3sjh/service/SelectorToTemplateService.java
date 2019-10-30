package de.trustable.ca3sjh.service;

import de.trustable.ca3sjh.domain.SelectorToTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link SelectorToTemplate}.
 */
public interface SelectorToTemplateService {

    /**
     * Save a selectorToTemplate.
     *
     * @param selectorToTemplate the entity to save.
     * @return the persisted entity.
     */
    SelectorToTemplate save(SelectorToTemplate selectorToTemplate);

    /**
     * Get all the selectorToTemplates.
     *
     * @return the list of entities.
     */
    List<SelectorToTemplate> findAll();


    /**
     * Get the "id" selectorToTemplate.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<SelectorToTemplate> findOne(Long id);

    /**
     * Delete the "id" selectorToTemplate.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
