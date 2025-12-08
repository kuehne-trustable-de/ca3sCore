package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.exception.UserNotFoundException;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.IPBlockedException;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.AuditService;
import de.trustable.ca3s.core.service.dto.CSRView;
import de.trustable.ca3s.core.service.dto.CertificateView;
import de.trustable.ca3s.core.service.dto.UserLoginData;
import de.trustable.ca3s.core.service.exception.BlockedCredentialsException;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;
@Service
public class UserUtil {

    private final Logger LOG = LoggerFactory.getLogger(UserUtil.class);

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final UserRepository userRepository;
    private final AuditService auditService;

    private final boolean loginByEmailAddress;

    private final RateLimiterService rateLimiterService;

    private String eabKidPrefix;

    private final PasswordEncoder passwordEncoder;

    private final RequestUtil requestUtil;

    public UserUtil(TokenProvider tokenProvider,
                    AuthenticationManagerBuilder authenticationManagerBuilder,
                    UserRepository userRepository,
                    AuditService auditService,
                    @Value("${ca3s.ui.login.allowEmailAddress:false}") boolean loginByEmailAddress,
                    @Value("${ca3s.ui.login.ratelimit.second:0}") int rateSec,
                    @Value("${ca3s.ui.login.ratelimit.minute:20}") int rateMin,
                    @Value("${ca3s.ui.login.ratelimit.hour:0}") int rateHour,
                    @Value("${ca3s.acme.account.eabKidPrefix:ca3s}") String eabKidPrefix,
                    @Lazy PasswordEncoder passwordEncoder,
                    RequestUtil requestUtil) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.loginByEmailAddress = loginByEmailAddress;
        this.eabKidPrefix = eabKidPrefix;
        this.passwordEncoder = passwordEncoder;
        this.requestUtil = requestUtil;

