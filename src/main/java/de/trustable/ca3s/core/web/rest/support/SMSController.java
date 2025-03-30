package de.trustable.ca3s.core.web.rest.support;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.SMSService;
import de.trustable.ca3s.core.service.util.BPMNUtil;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.service.vault.UserCredentialService;
import de.trustable.ca3s.core.web.rest.errors.AccountResourceException;
import de.trustable.ca3s.core.web.rest.vm.LoginData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/publicapi")
public class SMSController {

    private final Logger LOG = LoggerFactory.getLogger(SMSController.class);

    final private CertificateUtil certificateUtil;
    private final UserCredentialService userCredentialService;

    private final UserRepository userRepository;
    final private UserUtil userUtil;
    final private BPMNUtil bpmnUtil;
    final private SMSService smsService;

    public SMSController(CertificateUtil certificateUtil, UserCredentialService userCredentialService, UserRepository userRepository, UserUtil userUtil, BPMNUtil bpmnUtil, SMSService smsService) {
        this.certificateUtil = certificateUtil;
        this.userCredentialService = userCredentialService;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
        this.bpmnUtil = bpmnUtil;
        this.smsService = smsService;
    }


    @RequestMapping(value = "/smsDelivery", method = POST)
    public ResponseEntity sendSMS() {

        String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccountResourceException("Current user login not found"));
        Optional<User> userOpt = userRepository.findOneByLogin(userLogin);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        String msg = smsService.sendPIN_SMS(userOpt.get());

        return ResponseEntity.ok(msg);
    }

    @RequestMapping(value = "/smsDelivery/{user}", method = POST)
    public ResponseEntity sendSMS(@PathVariable final String user, @RequestBody LoginData loginData) {

        userUtil.checkIPBlocked(loginData.getUsername());

        try {
            userCredentialService.validateUserPassword(loginData);

            Optional<User> userOpt = userRepository.findOneByLogin(loginData.getUsername());
            if (!userOpt.isPresent()) {
                // no leakage of user details
                LOG.warn("user '{}' unknown", loginData.getUsername());
                return ResponseEntity.noContent().build();
            }

            smsService.sendPIN_SMS(userOpt.get());
        }catch(RuntimeException rte){
            LOG.warn("invalid SMS sending request", rte);
        }
        return ResponseEntity.noContent().build();

    }
}
