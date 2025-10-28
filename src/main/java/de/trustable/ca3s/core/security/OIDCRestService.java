package de.trustable.ca3s.core.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;


@Service
public class OIDCRestService {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCRestService.class);

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

}
