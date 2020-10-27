package de.trustable.ca3s.core.security;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class Ca3sAuthenticationProvider 
	implements AuthenticationProvider {
//	extends AbstractUserDetailsAuthenticationProvider {
	
	private final Logger LOG = LoggerFactory.getLogger(Ca3sAuthenticationProvider.class);


	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		LOG.debug("!!! authenticate({})", authentication); 

		if (authentication.getCredentials() == null) {
			LOG.debug("Authentication failed: no credentials provided");

			throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
		}

	    String name = authentication.getName();
	    String password = authentication.getCredentials().toString();

        return new UsernamePasswordAuthenticationToken(
	              name, password, new ArrayList<>());
	}

	@Override
	public boolean supports(Class<?> authentication) {
		LOG.debug("supports class {} ?", authentication.getClass().getName()); 

		return true;

	}
}
