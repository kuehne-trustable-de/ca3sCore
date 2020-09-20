package de.trustable.ca3s.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

import java.util.ArrayList;
import java.util.Collection;

public class ActiveDirectoryLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

	private static final Logger LOG = LoggerFactory.getLogger(ActiveDirectoryLdapAuthoritiesPopulator.class);

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData, String username) {
        String[] groups = userData.getStringAttributes("memberOf");

        if (groups == null) {
            LOG.debug("no group memberships found ");
            return AuthorityUtils.NO_AUTHORITIES;
        }

        LOG.debug("#{} group memberships found for '{}'", groups.length, userData.getDn().get(0));
        

        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(
                groups.length);

        for (String group : groups) {
            authorities.add(new SimpleGrantedAuthority(new DistinguishedName(group)
                    .removeLast().getValue()));
        }

        return authorities;
    }

}
