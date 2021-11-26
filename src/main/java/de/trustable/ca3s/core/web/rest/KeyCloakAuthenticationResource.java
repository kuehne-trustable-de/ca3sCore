package de.trustable.ca3s.core.web.rest;


import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * REST controller for managing the current user login using KeyCloak.
 */
@RestController
@RequestMapping("/keyCloak")
public class KeyCloakAuthenticationResource {

    private final Logger log = LoggerFactory.getLogger(KeyCloakAuthenticationResource.class);

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public KeyCloakAuthenticationResource(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
    }

    /**
     * {@code GET  /authenticatedUser} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticatedUser")
    public ResponseEntity<String> getAuthenticatedUser(HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        log.info("REST request to check if the current user is authenticated thru KeyCloak. User {}, Principal {}, authType {}",
            request.getRemoteUser(), principal, request.getAuthType());

        HttpHeaders httpHeaders = new HttpHeaders();

        if( principal == null) {
            log.info("User Principal == null !");
        }else{
            SecurityContext securityContext = SecurityContextHolder.getContext();
            log.info("Authentication by SecurityContext: " + securityContext.getAuthentication());

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(request.getUserPrincipal().getName(), "KeyCloakToken");

            securityContext.setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, false);
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
        }

        log.info("Bearer Token created for KeyCloak Authentication");
        return new ResponseEntity<>(request.getRemoteUser(), httpHeaders, HttpStatus.OK);
    }

}
