package de.trustable.ca3s.core.web.rest;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.trustable.ca3s.core.security.OIDCRestService;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.security.oidc.OpenIdConnectUserDetails;
import de.trustable.ca3s.core.service.mapper.OIDCUserProviderMapper;
import de.trustable.ca3s.core.config.oidc.OpenIdConnectConfig;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.PortResolver;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

/**
 * REST controller for managing the current user login using KeyCloak.
 */
@RestController
public class OIDCGoogleAuthenticationResource {

    private final Logger LOG = LoggerFactory.getLogger(OIDCGoogleAuthenticationResource.class);

    private final TokenProvider tokenProvider;
    private final String oidcAuthorizationUri;
    private final String flowType;
    private final String redirectUri;
    private final boolean usePostLogoutRedirectUri;
    private final OIDCRestService oidcRestService;
    final private OIDCUserProviderMapper oidcUserProviderMapper;

    private PortResolver portResolver = new PortResolverImpl();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    @Value("ca3s.oidc.client-id:#{null}")
    private String clientId;

    @Value("${ca3s.oidc.issuer}")
    private String issuer;

    @Value("${ca3s.oidc.jwkUrl}")
    private String jwkUrl;

    private final OpenIdConnectConfig openIdConnectConfig;

    private final OAuth2RestOperations restTemplate;


    public OIDCGoogleAuthenticationResource(TokenProvider tokenProvider,
                                            @Value("${ca3s.oidc.authorization-uri:}") String oidcAuthorizationUri,
                                            @Value("${ca3s.oidc.realm:@null}") String realm,
                                            @Value("${ca3s.oidc.client-id:#{null}}") String clientId,
                                            @Value("${ca3s.oidc.flow-type:code}") String flowType,
                                            @Value("${ca3s.oidc.redirectUri:google-login}") String redirectUri,
                                            @Value("${ca3s.oidc.use-post-logout-redirect-uri:true}") boolean usePostLogoutRedirectUri,
                                            OIDCRestService OIDCRestService,
                                            OIDCUserProviderMapper oidcUserProviderMapper, OpenIdConnectConfig openIdConnectConfig,
                                            OAuth2RestOperations restTemplate) {
        this.tokenProvider = tokenProvider;
        this.oidcAuthorizationUri = oidcAuthorizationUri;
        this.usePostLogoutRedirectUri = usePostLogoutRedirectUri;
        this.oidcRestService = OIDCRestService;
        this.flowType = flowType;
        this.oidcUserProviderMapper = oidcUserProviderMapper;
        this.restTemplate = restTemplate;

        this.redirectUri = redirectUri;
        this.openIdConnectConfig = openIdConnectConfig;

        if (oidcAuthorizationUri.isEmpty()) {
            LOG.info("OIDC not configured, 'ca3s.oidc.authorization-uri' is empty!");
            this.clientId = "";
        } else if( clientId == null){
            LOG.info("OIDC not configured, 'ca3s.oidc.client-id' is empty!");
            this.clientId = "";
        } else {
            this.clientId = clientId;

//            String authServerUrl = oidcAuthorizationUri;

            try {
//                String authUrl = StringUtils.substringBefore(authServerUrl, "/realms/");
//                String postRealms = StringUtils.substringAfter(authServerUrl, "/realms/");
//                String realm = StringUtils.substringBefore(postRealms, '/');

                String authUrl = oidcAuthorizationUri;
                LOG.info("authUrl : {}, realm : {}", authUrl, realm);

                AdapterConfig adapterConfig = new AdapterConfig();
                if(realm != null) {
                    adapterConfig.setRealm(realm);
                }
                adapterConfig.setAuthServerUrl(authUrl);
                adapterConfig.setResource(clientId);

            } catch (Exception cause) {
                throw new RuntimeException("Failed to parse the realm name.", cause);
            }
        }
    }


    @GetMapping("/google-login")
    public ResponseEntity<Void> attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {

        LOG.info("Attempting OpenID Connect Authentication" );
        LOG.info("Attempting OpenID Connect Authentication" );

        OAuth2AccessToken accessToken;
        try {
            LOG.info("oidc restTemplate.getResource(): {}",restTemplate.getResource() );
            accessToken = restTemplate.getAccessToken();
            LOG.info("oidc accessToken: {}", accessToken.getValue() );
        } catch (final OAuth2Exception e) {
            LOG.info("oidc accessToken problem", e );
            throw new BadCredentialsException("Could not obtain access token", e);
        } catch (final RuntimeException e) {
            LOG.info("oidc accessToken problem", e );
            throw new BadCredentialsException("Could not obtain access token", e);
        }

        try {
            final String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
            LOG.info("oidc id_token: {}", idToken );

            String kid = JwtHelper.headers(idToken).get("kid");
            final Jwt tokenDecoded = JwtHelper.decodeAndVerify(idToken, verifier(kid));
            final Map<String, String> authInfo = new ObjectMapper().readValue(tokenDecoded.getClaims(), Map.class);
            verifyClaims(authInfo);
            LOG.info("oidc authInfo: {}", authInfo );
            final OpenIdConnectUserDetails user = new OpenIdConnectUserDetails(authInfo, accessToken);
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

            LOG.info( "authenticated name {}", usernamePasswordAuthenticationToken.getName());

            ServletUriComponentsBuilder redirectUriBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
            String redirectUrlPart = "/../code";
            if( "implicit".equalsIgnoreCase(this.flowType)){
                redirectUrlPart = "../../..";
            }

            String redirectCodeUri = redirectUriBuilder.path(redirectUrlPart).build().normalize().toString();
//            locationUrlBuilder.queryParam(OAuth2Constants.REDIRECT_URI, redirectCodeUri);

            String locationUrl = "locationUrlBuilder/" + redirectCodeUri;

            LOG.info("locationUrl : '{}'", locationUrl);

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Access-Control-Allow-Origin", "*");
            httpHeaders.add("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
            httpHeaders.add("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token");
            httpHeaders.add("Location", locationUrl);
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);

        } catch (final Exception e) {
            throw new BadCredentialsException("Could not obtain user details from token", e);
        }

    }
    public void verifyClaims(Map claims) {
        int exp = (int) claims.get("exp");
        Date expireDate = new Date(exp * 1000L);
        Date now = new Date();
        if (expireDate.before(now) || !claims.get("iss").equals(issuer) || !claims.get("aud").equals(clientId)) {
            throw new RuntimeException("Invalid claims");
        }
    }


    private RsaVerifier verifier(String kid) throws Exception {
        JwkProvider provider = new UrlJwkProvider(new URI(jwkUrl).toURL());
        Jwk jwk = provider.get(kid);
        return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
    }

}
