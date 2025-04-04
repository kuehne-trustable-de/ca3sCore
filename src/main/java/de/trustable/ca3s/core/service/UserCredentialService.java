package de.trustable.ca3s.core.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserCredentialService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public UserCredentialService(AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    public Authentication validateUserPassword(final String username, final String password) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            username,password
        );

        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
