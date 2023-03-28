package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.AlgorithmRestriction;
import de.trustable.ca3s.core.repository.AlgorithmRestrictionRepository;
import de.trustable.ca3s.core.service.AlgorithmRestrictionService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link AlgorithmRestriction}.
 */
@Service
@Transactional
public class AlgorithmRestrictionServiceImpl implements AlgorithmRestrictionService {

    private final Logger log = LoggerFactory.getLogger(AlgorithmRestrictionServiceImpl.class);

    private final AlgorithmRestrictionRepository algorithmRestrictionRepository;

    public AlgorithmRestrictionServiceImpl(AlgorithmRestrictionRepository algorithmRestrictionRepository) {
        this.algorithmRestrictionRepository = algorithmRestrictionRepository;
    }

    @Override
    public AlgorithmRestriction save(AlgorithmRestriction algorithmRestriction) {
        log.debug("Request to save AlgorithmRestriction : {}", algorithmRestriction);
        return algorithmRestrictionRepository.save(algorithmRestriction);
    }

    @Override
    public Optional<AlgorithmRestriction> partialUpdate(AlgorithmRestriction algorithmRestriction) {
        log.debug("Request to partially update AlgorithmRestriction : {}", algorithmRestriction);

        return algorithmRestrictionRepository
            .findById(algorithmRestriction.getId())
            .map(existingAlgorithmRestriction -> {
                if (algorithmRestriction.getType() != null) {
                    existingAlgorithmRestriction.setType(algorithmRestriction.getType());
                }
                if (algorithmRestriction.getNotAfter() != null) {
                    existingAlgorithmRestriction.setNotAfter(algorithmRestriction.getNotAfter());
                }
                if (algorithmRestriction.getIdentifier() != null) {
                    existingAlgorithmRestriction.setIdentifier(algorithmRestriction.getIdentifier());
                }
                if (algorithmRestriction.getName() != null) {
                    existingAlgorithmRestriction.setName(algorithmRestriction.getName());
                }
                if (algorithmRestriction.getAcceptable() != null) {
                    existingAlgorithmRestriction.setAcceptable(algorithmRestriction.getAcceptable());
                }

                return existingAlgorithmRestriction;
            })
            .map(algorithmRestrictionRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlgorithmRestriction> findAll() {
        log.debug("Request to get all AlgorithmRestrictions");
        return algorithmRestrictionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AlgorithmRestriction> findOne(Long id) {
        log.debug("Request to get AlgorithmRestriction : {}", id);
        return algorithmRestrictionRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete AlgorithmRestriction : {}", id);
        algorithmRestrictionRepository.deleteById(id);
    }
}
