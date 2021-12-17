package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.ACMEAccount;
import de.trustable.ca3s.core.service.ACMEAccountService;
import de.trustable.ca3s.core.service.dto.ACMEAccountView;
import de.trustable.ca3s.core.service.util.ACMEAccountUtil;
import io.github.jhipster.web.util.ResponseUtil;
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
 * REST controller for managing {@link ACMEAccount}.
 */
@Transactional
@RestController
@RequestMapping("/api")
public class ACMEAccountViewResource {

    private final Logger log = LoggerFactory.getLogger(ACMEAccountViewResource.class);

    private final ACMEAccountService acmeAccountService;

    private final ACMEAccountUtil acmeAccountUtil;

    public ACMEAccountViewResource(ACMEAccountService acmeAccountService, ACMEAccountUtil acmeAccountUtil) {
        this.acmeAccountService = acmeAccountService;
        this.acmeAccountUtil = acmeAccountUtil;
    }

    /**
     * {@code GET  /acmeAccountViews} : get all ACME accounts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ACME accounts in body.
     */
    @GetMapping("/acmeAccountViews")
    public List<ACMEAccountView> getAllACMEAccountViews() {
        log.debug("REST request to get all ACMEAccountViews");
        List<ACMEAccountView> avList = new ArrayList<>();
        for( ACMEAccount acmeAccount: acmeAccountService.findAll()){
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
    public ResponseEntity<ACMEAccountView> getACMEAccount(@PathVariable Long id) {
        log.debug("REST request to get ACMEAccountView : {}", id);
        Optional<ACMEAccount> acmeAccountOptional = acmeAccountService.findOne(id);
        Optional<ACMEAccountView> avOpt = Optional.empty();
        if( acmeAccountOptional.isPresent()){
            ACMEAccount acmeAccount = acmeAccountOptional.get();
            ACMEAccountView acmeAccountView = acmeAccountUtil.from(acmeAccount);
            avOpt = Optional.of(acmeAccountView);
        }
        return ResponseUtil.wrapOrNotFound(avOpt);
    }

}
