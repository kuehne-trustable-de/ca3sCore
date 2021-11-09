package de.trustable.ca3s.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class AuthenticationProviderWrapper implements AuthenticationProvider {

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationProviderWrapper.class);

	private final AuthenticationProvider authenticationProvider;


	public AuthenticationProviderWrapper(AuthenticationProvider authenticationProvider) {
		this.authenticationProvider = authenticationProvider;
	}

	@Override
	public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

		LOG.debug("AuthenticationProviderWrapper.authenticate got {}", authentication);
		Authentication auth = authenticationProvider.authenticate(authentication);

		LOG.debug("AuthenticationProviderWrapper.authenticate returns {}", auth);

		return auth;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		boolean isSuported = authenticationProvider.supports(authentication);

		LOG.debug("AuthenticationProviderWrapper.supports returns {} for {}", isSuported, authentication);

		return isSuported;
	}
}
