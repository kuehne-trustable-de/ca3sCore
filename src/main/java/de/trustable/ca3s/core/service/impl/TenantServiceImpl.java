package de.trustable.ca3s.core.service.impl;

import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.service.TenantService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Tenant}.
 */
@Service
@Transactional
public class TenantServiceImpl implements TenantService {

    private final Logger log = LoggerFactory.getLogger(TenantServiceImpl.class);

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant save(Tenant tenant) {
        log.debug("Request to save Tenant : {}", tenant);
        return tenantRepository.save(tenant);
    }

    @Override
    public Tenant update(Tenant tenant) {
        log.debug("Request to update Tenant : {}", tenant);
        return tenantRepository.save(tenant);
    }

    @Override
    public Optional<Tenant> partialUpdate(Tenant tenant) {
        log.debug("Request to partially update Tenant : {}", tenant);

        return tenantRepository
            .findById(tenant.getId())
            .map(existingTenant -> {
                if (tenant.getName() != null) {
                    existingTenant.setName(tenant.getName());
                }
                if (tenant.getLongname() != null) {
                    existingTenant.setLongname(tenant.getLongname());
                }
                if (tenant.getActive() != null) {
                    existingTenant.setActive(tenant.getActive());
                }

                return existingTenant;
            })
            .map(tenantRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tenant> findAll() {
        log.debug("Request to get all Tenants");
        return tenantRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Tenant> findOne(Long id) {
        log.debug("Request to get Tenant : {}", id);
        return tenantRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Tenant : {}", id);
        tenantRepository.deleteById(id);
    }
}
