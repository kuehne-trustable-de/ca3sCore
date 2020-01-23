package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.ImportedURLService;
import de.trustable.ca3s.core.domain.ImportedURL;
import de.trustable.ca3s.core.repository.ImportedURLRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link ImportedURL}.
 */
@Service
@Transactional
public class ImportedURLServiceImpl implements ImportedURLService {

    private final Logger log = LoggerFactory.getLogger(ImportedURLServiceImpl.class);

    private final ImportedURLRepository importedURLRepository;

    public ImportedURLServiceImpl(ImportedURLRepository importedURLRepository) {
        this.importedURLRepository = importedURLRepository;
    }

    /**
     * Save a importedURL.
     *
     * @param importedURL the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ImportedURL save(ImportedURL importedURL) {
        log.debug("Request to save ImportedURL : {}", importedURL);
        return importedURLRepository.save(importedURL);
    }

    /**
     * Get all the importedURLS.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ImportedURL> findAll() {
        log.debug("Request to get all ImportedURLS");
        return importedURLRepository.findAll();
    }


    /**
     * Get one importedURL by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ImportedURL> findOne(Long id) {
        log.debug("Request to get ImportedURL : {}", id);
        return importedURLRepository.findById(id);
    }

    /**
     * Delete the importedURL by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ImportedURL : {}", id);
        importedURLRepository.deleteById(id);
    }
}
