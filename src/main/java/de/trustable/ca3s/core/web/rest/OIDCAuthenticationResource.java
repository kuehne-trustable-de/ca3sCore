package de.trustable.ca3s.core.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.security.KeycloakUserDetails;
import de.trustable.ca3s.core.security.OIDCRestService;
import de.trustable.ca3s.core.security.jwt.JWTFilter;
import de.trustable.ca3s.core.security.jwt.TokenProvider;
import de.trustable.ca3s.core.service.mapper.OIDCUserProviderMapper;
import org.jetbrains.annotations.NotNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.TokenVerifier;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.rotation.PublicKeyLocator;
import org.keycloak.common.VerificationException;
import org.keycloak.common.util.KeycloakUriBuilder;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.adapters.config.AdapterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.*;

/**
 * REST controller for managing the current user login using KeyCloak.
 */
@RestController
@RequestMapping("/oidc")
public class OIDCAuthenticationResource {

    private final Logger log = LoggerFactory.getLogger(OIDCAuthenticationResource.class);

    private final TokenProvider tokenProvider;
    private final String keycloakAuthorizationUri;
    private final String flowType;
    private final String clientId;
    private final boolean usePostLogoutRedirectUri;
    private final OIDCRestService oidcRestService;
    final private OIDCUserProviderMapper oidcUserProviderMapper;

    private KeycloakDeployment deployment;

