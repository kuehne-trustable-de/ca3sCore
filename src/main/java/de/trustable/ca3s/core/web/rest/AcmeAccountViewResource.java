package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.service.AcmeAccountService;
import de.trustable.ca3s.core.service.dto.AcmeAccountView;
import de.trustable.ca3s.core.service.util.AcmeAccountUtil;
import tech.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link AcmeAccount}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class AcmeAccountViewResource {

    private final Logger log = LoggerFactory.getLogger(AcmeAccountViewResource.class);

    private final AcmeAccountService acmeAccountService;

    private final AcmeAccountUtil acmeAccountUtil;

    public AcmeAccountViewResource(AcmeAccountService acmeAccountService, AcmeAccountUtil acmeAccountUtil) {
        this.acmeAccountService = acmeAccountService;
        this.acmeAccountUtil = acmeAccountUtil;
    }

    /**
     * {@code GET  /acmeAccountViews} : get all ACME accounts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME accounts in body.
     */
    @GetMapping("/acmeAccountViews")
    public List<AcmeAccountView> getAllAcmeAccountViews() {
        log.debug("REST request to get all AcmeAccountViews");
        List<AcmeAccountView> avList = new ArrayList<>();
        for( AcmeAccount acmeAccount: acmeAccountService.findAll()){
            avList.add(acmeAccountUtil.from(acmeAccount));
        }
        return avList;
    }

    /**
     * {@code GET  /acmeAccountViews/:id} : get the "id" ACME account.
     *
     * @param id the id of the ACME account to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ACME account, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/acmeAccountViews/{id}")
    public ResponseEntity<AcmeAccountView> getAcmeAccount(@PathVariable Long id) {
        log.debug("REST request to get AcmeAccountView : {}", id);
        Optional<AcmeAccount> acmeAccountOptional = acmeAccountService.findOne(id);
        Optional<AcmeAccountView> avOpt = Optional.empty();
        if( acmeAccountOptional.isPresent()){
            AcmeAccount acmeAccount = acmeAccountOptional.get();
            AcmeAccountView acmeAccountView = acmeAccountUtil.from(acmeAccount);
            avOpt = Optional.of(acmeAccountView);
        }
        return ResponseUtil.wrapOrNotFound(avOpt);
    }

}
