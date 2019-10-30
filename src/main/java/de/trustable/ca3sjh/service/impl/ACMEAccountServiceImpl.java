package de.trustable.ca3sjh.service.impl;

import de.trustable.ca3sjh.service.ACMEAccountService;
import de.trustable.ca3sjh.domain.ACMEAccount;
import de.trustable.ca3sjh.repository.ACMEAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link ACMEAccount}.
 */
@Service
@Transactional
public class ACMEAccountServiceImpl implements ACMEAccountService {

    private final Logger log = LoggerFactory.getLogger(ACMEAccountServiceImpl.class);

    private final ACMEAccountRepository aCMEAccountRepository;

    public ACMEAccountServiceImpl(ACMEAccountRepository aCMEAccountRepository) {
        this.aCMEAccountRepository = aCMEAccountRepository;
    }

    /**
     * Save a aCMEAccount.
     *
     * @param aCMEAccount the entity to save.
     * @return the persisted entity.
     */
    @Override
    public ACMEAccount save(ACMEAccount aCMEAccount) {
        log.debug("Request to save ACMEAccount : {}", aCMEAccount);
        return aCMEAccountRepository.save(aCMEAccount);
    }

    /**
     * Get all the aCMEAccounts.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<ACMEAccount> findAll() {
        log.debug("Request to get all ACMEAccounts");
        return aCMEAccountRepository.findAll();
    }


    /**
     * Get one aCMEAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<ACMEAccount> findOne(Long id) {
        log.debug("Request to get ACMEAccount : {}", id);
        return aCMEAccountRepository.findById(id);
    }

    /**
     * Delete the aCMEAccount by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ACMEAccount : {}", id);
        aCMEAccountRepository.deleteById(id);
    }
}
