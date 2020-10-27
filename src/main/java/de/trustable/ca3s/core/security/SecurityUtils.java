package de.trustable.ca3s.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import de.trustable.ca3s.core.config.SecurityConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for Spring Security.
 * 
 * 
 * https://www.baeldung.com/spring-security-kerberos-integration
 * https://www.baeldung.com/spring-security-kerberos
 * 
 */
public final class SecurityUtils {

	private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);

	// @todo check kerberos integration

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(authentication -> {
                if (authentication.getPrincipal() instanceof UserDetails) {
                    UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
                    return springSecurityUser.getUsername();
                } else if (authentication.getPrincipal() instanceof String) {
                    return (String) authentication.getPrincipal();
                } else if (authentication.getPrincipal() instanceof LdapUserDetails) {
                	LdapUserDetails ldapUser = (LdapUserDetails) authentication.getPrincipal();
                	return ldapUser.getUsername();
                } else {
                	LOG.warn("SecurityUtils getCurrentUserLogin found unsupported authentication object: " + authentication.toString());
                }
                return null;
            });
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    public static boolean isAuthenticated() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(authentication -> {
                List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.addAll(authentication.getAuthorities());
                return authorities.stream()
                    .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(AuthoritiesConstants.ANONYMOUS));
            })
            .orElse(false);
    }

    /**
     * If the current user has a specific authority (security role).
     * <p>
     * The name of this method comes from the {@code isUserInRole()} method in the Servlet API.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    public static boolean isCurrentUserInRole(String authority) {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .map(authentication -> {
                List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.addAll(authentication.getAuthorities());
                return authorities.stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
            })
            .orElse(false);
    }
}
