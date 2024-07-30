package de.trustable.ca3s.core.security;

import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(DomainUserDetailsService.class);

    private final UserRepository userRepository;

    private final UserUtil userUtil;

    public DomainUserDetailsService(UserRepository userRepository, UserUtil userUtil) {
        this.userRepository = userRepository;
        this.userUtil = userUtil;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("----------- Authenticating {}", login);

        if(login.startsWith("Kerberos@@")){
            String username = login.substring(10);
            return new org.springframework.security.core.userdetails.User( username,
                "KerberosToken",
                AuthorityUtils.createAuthorityList(AuthoritiesConstants.USER));

        }else {
            return createSpringSecurityUser(login,userUtil.getUserByLogin(login));
        }
    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String login, User user) {

        log.info("user {}, isActive {}, failed logins {}, blocked until {}, credentials valid until {}",
            login, user.isActivated(),user.getFailedLogins(), user.getBlockedUntilDate(), user.getCredentialsValidToDate());

        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + login + " was not activated");
        }

        Instant now = Instant.now();
        if(user.getBlockedUntilDate() != null &&
            user.getBlockedUntilDate().isAfter(now)) {
            throw new UserBlockedException("User " + login + " blocked until " + user.getBlockedUntilDate());
        }

        if(user.getCredentialsValidToDate() != null &&
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
}
