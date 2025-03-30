package de.trustable.ca3s.core.service.vault;

import de.trustable.ca3s.core.web.rest.JWTToken;
import de.trustable.ca3s.core.web.rest.vm.LoginData;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Service
public class UserCredentialService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public UserCredentialService(AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    public Authentication validateUserPassword(@Valid @RequestBody LoginData loginData) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
            loginData.getUsername(),
            loginData.getPassword()
        );

        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }
}
