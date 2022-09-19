package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.service.AcmeAccountService;
import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link AcmeAccount}.
 */
@Service
@Transactional
public class AcmeAccountServiceImpl implements AcmeAccountService {

    private final Logger log = LoggerFactory.getLogger(AcmeAccountServiceImpl.class);

    private final AcmeAccountRepository aCMEAccountRepository;

    public AcmeAccountServiceImpl(AcmeAccountRepository aCMEAccountRepository) {
        this.aCMEAccountRepository = aCMEAccountRepository;
    }

    /**
     * Save a aCMEAccount.
     *
     * @param aCMEAccount the entity to save.
     * @return the persisted entity.
     */
    @Override
    public AcmeAccount save(AcmeAccount aCMEAccount) {
        log.debug("Request to save AcmeAccount : {}", aCMEAccount);
        return aCMEAccountRepository.save(aCMEAccount);
    }

    /**
     * Get all the aCMEAccounts.
     *
     * @return the list of entities.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AcmeAccount> findAll() {
        log.debug("Request to get all AcmeAccounts");
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
    public Optional<AcmeAccount> findOne(Long id) {
        log.debug("Request to get AcmeAccount : {}", id);
        return aCMEAccountRepository.findById(id);
    }

    /**
     * Delete the aCMEAccount by id.
     *
     * @param id the id of the entity.
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AcmeAccount : {}", id);
        aCMEAccountRepository.deleteById(id);
    }
}
