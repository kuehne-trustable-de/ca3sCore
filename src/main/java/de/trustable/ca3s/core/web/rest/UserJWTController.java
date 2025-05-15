package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.*;
import de.trustable.ca3s.core.service.dto.acme.problem.ProblemDetail;
import de.trustable.ca3s.core.service.util.AcmeUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.service.UserCredentialService;
import de.trustable.ca3s.core.web.rest.acme.AcmeController;
import de.trustable.ca3s.core.web.rest.vm.LoginData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class UserJWTController {

    private final Logger log = LoggerFactory.getLogger(UserJWTController.class);

    private final TokenProvider tokenProvider;
    private final UserService userService;
    private final UserCredentialService userCredentialService;
    private final UserUtil userUtil;

    private final TotpService totpService;
    private final SMSService smsService;
    private final ClientAuthService clientAuthService;

    public UserJWTController(TokenProvider tokenProvider, UserService userService,
                             UserCredentialService userCredentialService,
                             UserUtil userUtil, TotpService totpService,
                             SMSService smsService, ClientAuthService clientAuthService) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userCredentialService = userCredentialService;
        this.userUtil = userUtil;
        this.totpService = totpService;
        this.smsService = smsService;
        this.clientAuthService = clientAuthService;
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AuthenticationException.class, InternalAuthenticationServiceException.class, UserNotAuthenticatedException.class})
    @PostMapping("/authenticate")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginData loginData) {

        userUtil.checkIPBlocked(loginData.getUsername());

        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("--- is already authenticated: {}", authentication.isAuthenticated());
        }

        try {
            Authentication authentication = userCredentialService.validateUserPassword(loginData.getUsername(),
                loginData.getPassword());

            if( authentication == null){
                return buildProblemDetailForAuthenticationFailure(loginData, "authentication failed");
            }

            User user = userUtil.getUserByLogin(loginData.getUsername());
            if(loginData.getAuthSecondFactor() == AuthSecondFactor.TOTP ){
                if( loginData.getSecondSecret() != null &&
                    !loginData.getSecondSecret().isEmpty()){
                    totpService.checkOtpToken(user, loginData.getSecondSecret());
                }else{
                    log.warn("OTP value missing");
                    throw new BadCredentialsException("Client credentials invalid");
                }
            }else if(loginData.getAuthSecondFactor() == AuthSecondFactor.SMS ){
                if( loginData.getSecondSecret() != null &&
                    !loginData.getSecondSecret().isEmpty()){
                    smsService.checkSMS(user, loginData.getSecondSecret());
                }else{
                    log.warn("OTP value missing");
                    throw new BadCredentialsException("Client credentials invalid");
                }
            }else if(loginData.getAuthSecondFactor() == AuthSecondFactor.CLIENT_CERT ){
                if( loginData.getSecondSecret() != null &&
                    !loginData.getSecondSecret().isEmpty()){

                    String jwt = loginData.getSecondSecret();
                    if(tokenProvider.validateToken(jwt)) {
                        log.info("jwt for second factor containing related to client cert : {}", jwt);

                        if( clientAuthService.isClientCertValidForUser(jwt, user)){
                            log.debug("jwt holds ski matching user's client cert ");
                        }else{
                            log.warn("Client cert ski of JWT does not match user");
                            throw new BadCredentialsException("Client credentials invalid");
                        }
                    }else{
                        log.warn("Client cert JWT invalid");
                        throw new BadCredentialsException("Client credentials invalid");
                    }
                }else{
                    log.warn("Client cert JWT missing");
                    throw new BadCredentialsException("Client credentials invalid");
                }
            }else if(loginData.getAuthSecondFactor() == AuthSecondFactor.NONE ){
                userService.updateSecondFactorRequirement(user, true);
                if (user.isSecondFactorRequired()){
                    log.warn("Second factor missing but it is required");
                    throw new BadCredentialsException("Client credentials invalid");
                }
            }

            userUtil.handleSuccesfulAuthentication(user, loginData.getAuthSecondFactor());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = tokenProvider.createToken(authentication, loginData.isRememberMe());
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);

        } catch(AuthenticationException authenticationException){
            return buildProblemDetailForAuthenticationFailure(loginData, authenticationException.getMessage());

        } catch(Throwable th){
            log.info("login failed for user '" + loginData.getUsername() + "' with unexpected exception !", th );
            throw th;
        }
    }

    private @NotNull ResponseEntity<ProblemDetail> buildProblemDetailForAuthenticationFailure(LoginData loginData, String authenticationExceptionMsg) {
        log.info("login failed for user '{}' with reason {} !", loginData.getUsername(), authenticationExceptionMsg);
        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Authentication problem",
            HttpStatus.FORBIDDEN, authenticationExceptionMsg, AcmeUtil.NO_INSTANCE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

}
