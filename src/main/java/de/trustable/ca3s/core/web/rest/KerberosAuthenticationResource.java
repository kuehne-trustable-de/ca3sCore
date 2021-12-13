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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/kerberos")
public class KerberosAuthenticationResource {

    private final Logger log = LoggerFactory.getLogger(KerberosAuthenticationResource.class);

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    public KerberosAuthenticationResource(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder) {
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

        if( request.getUserPrincipal() == null){
            // not authenticated, yet
            log.info("Not authenticated");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        log.info("REST request to check if the current user is authenticated. User {}, Principal {}, authType {}",
            request.getRemoteUser(), request.getUserPrincipal().getName(), request.getAuthType());

        SecurityContext securityContext = SecurityContextHolder.getContext();
        log.info("Authentication by SecurityContext: " + securityContext.getAuthentication());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(request.getUserPrincipal().getName(), "KerberosToken");

        securityContext.setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);

        log.info("Bearer Token created for Kerberos Authentication");
        return new ResponseEntity<>(request.getRemoteUser(), httpHeaders, HttpStatus.OK);
    }

}
