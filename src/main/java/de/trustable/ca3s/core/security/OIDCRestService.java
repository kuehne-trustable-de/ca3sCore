package de.trustable.ca3s.core.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import de.trustable.ca3s.core.service.mapper.OIDCUserProviderMapper;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class OIDCRestService {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCRestService.class);

    /**
     * Prefix used for realm level roles.
     */
    public static final String PREFIX_REALM_ROLE = "ROLE_realm_";
    /**
     * Prefix used in combination with the resource (client) name for resource level roles.
     */
    public static final String PREFIX_RESOURCE_ROLE = "ROLE_";

    /**
     * Name of the claim containing the realm level roles
     */
    private static final String CLAIM_REALM_ACCESS = "realm_access";
    /**
     * Name of the claim containing the resources (clients) the user has access to.
     */
    private static final String CLAIM_RESOURCE_ACCESS = "resource_access";
    /**
     * Name of the claim containing roles. (Applicable to realm and resource level.)
     */
    private static final String CLAIM_ROLES = "roles";

    final private String clientId;
    final private String clientSecret;

    final private OIDCUserProviderMapper oidcUserProviderMapper;


    public OIDCRestService(@Value("${ca3s.oidc.client-id:#{null}}") String clientId,
                           @Value("${ca3s.oidc.client-secret:clientSecret}") String clientSecret,
                           OIDCUserProviderMapper oidcUserProviderMapper
                           ) {


        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.oidcUserProviderMapper = oidcUserProviderMapper;
    }


    public String exchangeCodeToToken( final String keycloakTokenUri, final String authCode, final String redirectUri ) throws JsonProcessingException, UnsupportedEncodingException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(OAuth2Constants.CODE,authCode);
        map.add(OAuth2Constants.CLIENT_ID,clientId);
        map.add(OAuth2Constants.GRANT_TYPE,"authorization_code");
        map.add(OAuth2Constants.CLIENT_SECRET,clientSecret);
        map.add(OAuth2Constants.REDIRECT_URI, redirectUri);

        LOG.info("sending map: {} to URL '{}'", map, keycloakTokenUri);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String authToken = restTemplate.postForObject(keycloakTokenUri, request, String.class);

        LOG.info("authToken: {}", authToken);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KeycloakUserId keycloakUserId = objectMapper.readValue(authToken, KeycloakUserId.class);

        return keycloakUserId.getAccess_token();
    }

    @Transactional
    public KeycloakUserDetails getUserInfo(final String keycloakUserInfoUrl, final String token) throws JsonProcessingException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        LOG.info("request: {}", request);

        String userInfo = restTemplate.postForObject(keycloakUserInfoUrl, request, String.class);
        LOG.debug("userInfo: {}", userInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KeycloakUserDetails keycloakUserDetails = objectMapper.readValue(userInfo, KeycloakUserDetails.class);

        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            String[] rolesArr = getRoles(signedJWT);
            keycloakUserDetails.setRoles(rolesArr);
        } catch (ParseException e) {
            LOG.info("parsing of JWT failed", e);
        }

        oidcUserProviderMapper.storeUserInfo(keycloakUserDetails);

        return keycloakUserDetails;
    }

    @Transactional
    public KeycloakUserDetails getUserInfo(final AccessToken token) {

        KeycloakUserDetails keycloakUserDetails = new KeycloakUserDetails();

        keycloakUserDetails.setEmail( token.getEmail());
        keycloakUserDetails.setName(token.getName());
        keycloakUserDetails.setFamily_name(token.getFamilyName());
        keycloakUserDetails.setGiven_name(token.getGivenName());
        keycloakUserDetails.setPreferred_username(token.getPreferredUsername());
        String[] roleArr = {"ROLE_USER"};
        keycloakUserDetails.setRoles(roleArr);
        keycloakUserDetails.setSub(token.getSubject());

        oidcUserProviderMapper.storeUserInfo(keycloakUserDetails);

        return keycloakUserDetails;
    }

    String[] getRoles(SignedJWT signedJWT) throws ParseException {

        Collection<String> rolesList = new ArrayList<>();

        JWTClaimsSet claimSet = signedJWT.getJWTClaimsSet();
        Object claim = claimSet.getClaim(CLAIM_REALM_ACCESS);
        LOG.info("CLAIM_REALM_ACCESS has type {} and value {}", claim.getClass(), claim);

        try {
            // Realm roles
            // Get the part of the access token that holds the roles assigned on realm level
            Map<String, Collection<String>> realmAccess = (Map<String, Collection<String>>)claim;

            // From the realm_access claim get the roles
            Collection<String> roles = realmAccess.get(CLAIM_ROLES);
            // Check if any roles are present
            if (roles != null && !roles.isEmpty()) {
                // Iterate of the roles and add them to the granted authorities
                Collection<String> realmRoles = roles.stream()
                    // Prefix all realm roles with "ROLE_realm_"
                    .map(role -> PREFIX_REALM_ROLE + role)
                    .collect(Collectors.toList());

                rolesList.addAll(realmRoles);
            }
        } catch (ClassCastException e) {
            LOG.info("problem processing realm claims", e);
        }

        Object claimResource = claimSet.getClaim(CLAIM_RESOURCE_ACCESS);
        LOG.info("CLAIM_REALM_ACCESS has type {} and value {}", claimResource.getClass(), claimResource);

        try {
            // Resource (client) roles
            // A user might have access to multiple resources all containing their own roles. Therefore, it is a map of
            // resource each possibly containing a "roles" property.
            Map<String, Map<String, Collection<String>>> resourceAccess = (Map<String, Map<String, Collection<String>>> )claimResource;

            // Iterate of all the resources
            resourceAccess.forEach((resource, resourceClaims) -> {
                // Iterate of the "roles" claim inside the resource claims
                resourceClaims.get(CLAIM_ROLES).forEach(
                    // Add the role to the granted authority prefixed with ROLE_ and the name of the resource
                    role -> rolesList.add(PREFIX_RESOURCE_ROLE + resource + "_" + role)
                );
            });
        } catch (ClassCastException e) {
            LOG.info("problem processing resource claims", e);
        }

        return rolesList.toArray(new String[0]);
    }
}
