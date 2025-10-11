package de.trustable.ca3s.core.security.apikey;

import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.exception.BadRequestAlertException;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.ArrayList;
import java.util.List;

public class APIKeyAuthenticationManager implements AuthenticationManager {

    private final Logger log = LoggerFactory.getLogger(APIKeyAuthenticationManager.class);

    private final String apiKeyAdminValue;
    private final UserService userService;

    public APIKeyAuthenticationManager(final UserService userService, String apiKeyAdminValue) {
        this.apiKeyAdminValue = apiKeyAdminValue;
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String principal = (String) authentication.getPrincipal();

        try {
            User user = userService.authenticateUserByToken(principal);
            List<GrantedAuthority> grantedAuthorities = new ArrayList<>(user.getAuthorities().size());
            for(Authority authority : user.getAuthorities()) {
                log.debug("api token identified user {} / #{} in role {}",
                    user.getLogin(),
                    user.getId(),
                    authority);
                grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
            }
            return new PreAuthenticatedAuthenticationToken(user.getLogin(), "",
                grantedAuthorities);

        }catch( BadRequestAlertException badRequestAlertException){
            log.debug("no user-related api token found");
        }

        if(apiKeyAdminValue == null || !apiKeyAdminValue.equals(principal)) {
            throw new BadCredentialsException("The API key was not found or not the expected value.");
        }
        return new PreAuthenticatedAuthenticationToken("APIKeyUser", "",
            AuthorityUtils.createAuthorityList(AuthoritiesConstants.ADMIN));
    }

}