    public OIDCAuthenticationResource(TokenProvider tokenProvider,
//                                      @Value("${ca3s.oidc.authorization-uri:}") String keycloakAuthorizationUri,
                                      @Value("${ca3s.oidc.auth-server-url:}") String keycloakAuthorizationUri,
                                      @Value("${ca3s.oidc.realm:@null}") String realm,
                                      @Value("${ca3s.oidc.client-id:#{null}}") String clientId,
                                      @Value("${ca3s.oidc.flow-type:code}") String flowType,
                                      @Value("${ca3s.oidc.use-post-logout-redirect-uri:true}") boolean usePostLogoutRedirectUri,
                                      OIDCRestService OIDCRestService, OIDCUserProviderMapper oidcUserProviderMapper) {
        this.tokenProvider = tokenProvider;
        this.keycloakAuthorizationUri = keycloakAuthorizationUri;
        this.usePostLogoutRedirectUri = usePostLogoutRedirectUri;
        this.oidcRestService = OIDCRestService;
        this.flowType = flowType;
        this.oidcUserProviderMapper = oidcUserProviderMapper;

        if (keycloakAuthorizationUri.isEmpty()) {
            log.info("OIDC not configured, 'ca3s.oidc.authorization-uri' is empty!");
            this.clientId = "";
        } else if( clientId == null){
            log.info("OIDC not configured, 'ca3s.oidc.client-id' is empty!");
            this.clientId = "";
        } else {
            this.clientId = clientId;

//            String authServerUrl = keycloakAuthorizationUri;

            try {
//                String authUrl = StringUtils.substringBefore(authServerUrl, "/realms/");
//                String postRealms = StringUtils.substringAfter(authServerUrl, "/realms/");
//                String realm = StringUtils.substringBefore(postRealms, '/');

                String authUrl = keycloakAuthorizationUri;
                log.info("authUrl : {}, realm : {}", authUrl, realm);

                AdapterConfig adapterConfig = new AdapterConfig();
                if(realm != null) {
                    adapterConfig.setRealm(realm);
                }
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
    public ResponseEntity<String> getAuthenticatedUser(HttpServletRequest request,
                                                       @RequestParam Map<String,String> allParams) {

        if (keycloakAuthorizationUri.isEmpty()) {
            log.info("oidc Authentication not configured");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders httpHeaders = new HttpHeaders();

        if (request.getUserPrincipal() == null) {
            // not authenticated, yet
            log.info("Not authenticated, forwarding");

            ServletUriComponentsBuilder redirectUriBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
            String redirectUrlPart = "/../code";
            if( "implicit".equalsIgnoreCase(this.flowType)){
                redirectUrlPart = "../../..";
            }

            KeycloakUriBuilder locationUrlBuilder = deployment.getAuthUrl().clone()
                .queryParam(OAuth2Constants.CLIENT_ID, deployment.getResourceName())
                .queryParam(OAuth2Constants.SCOPE, OAuth2Constants.SCOPE_OPENID)
                ;

            if( "implicit".equalsIgnoreCase(this.flowType)){
                locationUrlBuilder
                    .queryParam(OAuth2Constants.RESPONSE_TYPE, OAuth2Constants.TOKEN)
                    .queryParam("nonce", "" + System.currentTimeMillis());
            }else{
                locationUrlBuilder
                    .queryParam(OAuth2Constants.RESPONSE_TYPE, OAuth2Constants.CODE);
            }

            String state = "";

            for( String key: allParams.keySet()){
                if( !key.equalsIgnoreCase(OAuth2Constants.CLIENT_ID) &&
                    !key.equalsIgnoreCase(OAuth2Constants.REDIRECT_URI) &&
                    !key.equalsIgnoreCase(OAuth2Constants.SCOPE) &&
                    !key.equalsIgnoreCase(OAuth2Constants.STATE) &&
                    !key.equalsIgnoreCase(OAuth2Constants.RESPONSE_TYPE) &&
                    !key.equalsIgnoreCase("nonce")){

                    /*
                    if( allParams.containsKey(INITIAL_URI_PARAM_NAME)){
                        String intialUriValue = allParams.get(INITIAL_URI_PARAM_NAME);
                        if( !intialUriValue.trim().isEmpty()) {
                            log.debug("initialUri defined as '{}'", intialUriValue);

                            try {
                                URL intialUriAsUrl = new URL(intialUriValue);
                                String path = intialUriAsUrl.getPath();
                                log.debug("initialUri has path '{}'",path);
                                log.debug("initialUri has query '{}'",intialUriAsUrl.getQuery());

                                if(path.equals("/pkcsxx") ||
                                    path.equals("/requestCertificate") ||
                                    path.equals("/cert-info")||
                                    path.equals("/csr-info")){
                                    if( !state.isEmpty()){
                                        state += "&";
                                    }
                                    state += REDIRECT_URI_PARAM_PATH + "=" + path;
                                }

                                MultiValueMap<String, String> parameters =
                                    UriComponentsBuilder.fromUriString(intialUriValue).build().getQueryParams();
                                for( String paramKey: parameters.keySet()){
                                    if( paramKey.equals(PIPELINE_ID) ||
                                        paramKey.equals(CSR_ID)||
                                        paramKey.equals(CERTIFICATE_ID) ||
                                        paramKey.equals(SHOW_NAV_BAR)){
                                        if( !state.isEmpty()){
                                            state += "&";
                                        }
                                        state += paramKey + "=" + parameters.toSingleValueMap().get(paramKey);
                                    }
                                }
                            } catch (MalformedURLException e) {
                                log.info("unparsable initialUri detected", e);
                            }
                        }
                    }else {
*/
                    {
                        log.debug("passing query parameter '{}' with value '{}' as redirect uri", key, allParams.get(key));
                        locationUrlBuilder.queryParam(key, allParams.get(key));
                    }
                }
            }

            if( state.isEmpty()) {
//                locationUrlBuilder.queryParam(OAuth2Constants.STATE, UUID.randomUUID().toString());
            }else{
                locationUrlBuilder.queryParam(OAuth2Constants.STATE, state);
                log.info("state : '{}'", state);
            }

            String redirectCodeUri = redirectUriBuilder.path(redirectUrlPart).build().normalize().toString();
            locationUrlBuilder.queryParam(OAuth2Constants.REDIRECT_URI, redirectCodeUri);

            String locationUrl = locationUrlBuilder.build().toString();
            log.info("locationUrl : '{}'", locationUrl);

            httpHeaders.add("Access-Control-Allow-Origin", "*");
            httpHeaders.add("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS");
            httpHeaders.add("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token");
            httpHeaders.add("Location", locationUrl);
            return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
        }

        log.info("Bearer Token created for oidc Authentication");
        return new ResponseEntity<>(request.getRemoteUser(), httpHeaders, HttpStatus.OK);
    }

    @GetMapping(value={"/code", "/code/"})
    public ResponseEntity<String> getCode(HttpServletRequest request,
                                          @RequestParam Map<String,String> allParams){

        String code = allParams.get("code");
        String accessToken = allParams.get("accessToken");

        log.debug("getCode() code : '{}', accessToken '{}'", code, accessToken);

        if (keycloakAuthorizationUri.isEmpty()) {
            log.info("oidc Authentication not configured");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        try {
            if( accessToken == null || accessToken.trim().isEmpty()){
                ServletUriComponentsBuilder redirectUriBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
                String redirectUri = redirectUriBuilder.build().normalize().toString();

//                ServletUriComponentsBuilder redirectUriBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
//                String redirectUri = redirectUriBuilder.path("/../code").build().normalize().toString();
                log.debug("getCode() redirectUri is '{}'", redirectUri);


                String token = oidcRestService.exchangeCodeToToken(deployment.getTokenUrl(), code, redirectUri);
                log.debug("getCode() code : '{}', token was '{}'", code, token);

                String userinfoURL = deployment.getTokenUrl().replace("/token", "/userinfo");
                log.debug("connecting for user info to  '{}' with token was '{}'", userinfoURL, token);

                KeycloakUserDetails keycloakUserDetails = oidcRestService.getUserInfo(userinfoURL, token);

                if (keycloakUserDetails != null) {
                    ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
                    return buildAndForwardJWT(servletUriComponentsBuilder, keycloakUserDetails);
                } else {
                    log.info("keycloakUserDetails == null, token was '{}'", token);
                }
            }else{
                log.info("ToDo : authToken = '{}'", accessToken);
            }

        } catch (RuntimeException re) {
            log.warn("problem processing OIDC code, token or authentication", re);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            log.warn("problem processing OIDC code and token", e);
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @NotNull
    private ResponseEntity<String> buildAndForwardJWT(ServletUriComponentsBuilder servletUriComponentsBuilder,
                                                      KeycloakUserDetails keycloakUserDetails) {

        SecurityContext securityContext = SecurityContextHolder.getContext();
        log.info("Current authentication in SecurityContext: " + securityContext.getAuthentication());

        List<OpenIDAttribute> attributes = new ArrayList<>();


        OpenIDAuthenticationToken authentication = new OpenIDAuthenticationToken(
            oidcUserProviderMapper.retrieveUserName(keycloakUserDetails),
            getAuthorities(keycloakUserDetails),
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
        return new ResponseEntity<>(keycloakUserDetails.getName(), httpHeaders, HttpStatus.TEMPORARY_REDIRECT);
    }

    public Set<GrantedAuthority> getAuthorities(final KeycloakUserDetails keycloakUserDetails){
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        for( Authority authority:  oidcUserProviderMapper.getAuthorities(keycloakUserDetails)){
            log.debug("oidc role '{}' added to granted roles", authority.getName());
            grantedAuthoritySet.add(new SimpleGrantedAuthority(authority.getName()));
        }
        return grantedAuthoritySet;
    }

    @GetMapping(value={"/tokenImplicit"})
    public ResponseEntity<String> getToken(HttpServletRequest request,
                                        @RequestParam(required = false, name = "access_token") String access_token) {

        log.debug("getToken(): retrieved token '{}'", access_token);

        if( access_token != null && !access_token.trim().isEmpty()) {

            TokenVerifier<AccessToken> tokenVerifier = TokenVerifier.create(access_token, AccessToken.class);

            tokenVerifier.withDefaultChecks()
                .realmUrl(deployment.getRealmInfoUrl());

            try {
                String kid = tokenVerifier.getHeader().getKeyId();
                PublicKey publicKey = getPublicKey(kid, deployment);
                tokenVerifier.publicKey(publicKey);

                AccessToken accessToken = tokenVerifier.verify().getToken();
                log.info("accessToken Name: {}", accessToken.getName());

                KeycloakUserDetails keycloakUserDetails = oidcRestService.getUserInfo(accessToken);

                if (keycloakUserDetails != null) {
                    ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
                    return buildAndForwardJWT(servletUriComponentsBuilder, keycloakUserDetails);
                } else {
                    log.info("keycloakUserDetails == null, token was '{}'", access_token);
                }

            } catch (VerificationException e) {
                log.warn("problem processing access token", e);

            }
        }

        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    private PublicKey getPublicKey(String kid, KeycloakDeployment deployment) throws VerificationException {
        PublicKeyLocator pkLocator = deployment.getPublicKeyLocator();

        PublicKey publicKey = pkLocator.getPublicKey(kid, deployment);
        if (publicKey == null) {
            log.warn("Didn't find publicKey for kid: {}", kid);
            throw new VerificationException("Didn't find publicKey for specified kid");
        }

        return publicKey;
    }

    @CrossOrigin
    @PostMapping("/logout")
    public ResponseEntity logout(HttpServletRequest request) {

        HttpHeaders httpHeaders = new HttpHeaders();

        ServletUriComponentsBuilder servletUriComponentsBuilder = ServletUriComponentsBuilder.fromRequestUri(request);
        String redirectCodeUri = servletUriComponentsBuilder.path("/../..").queryParam("instantLogin","false").build().normalize().toString();
        if( deployment == null || deployment.getLogoutUrl() == null) {
            log.info("logout call without OIDC info");
        }else {

            KeycloakUriBuilder builder = deployment.getLogoutUrl().clone();
            if(usePostLogoutRedirectUri) {
                builder.queryParam(OAuth2Constants.POST_LOGOUT_REDIRECT_URI, redirectCodeUri)
                    .queryParam(OAuth2Constants.CLIENT_ID, clientId);
            }else{
                builder.queryParam(OAuth2Constants.REDIRECT_URI, redirectCodeUri);
            }

            String redirectUrl = builder.build().toString();
            log.info("logout redirectUrl : '{}'", redirectUrl);

            httpHeaders.add("Access-Control-Allow-Origin", "*");
            httpHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            httpHeaders.add("Access-Control-Allow-Headers", "Origin, Content-Type, X-Auth-Token");
            httpHeaders.add("Location", redirectUrl);
        }

        return new ResponseEntity<>(httpHeaders, HttpStatus.OK);
    }
}
