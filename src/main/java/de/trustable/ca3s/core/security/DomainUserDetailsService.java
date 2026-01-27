package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.config.LDAPConfig;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.UserNotAuthenticatedException;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.mapper.LDAPUserProviderMapping;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.NamingException;
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

    public DomainUserDetailsService(UserUtil userUtil,
                                    UserRepository userRepository,
                                    LDAPConfig ldapConfig,
                                    LDAPUserProviderMapping ldapUserProviderMapping) {
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
            String sAMAccountName = login.split("@")[0];
            String username = domainSuffix + "\\" + sAMAccountName;
            return handleAuthenticatedUser(username, sAMAccountName);
        } else {
            return createSpringSecurityUser(login, userUtil.getUserByLogin(login));
        }
    }

    public org.springframework.security.core.userdetails.User handleAuthenticatedUser(String username, String sAMAccountName) {

        log.info("processing kerberos-authenticated user '{}' ", username);
        try {
            User user = enrichUserAccount(username, sAMAccountName);
            List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(user);

            return new org.springframework.security.core.userdetails.User(username,
                RandomStringUtils.random(16),
                grantedAuthorities);
        } catch (NamingException e) {
            log.warn("Problem accessing LDAP", e);
            throw new UserNotAuthenticatedException(e.getMessage());
        }
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {

        log.info("user {}, isActive {}, failed logins {}, blocked until {}, credentials valid until {}",
            login, user.isActivated(), user.getFailedLogins(), user.getBlockedUntilDate(), user.getCredentialsValidToDate());

        userUtil.preCheckUser(user);

        List<GrantedAuthority> grantedAuthorities = getGrantedAuthorities(user);

        return new org.springframework.security.core.userdetails.User(user.getLogin(),
            user.getPassword(),
            grantedAuthorities);
    }

    private static @NotNull List<GrantedAuthority> getGrantedAuthorities(User user) {
        List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
            .collect(Collectors.toList());
        return grantedAuthorities;
    }

    private User enrichUserAccount(String username, String sAMAccountName) throws NamingException {

        Optional<User> userOpt = userRepository.findOneByLogin(username);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            ldapUserProviderMapping.updateUserFromLDAP(user, sAMAccountName);
            userRepository.save(user);
            log.info("updated known user {}", user.getId());
            return userOpt.get();
        }else{

            User user = new User();
            user.setLogin(username);
            user.setPassword("$0$0$0000000000000000000000000000000000000000000000000000000");
            user.setActivated(true);
            user.setManagedExternally(true);

//            user.setLangKey(languages.alignLanguage("en"));

            ldapUserProviderMapping.updateUserFromLDAP(user, sAMAccountName);
            log.info("created new user {}", user.getId());
            userRepository.save(user);
            return user;
        }
    }
}
