package de.trustable.ca3s.core.web.rest;

import de.trustable.ca3s.core.config.LDAPConfig;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.DomainUserDetailsService;
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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final LDAPCredentialService ldapCredentialService;
    private final UserUtil userUtil;
    private final UserRepository userRepository;

    private final DomainUserDetailsService domainUserDetailsService;

    private final TotpService totpService;
    private final SMSService smsService;
    private final ClientAuthService clientAuthService;
    final private PasswordEncoder passwordEncoder;

    private String domainSuffix;

    public UserJWTController(TokenProvider tokenProvider, UserService userService,
                             UserCredentialService userCredentialService, LDAPCredentialService ldapCredentialService,
                             UserUtil userUtil, UserRepository userRepository,
                             DomainUserDetailsService domainUserDetailsService,
                             TotpService totpService,
                             SMSService smsService, ClientAuthService clientAuthService, PasswordEncoder passwordEncoder,
                             LDAPConfig ldapConfig) {
        this.tokenProvider = tokenProvider;
        this.userService = userService;
        this.userCredentialService = userCredentialService;
        this.ldapCredentialService = ldapCredentialService;
        this.userUtil = userUtil;
        this.userRepository = userRepository;
        this.domainUserDetailsService = domainUserDetailsService;
        this.totpService = totpService;
        this.smsService = smsService;
        this.clientAuthService = clientAuthService;
        this.passwordEncoder = passwordEncoder;

        if (ldapConfig.getAdDomain() != null) {
            domainSuffix = "@" + ldapConfig.getAdDomain().toLowerCase();
        }
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AuthenticationException.class, InternalAuthenticationServiceException.class, UserNotAuthenticatedException.class})
    @PostMapping("/authenticate")
    public ResponseEntity<?> authorize(@Valid @RequestBody LoginData loginData) {

        userUtil.checkIPBlocked(loginData.getUsername());

        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("--- is already authenticated: {} for {}", authentication.isAuthenticated(), authentication.getName());
        }

        try {
            Authentication authentication = userCredentialService.validateUserPassword(loginData.getUsername(),
                loginData.getPassword());

            if (authentication == null) {
                return buildProblemDetailForAuthenticationFailure(loginData, "authentication failed");
            }

            User user = handleCa3sInternalUser(loginData);
            userUtil.handleSuccesfulAuthentication(user, loginData.getAuthSecondFactor());

            return getJwtTokenResponseEntity(authentication);

        } catch (AuthenticationException authenticationException) {
            return buildProblemDetailForAuthenticationFailure(loginData, authenticationException.getMessage());

        } catch (Throwable th) {
            log.info("login failed for user '{}' with unexpected exception !", loginData.getUsername(), th);
            throw th;
        }
    }

    @Transactional(noRollbackFor = {BadCredentialsException.class, AuthenticationException.class, InternalAuthenticationServiceException.class, UserNotAuthenticatedException.class})
    @PostMapping("/authenticateLDAP")
    public ResponseEntity<?> authorizeLDAP(@Valid @RequestBody LoginData loginData) {

        userUtil.checkIPBlocked(loginData.getUsername());

        {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.info("--- is already authenticated: {} for {}", authentication.isAuthenticated(), authentication.getName());
        }

        try {
            if(!loginData.getUsername().contains("\\")){
                return buildProblemDetailForAuthenticationFailure(loginData,
                    "unexpected username structure. A single backslash expected.");
            }

            String[] nameParts = loginData.getUsername().split("\\\\");
            String domainPrefix = nameParts[0];
            String sAMAccountName = nameParts[1];
            String atStyleName = sAMAccountName + "@" + domainPrefix;
            log.info("processing LDAP-managed user '{}', sAMAccountName  '{}'", atStyleName, sAMAccountName);

            boolean isCredentialsValid = ldapCredentialService.checkUserPasswordWithLDAP(sAMAccountName,
                atStyleName,
                loginData.getPassword());

            if (!isCredentialsValid) {
                return buildProblemDetailForAuthenticationFailure(loginData, "ldap authentication failed");
            }
            UserDetails userDetails = domainUserDetailsService.handleAuthenticatedUser(atStyleName, sAMAccountName);

            AnonymousAuthenticationToken authentication = new AnonymousAuthenticationToken(atStyleName, atStyleName, userDetails.getAuthorities());

            return getJwtTokenResponseEntity(authentication);

        } catch (AuthenticationException authenticationException) {
            return buildProblemDetailForAuthenticationFailure(loginData, authenticationException.getMessage());

        } catch (Throwable th) {
            log.info("login failed for user '{}' with unexpected exception !", loginData.getUsername(), th);
            throw th;
        }
    }

    private @NotNull ResponseEntity<JWTToken> getJwtTokenResponseEntity(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.createToken(authentication, false);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
    }

    private User handleCa3sInternalUser(final LoginData loginData){
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
                log.warn("SMS value missing");
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
        return user;
    }

    private @NotNull ResponseEntity<ProblemDetail> buildProblemDetailForAuthenticationFailure(LoginData loginData, String authenticationExceptionMsg) {
        log.info("login failed for user '{}' with reason {} !", loginData.getUsername(), authenticationExceptionMsg);
        final ProblemDetail problem = new ProblemDetail(AcmeUtil.MALFORMED, "Authentication problem",
            HttpStatus.FORBIDDEN, authenticationExceptionMsg, AcmeUtil.NO_INSTANCE);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).contentType(AcmeController.APPLICATION_PROBLEM_JSON).body(problem);
    }

}
