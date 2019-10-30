package de.trustable.ca3sjh.service.impl;

import de.trustable.ca3sjh.service.SelectorToTemplateService;
import de.trustable.ca3sjh.domain.SelectorToTemplate;
import de.trustable.ca3sjh.repository.SelectorToTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link SelectorToTemplate}.
 */
@Service
@Transactional
public class SelectorToTemplateServiceImpl implements SelectorToTemplateService {

    private final Logger log = LoggerFactory.getLogger(SelectorToTemplateServiceImpl.class);

    private final SelectorToTemplateRepository selectorToTemplateRepository;

    public SelectorToTemplateServiceImpl(SelectorToTemplateRepository selectorToTemplateRepository) {
        this.selectorToTemplateRepository = selectorToTemplateRepository;
    }

    /**
     * Save a selectorToTemplate.
     *
     * @param selectorToTemplate the entity to save.
     * @return the persisted entity.
     */
    @Override
    public SelectorToTemplate save(SelectorToTemplate selectorToTemplate) {
        log.debug("Request to save SelectorToTemplate : {}", selectorToTemplate);
        return selectorToTemplateRepository.save(selectorToTemplate);
    }

    /**
     * Get all the selectorToTemplates.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<SelectorToTemplate> findAll() {
        log.debug("Request to get all SelectorToTemplates");
        return selectorToTemplateRepository.findAll();
    }


    /**
     * Get one selectorToTemplate by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<SelectorToTemplate> findOne(Long id) {
        log.debug("Request to get SelectorToTemplate : {}", id);
        return selectorToTemplateRepository.findById(id);
    }

    /**
     * Delete the selectorToTemplate by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete SelectorToTemplate : {}", id);
        selectorToTemplateRepository.deleteById(id);
    }
}
