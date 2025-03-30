package de.trustable.ca3s.core.service.util;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
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

    private final HttpServletRequest request;

    public UserUtil(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserRepository userRepository, AuditService auditService,
                    @Value("${ca3s.ui.login.allowEmailAddress:false}") boolean loginByEmailAddress,
                    @Value("${ca3s.ui.login.ratelimit.second:0}") int rateSec,
                    @Value("${ca3s.ui.login.ratelimit.minute:20}") int rateMin,
                    @Value("${ca3s.ui.login.ratelimit.hour:0}") int rateHour,
                    HttpServletRequest request) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.loginByEmailAddress = loginByEmailAddress;

        this.rateLimiterService = new RateLimiterService("Login", rateSec, rateMin, rateHour);

        this.request = request;
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
        if (!optCurrentUser.isPresent()) {
            String msg ="Name '"+userName+ "' not found as user";
            LOG.warn(msg);
            throw new UserNotFoundException(msg);
        }
        return optCurrentUser.get();
    }

    public boolean isAdministrativeUser() {
        return isAdministrativeUser(getCurrentUser());
    }

    public static boolean isAdministrativeUser(final User user){
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

    public void addUserDetails(CertificateView certificateView){
        if( isAdministrativeUser()){
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
        if( isAdministrativeUser()){
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
        String clientIP = getClientIP();
        try {
            rateLimiterService.checkSprayingRateLimit(getClientIPAsLong(clientIP), clientIP);
        }catch(IPBlockedException ipBlockedException){
            auditService.saveAuditTrace( auditService.createAuditTraceLoginForIPBlocked(username, clientIP));
            throw ipBlockedException;
        }
    }

    public void handleSuccesfulAuthentication(final String username) {
        handleSuccesfulAuthentication(getUserByLogin(username));
    }

    public void handleSuccesfulAuthentication(final User user) {
        String clientIP = getClientIP();

        user.setFailedLogins(0L);
        user.setLastloginDate(Instant.now());
        user.setBlockedUntilDate(null);
        userRepository.save(user);

        auditService.saveAuditTrace(auditService.createAuditTraceLoginSucceeded(null, clientIP));

    }
    public void handleBadCredentials(String username) {
        String clientIP = getClientIP();

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
            String msg = "User '"+ user.getLogin()+"' blocked";
            LOG.info(msg);
            auditService.saveAuditTrace( auditService.createAuditTraceLoginFailed(username, clientIP));
            throw new BlockedCredentialsException(msg, blockedUntilDate);
        }else{
            auditService.saveAuditTrace( auditService.createAuditTraceLoginBlocked(username, clientIP, blockedForSec));
        }
        userRepository.save(user);
    }

    private String getClientIP() {
        String addressString;
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            addressString = request.getRemoteAddr();
        }else {
            addressString = xfHeader.split(",")[0];
        }

        return addressString;
    }
    private Long getClientIPAsLong(final String addressString) {
        try {
            return new BigInteger(InetAddress.getByName(addressString).getAddress()).longValue();
        } catch (UnknownHostException e) {
            return 0L;
        }
    }

}
