package de.trustable.ca3s.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class DaoAuthenticationProviderWrapper implements AuthenticationProvider {

    private final static Logger LOG = LoggerFactory.getLogger(DaoAuthenticationProviderWrapper.class);

	private final DaoAuthenticationProvider daoAuthenticationProvider;
	
	
	public DaoAuthenticationProviderWrapper(DaoAuthenticationProvider daoAuthenticationProvider) {
		this.daoAuthenticationProvider = daoAuthenticationProvider;
	}
	
	@Override
	public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
		
		LOG.debug("DaoAuthenticationProviderWrapper.authenticate got {}", authentication);
		Authentication auth = daoAuthenticationProvider.authenticate(authentication);

		LOG.debug("DaoAuthenticationProviderWrapper.authenticate returns {}", auth);

		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		boolean isSuported = daoAuthenticationProvider.supports(authentication);
		
		LOG.debug("DaoAuthenticationProviderWrapper.supports returns {} for {}", isSuported, authentication);
		
		return isSuported;
	}
}
