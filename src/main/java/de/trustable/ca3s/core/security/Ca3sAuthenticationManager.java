package de.trustable.ca3s.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

// @Component
public class Ca3sAuthenticationManager implements AuthenticationManager {

    private final static Logger LOG = LoggerFactory.getLogger(Ca3sAuthenticationManager.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        LOG.debug("authentication of type {}", authentication.getClass().getName());
        LOG.debug("principal {}", authentication.getPrincipal().toString());
        LOG.debug("credential {}", authentication.getCredentials().toString());

        try
        {

            if( true )
            {
                //
                // Need to create a new local user if this is the  first time logging in; this
                // is required so they can be issued JWTs. We can use this flow to also keep
                // our local use entry up to date with data from the remote service if needed
                // (for example, if the first and last name might change, this is where we would
                // update the local user entry)
                //

                return new UsernamePasswordAuthenticationToken( authentication.getPrincipal().toString(),
                		authentication.getCredentials().toString(),
                		authentication.getAuthorities());

            }
            else
            {
                throw new BadCredentialsException("Invalid username or password");
            }
        }
        catch (Exception e)
        {
            LOG.warn("Failed to authenticate", e);
            throw new AuthenticationServiceException("Failed to login", e);
        }
    }
}