        this.rateLimiterService = new RateLimiterService("Login", rateSec, rateMin, rateHour);
    }

    public User getCurrentUser() {

        LOG.debug("getCurrentUser of a web session");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if( auth == null) {
            String msg = "auth == null!";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }
        String userName = auth.getName();
        if( userName == null) {
            String msg = "Current user == null!";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }

        Optional<User> optCurrentUser = userRepository.findOneByLogin(userName);
        if (optCurrentUser.isEmpty()) {
            String msg ="Name '"+userName+ "' not found as user";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }
        return optCurrentUser.get();
    }

    public boolean isRaRoleUser() {
        return isRaRoleUser(getCurrentUser());
    }

    public boolean isAdministrativeUser() {
        return isAdministrativeUser(getCurrentUser());
    }

    public static boolean isRaRoleUser(final User user){
        for( Authority authority: user.getAuthorities()){
            String authorityName = authority.getName();
            if( authorityName.equals(AuthoritiesConstants.ADMIN) ||
                authorityName.equals(AuthoritiesConstants.RA_OFFICER) ||
                authorityName.equals(AuthoritiesConstants.DOMAIN_RA_OFFICER) ) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAdministrativeUser(final User user){
        for( Authority authority: user.getAuthorities()){
            String authorityName = authority.getName();
            if( authorityName.equals(AuthoritiesConstants.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    public void addUserDetails(CertificateView certificateView){
        if( isRaRoleUser()){
            Optional<User> optionalUser = userRepository.findOneByLogin(certificateView.getRequestedBy());
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                certificateView.setFirstName(user.getFirstName());
                certificateView.setLastName(user.getLastName());
                certificateView.setEmail(user.getEmail());
            }
        }
    }
    public void addUserDetails(CSRView csrView){
        if( isRaRoleUser()){
            Optional<User> optionalUser = userRepository.findOneByLogin(csrView.getRequestedBy());
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                csrView.setFirstName(user.getFirstName());
                csrView.setLastName(user.getLastName());
                csrView.setEmail(user.getEmail());
            }
        }
    }
    public User getUserByLogin( final String login){
        if( login == null){
            throw new UserNotFoundException();
        }
        Optional<User> optionalUser;
        String currentLogin = login;
        if ( loginByEmailAddress && new EmailValidator().isValid(currentLogin, null)) {
            optionalUser = userRepository.findOneWithAuthoritiesByEmailIgnoreCase(login);
        }else {
            currentLogin = login.toLowerCase(Locale.ENGLISH);
            optionalUser = userRepository.findOneWithAuthoritiesByLogin(currentLogin);
        }

        if( optionalUser.isEmpty()){
            throw new UserNotAuthenticatedException("User " + currentLogin + " not authenticated");
        }else{
            return optionalUser.get();
        }
    }

    public void updateUserByLogin( final String login, final String password, final String email) {
        Optional<User> optionalUser = userRepository.findOneByLogin(login);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            user.setEmail(email);
            userRepository.save(user);
        }
    }

    public String validateCredentials( UserLoginData userLoginData) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            userLoginData.getLogin(),
            userLoginData.getPassword()
        );

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        handleSuccesfulAuthentication(userLoginData.getLogin());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return tokenProvider.createToken(authentication, userLoginData.isRememberMe());
    }

    public void checkIPBlocked(String username) {
        String clientIP = requestUtil.getClientIP();
        try {
            rateLimiterService.checkSprayingRateLimit(getClientIPAsLong(clientIP), clientIP);
        }catch(IPBlockedException ipBlockedException){
            auditService.saveAuditTrace( auditService.createAuditTraceLoginForIPBlocked(username, clientIP));
            throw ipBlockedException;
        }
    }

    public void handleSuccesfulAuthentication(final String username) {
        handleSuccesfulAuthentication(getUserByLogin(username), AuthSecondFactor.NONE);
    }

    public void handleSuccesfulAuthentication(final User user, final AuthSecondFactor authSecondFactor) {
        String clientIP = requestUtil.getClientIP();

        user.setFailedLogins(0L);
        user.setLastloginDate(Instant.now());
        user.setBlockedUntilDate(null);
        userRepository.save(user);

        auditService.saveAuditTrace(auditService.createAuditTraceLoginSucceeded( user,
            authSecondFactor == null ? "":authSecondFactor.toString(),
            clientIP));

    }
    public void handleBadCredentials(String username, final AuthSecondFactor authSecondFactor) {
        String clientIP = requestUtil.getClientIP();

        try {
            rateLimiterService.consumeSprayingRateLimit(getClientIPAsLong(clientIP), clientIP);
        }catch(IPBlockedException ipBlockedException){
            auditService.saveAuditTrace( auditService.createAuditTraceLoginForIPBlocked(username, clientIP));
            throw ipBlockedException;
        }

        User user = getUserByLogin(username);
        Long failedLogins = user.getFailedLogins() + 1;
        int blockedForSec = 600;
        LOG.warn("User {} failed login count incremented to {}.", user.getLogin(), failedLogins);
        user.setFailedLogins(failedLogins);
        if( failedLogins > 5){
            Instant blockedUntilDate = Instant.now().plus(blockedForSec, ChronoUnit.SECONDS);
            user.setBlockedUntilDate( blockedUntilDate);
            auditService.saveAuditTrace( auditService.createAuditTraceLoginBlocked(username, clientIP, blockedForSec));
            String msg = "User '"+ user.getLogin()+"' blocked";
            LOG.info(msg);
            throw new BlockedCredentialsException(msg, blockedUntilDate);
        }else{
            auditService.saveAuditTrace( auditService.createAuditTraceLoginFailed(username, clientIP));
        }
        userRepository.save(user);
    }


    private Long getClientIPAsLong(final String addressString) {
        try {
            return new BigInteger(InetAddress.getByName(addressString).getAddress()).longValue();
        } catch (UnknownHostException e) {
            return 0L;
        }
    }

    public String getLoginFromCa3sKeyId( final String kid){

        String[] kidParts = kid.split(":");
        if(kidParts.length != 3){
            auditService.saveAuditTrace( auditService.createAuditTraceAcmeEABInvalid(kid));
            return null;
        }
        if( !kidParts[0].equals(eabKidPrefix)){
            auditService.saveAuditTrace( auditService.createAuditTraceAcmeEABUnexpectedPrefix(kidParts[0]));
            LOG.warn("EAB KID prefix '{}' invalid", kidParts[0]);
            return null;
        }
        try{
            Long.parseLong( kidParts[2]);
        } catch( NumberFormatException nfe){
            auditService.saveAuditTrace( auditService.createAuditTraceAcmeEABUnexpectedKidId(kidParts[2]));
            LOG.warn("Unexpected kid id : '{}' ", kidParts[2]);
            return null;
        }
        return kidParts[1];
    }
}
