package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.RandomUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import de.trustable.ca3s.core.web.rest.vm.LoginData;
import de.trustable.ca3s.core.web.rest.vm.TokenRequest;
import de.trustable.ca3s.core.web.rest.vm.TokenResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class ApiTokenController {

    private final Logger log = LoggerFactory.getLogger(ApiTokenController.class);

    private final RandomUtil randomUtil;
    private final UserUtil userUtil;
    private final String eabKidPrefix;

    public ApiTokenController(RandomUtil randomUtil,
                              UserUtil userUtil,
                              @Value("${ca3s.acme.account.eabKidPrefix:ca3s}") String eabKidPrefix) {
        this.randomUtil = randomUtil;
        this.userUtil = userUtil;
        this.eabKidPrefix = eabKidPrefix;
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AuthenticationException.class, InternalAuthenticationServiceException.class, UserNotAuthenticatedException.class})
    @PostMapping("/token/apiToken")
    public ResponseEntity<?> getToken(@Valid @RequestBody TokenRequest tokenRequest) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userUtil.getUserByLogin(authentication.getName());
        TokenResponse tokenResponse = new TokenResponse();

        switch( tokenRequest.getCredentialType()){
            case API_TOKEN:
            case SCEP_TOKEN:
            case EST_TOKEN:
                tokenResponse.setTokenValue(randomUtil.generateApiToken() + "." + tokenRequest.getCredentialType() + "." + user.getId());
                break;
            case EAB_PASSWORD:
                tokenResponse.setTokenValue(randomUtil.generateMacKey());
                tokenResponse.setEabKid(eabKidPrefix + ":" + user.getLogin());
                break;
            default:
                String msg = String.format("unexpected type '%s' for token request!", tokenRequest.getCredentialType());
                throw new BadRequestAlertException(msg, "token", "400");
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        return new ResponseEntity<>(tokenResponse, httpHeaders, HttpStatus.OK);

    }

    private @NotNull ResponseEntity<ProblemDetail> buildProblemDetailForAuthenticationFailure(LoginData loginData, String authenticationExceptionMsg) {
        log.info("login failed for user '{}' with reason {} !", loginData.getUsername(), authenticationExceptionMsg);
        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Authentication problem",
            HttpStatus.FORBIDDEN, authenticationExceptionMsg, AcmeUtil.NO_INSTANCE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

}
