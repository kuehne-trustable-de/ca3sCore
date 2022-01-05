package de.trustable.ca3s.core.web.rest;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.trustable.ca3s.core.security.KeycloakUserDetails;
import de.trustable.ca3s.core.security.OIDCRestService;
import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.apache.commons.lang3.StringUtils;
import org.keycloak.OAuth2Constants;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing the current user login using KeyCloak.
 */
@RestController
@RequestMapping("/oidc")
public class OIDCAuthenticationResource {

    private final Logger log = LoggerFactory.getLogger(OIDCAuthenticationResource.class);

    private final TokenProvider tokenProvider;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final String keycloakAuthorizationUri;
    private final OIDCRestService oidcRestService;
    private KeycloakDeployment deployment;

    public OIDCAuthenticationResource(TokenProvider tokenProvider,
                                      AuthenticationManagerBuilder authenticationManagerBuilder,
                                      @Value("${ca3s.oidc.authorization-uri:}") String keycloakAuthorizationUri,
                                      @Value("${ca3s.oidc.client-id}") String clientId,
                                      @Value("${ca3s.oidc.client-secret}") String clientSecret,
                                      OIDCRestService OIDCRestService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.keycloakAuthorizationUri = keycloakAuthorizationUri;
        this.oidcRestService = OIDCRestService;

        if(keycloakAuthorizationUri.isEmpty()) {

            String authServerUrl = keycloakAuthorizationUri;

            try {
                String authUrl = StringUtils.substringBefore(authServerUrl, "/realms/");
                String postRealms = StringUtils.substringAfter(authServerUrl, "/realms/");
                String realm = StringUtils.substringBefore(postRealms, '/');
                log.info("authUrl : {}, realm : {}", authUrl, realm);

                AdapterConfig adapterConfig = new AdapterConfig();
                adapterConfig.setRealm(realm);
                adapterConfig.setAuthServerUrl(authUrl);
                adapterConfig.setResource(clientId);

                deployment = KeycloakDeploymentBuilder.build(adapterConfig);
                log.info("Using oidcDeployment: {}", deployment);

            } catch (Exception cause) {
                throw new RuntimeException("Failed to parse the realm name.", cause);
            }
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @CrossOrigin
    @GetMapping("/authenticate")
    public ResponseEntity<String> getAuthenticatedUser(HttpServletRequest request) {

        if(keycloakAuthorizationUri.isEmpty()) {
            log.info("oidc Authentication not configured");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders httpHeaders = new HttpHeaders();

        if (request.getUserPrincipal() == null) {
            // not authenticated, yet
            log.info("Not authenticated, forwarding");

            ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
            String redirectCodeUri = servletUriComponentsBuilder.path("/../code").build().normalize().toString();

            KeycloakUriBuilder builder = deployment.getAuthUrl().clone()
                .queryParam(OAuth2Constants.RESPONSE_TYPE, OAuth2Constants.CODE)
                .queryParam(OAuth2Constants.CLIENT_ID, deployment.getResourceName())
                .queryParam(OAuth2Constants.REDIRECT_URI, redirectCodeUri)
                .queryParam(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID)
                .queryParam(OAuth2Constants.STATE, UUID.randomUUID().toString());

            String redirectUrl = builder.build().toString();
            log.info("redirectUrl : '{}'", redirectUrl);

            httpHeaders.add("Access-Control-Allow-Origin", "*");
            httpHeaders.add("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
            httpHeaders.add("Access-Control-Allow-Headers","Origin, Content-Type, X-Auth-Token");
            httpHeaders.add("Location", redirectUrl);
            return new ResponseEntity<String>(httpHeaders, HttpStatus.OK);
        }

        log.info("Bearer Token created for oidc Authentication");
        return new ResponseEntity<>(request.getRemoteUser(), httpHeaders, HttpStatus.OK);
    }

    @GetMapping("/code")
    public ResponseEntity<String> getCode(HttpServletRequest request, @RequestParam(name ="code") String code) {

        if(keycloakAuthorizationUri.isEmpty()) {
            log.info("oidc Authentication not configured");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
            String redirectUri = servletUriComponentsBuilder.path("/../code").build().normalize().toString();

            String token = oidcRestService.exchangeCodeToToken( code, redirectUri );
            log.info("getCode() code : '{}', token was '{}'", code, token);

            KeycloakUserDetails keycloakUserDetails = oidcRestService.getUserInfo(token);
            if( keycloakUserDetails != null){

                SecurityContext securityContext = SecurityContextHolder.getContext();
                log.info("Current authentication in SecurityContext: " + securityContext.getAuthentication());

                List<OpenIDAttribute> attributes = new ArrayList<>();

                OpenIDAuthenticationToken authentication = new OpenIDAuthenticationToken(keycloakUserDetails.getName(),
                    oidcRestService.getAuthorities(keycloakUserDetails),
                    "identityUrl",
                    attributes);

                SecurityContextHolder.getContext().setAuthentication(authentication);

                securityContext.setAuthentication(authentication);
                String jwt = tokenProvider.createToken(authentication, false);

                HttpHeaders httpHeaders = new HttpHeaders();
                String startUri = servletUriComponentsBuilder.path("/../..").queryParam("bearer", jwt) .build().normalize().toString();

                httpHeaders.add("Location", startUri);
                httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
                return new ResponseEntity<>(keycloakUserDetails.getName(), httpHeaders, HttpStatus.TEMPORARY_REDIRECT);
            }else{
                log.info("keycloakUserDetails == null, token was '{}'", token);
            }

        } catch (RuntimeException re) {
            log.warn("problem processing OIDC code, token or authentication", re);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.warn("problem processing OIDC code and token", e);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}
