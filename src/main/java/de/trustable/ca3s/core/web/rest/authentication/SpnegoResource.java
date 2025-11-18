package de.trustable.ca3s.core.web.rest.authentication;

import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/spnego")
public class SpnegoResource {

    private final TokenProvider tokenProvider;

    private final Logger log = LoggerFactory.getLogger(SpnegoResource.class);

    public SpnegoResource(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /**
     * {@code GET /login} : start the spnego authentication by returning the
     * "WWW-Authenticate" header with the value "Negotiate"
     *
     * @return empty response with status {@code 401 (OK)} and appropriate header.
     */
    @GetMapping("/login")
    public ResponseEntity<?> getSpnegoLogin() {

        log.debug("REST request to start the spnego authentication");

        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        HttpHeaders headers = new HttpHeaders();
        if(authentication instanceof KerberosServiceRequestToken){
            log.info("Kerberos token present for : {}", authentication.getName());
            String jwt = tokenProvider.createToken(authentication, false);
            headers.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);

        }else {
            log.info("Non-Kerberos authentication token found : {} with value {}", authentication.getClass().getName(), authentication);

            headers.add(HttpHeaders.WWW_AUTHENTICATE, "Negotiate");
            return new ResponseEntity<>(headers, HttpStatus.UNAUTHORIZED);
        }
    }

/*
    public void setJWTHeader(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        log.info("Current authentication in SecurityContext: " + securityContext.getAuthentication());

        List<OpenIDAttribute> attributes = new ArrayList<>();

        OpenIDAuthenticationToken authentication = new OpenIDAuthenticationToken(
            oidcRestService.retrieveUserName(keycloakUserDetails),
            oidcRestService.getAuthorities(keycloakUserDetails),
            "identityUrl",
            attributes);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        securityContext.setAuthentication(authentication);
        String jwt = tokenProvider.createToken(authentication, false);

        HttpHeaders httpHeaders = new HttpHeaders();
        UriComponentsBuilder builder = servletUriComponentsBuilder.path("/../..");

        builder.queryParam("bearer", jwt);
        String startUri = builder.build().normalize().toString();
        log.debug("startUri : '{}'", startUri);

        httpHeaders.add("Location", startUri);
        httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
    }

 */
}
