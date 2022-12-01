package de.trustable.ca3s.core.security.apikey;

import de.trustable.ca3s.core.security.AuthoritiesConstants;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class APIKeyAuthenticationManager implements AuthenticationManager {

    private final String apiKeyAdminValue;

    public APIKeyAuthenticationManager(String apiKeyAdminValue) {
        this.apiKeyAdminValue = apiKeyAdminValue;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String principal = (String) authentication.getPrincipal();
        if(apiKeyAdminValue == null || !apiKeyAdminValue.equals(principal)) {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        }
        return new PreAuthenticatedAuthenticationToken("APIKeyUser", "",
            AuthorityUtils.createAuthorityList(AuthoritiesConstants.ADMIN));
    }

}
