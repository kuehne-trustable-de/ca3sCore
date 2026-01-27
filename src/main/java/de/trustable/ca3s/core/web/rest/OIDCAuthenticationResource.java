package de.trustable.ca3s.core.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * REST controller for managing the current user login using OIDC / OAuth.
 */
@RestController
@RequestMapping("/oidc")
@CrossOrigin
public class OIDCAuthenticationResource {

    private final Logger log = LoggerFactory.getLogger(OIDCAuthenticationResource.class);

    private final String providerName;

    public OIDCAuthenticationResource( @Value("${ca3s.oidc.provider-name:#{null}}") String providerName) {
        this.providerName = providerName;
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public ResponseEntity<String> getAuthenticatedUser(HttpServletRequest request,
                                                       @RequestParam Map<String, String> allParams) {

        HttpHeaders httpHeaders = new HttpHeaders();
        if (request.getUserPrincipal() == null) {

            ServletUriComponentsBuilder redirectUriBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
            String redirectCodeUri = redirectUriBuilder.replacePath("/oauth2/authorize-client/").path(providerName)
                .build().normalize().toString();
            log.info("redirectCodeUri : '{}'", redirectCodeUri);

            httpHeaders.add(HttpHeaders.LOCATION, redirectCodeUri);
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        }

        log.info("Bearer Token created for oidc Authentication");
        return new ResponseEntity<>(request.getRemoteUser(), httpHeaders, HttpStatus.FOUND);
    }

}
