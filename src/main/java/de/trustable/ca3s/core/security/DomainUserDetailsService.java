package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.config.LDAPConfig;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.AuthSecondFactor;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.exception.BlockedCredentialsException;
import de.trustable.ca3s.core.service.mapper.LDAPUserProviderMapping;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserUtil userUtil;
    private final UserRepository userRepository;
    private final LDAPUserProviderMapping ldapUserProviderMapping;

    private String domainSuffix;

    public DomainUserDetailsService(UserUtil userUtil, UserRepository userRepository, LDAPConfig ldapConfig, LDAPUserProviderMapping ldapUserProviderMapping) {
        this.userUtil = userUtil;
        this.userRepository = userRepository;
        this.ldapUserProviderMapping = ldapUserProviderMapping;

        if (ldapConfig.getAdDomain() != null) {
            domainSuffix = "@" + ldapConfig.getAdDomain().toLowerCase();
        }
    }

    @Override
    @Transactional(noRollbackFor = UserNotAuthenticatedException.class)
    public UserDetails loadUserByUsername(final String login) {
        log.debug("----------- Authenticating by username '{}'", login);

        if (login.toLowerCase().endsWith(domainSuffix)) {
            String username = login.split("@")[0];

            log.info("returning static 'user' role for kerberos-authenticated user '{}' ! Implement AD / LDAP access !!",
                username);

            try {
                handleUserAccount(username);
                return new org.springframework.security.core.userdetails.User(username,
                    "KerberosToken",
                    AuthorityUtils.createAuthorityList(AuthoritiesConstants.USER));
            } catch (NamingException e) {
                log.warn("Problem accessing LDAP", e);
                throw new UserNotAuthenticatedException(e.getMessage());
            }
        } else {
            return createSpringSecurityUser(login, userUtil.getUserByLogin(login));
        }
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {

        log.info("user {}, isActive {}, failed logins {}, blocked until {}, credentials valid until {}",
            login, user.isActivated(), user.getFailedLogins(), user.getBlockedUntilDate(), user.getCredentialsValidToDate());

        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + login + " was not activated");
        }

        Instant now = Instant.now();
        if (user.getBlockedUntilDate() != null &&
            user.getBlockedUntilDate().isAfter(now)) {
            userUtil.handleBadCredentials(login, AuthSecondFactor.NONE);

            throw new BlockedCredentialsException("User '" + login + "' blocked until " + user.getBlockedUntilDate());
        }

        if (user.getCredentialsValidToDate() != null &&
            user.getCredentialsValidToDate().isBefore(now)) {
            throw new UserCredentialsExpiredException("User " + login + " credentials expired since " + user.getCredentialsValidToDate());
        }

        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getLogin(),
            user.getPassword(),
            grantedAuthorities);
    }

    private void handleUserAccount(String username) throws NamingException {

        Optional<User> userOpt = userRepository.findOneByLogin(username);
        if(userOpt.isPresent()){
            ldapUserProviderMapping.updateUserFromLDAP(userOpt.get());
            log.info("updated known user {}", userOpt.get().getId());
        }else{

            User user = new User();
            user.setLogin(username);
            user.setPassword("$0$0$0000000000000000000000000000000000000000000000000000000");
            user.setActivated(true);
            user.setManagedExternally(true);

//            user.setLangKey(languages.alignLanguage("en"));

            ldapUserProviderMapping.updateUserFromLDAP(user);

            log.info("created new user {}", user.getId());
        }
    }
}
