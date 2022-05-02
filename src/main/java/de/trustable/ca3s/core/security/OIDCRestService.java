package de.trustable.ca3s.core.security;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.keycloak.OAuth2Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.domain.UserPreference.USER_PREFERENCE_KEYCLOAK_ID;


@Service
public class OIDCRestService {

    private static final Logger LOG = LoggerFactory.getLogger(OIDCRestService.class);

    final private String keycloakTokenUri;
    final private String keycloakUserInfo;
    final private String keycloakLogout;
    final private String clientId;
    final private String grantType;
    final private String clientSecret;
    final private String scope;

    final private String[] rolesUserArr;
    final private String[] rolesDomainRAArr;
    final private String[] rolesRAArr;
    final private String[] rolesAdminArr;

    final private UserPreferenceRepository userPreferenceRepository;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;

    final private PasswordEncoder passwordEncoder;

    public OIDCRestService(@Value("${ca3s.oidc.token-uri}") String keycloakTokenUri,
                           @Value("${ca3s.oidc.user-info-uri}") String keycloakUserInfo,
                           @Value("${ca3s.oidc.logout}") String keycloakLogout,
                           @Value("${ca3s.oidc.roles.user:USER}") String[] rolesUserArr,
                           @Value("${ca3s.oidc.roles.domainra:DOMAIN_RA}") String[] rolesDomainRAArr,
                           @Value("${ca3s.oidc.roles.ra:RA}") String[] rolesRAArr,
                           @Value("${ca3s.oidc.roles.admin:ADMIN}") String[] rolesAdminArr,
                           @Value("${ca3s.oidc.client-id}") String clientId,
                           @Value("${ca3s.oidc.authorization-grant-type}") String grantType,
                           @Value("${ca3s.oidc.client-secret}") String clientSecret,
                           @Value("${ca3s.oidc.scope}") String scope,
                           UserPreferenceRepository userPreferenceRepository,
                           UserRepository userRepository,
                           AuthorityRepository authorityRepository,
                           PasswordEncoder passwordEncoder) {
        this.keycloakTokenUri = keycloakTokenUri;
        this.keycloakUserInfo = keycloakUserInfo;
        this.keycloakLogout = keycloakLogout;

        this.rolesUserArr = rolesUserArr;
        this.rolesDomainRAArr = rolesDomainRAArr;
        this.rolesRAArr = rolesRAArr;
        this.rolesAdminArr = rolesAdminArr;

        this.clientId = clientId;
        this.grantType = grantType;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     *  login by using username and password to oidc, and capturing token on response body
     *
     * @param username
     * @param password
     * @return
     */
    public KeycloakUserId login(String username, String password) throws JsonProcessingException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username",username);
        map.add("password",password);
        map.add("client_id",clientId);
        map.add("grant_type",grantType);
        map.add("client_secret",clientSecret);
        map.add("scope",scope);

        LOG.info("map: {}", map);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        String userId = restTemplate.postForObject(keycloakTokenUri, request, String.class);

        LOG.info("userId: {}", userId);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(userId, KeycloakUserId.class);
    }

    public String exchangeCodeToToken( final String authCode, final String redirectUri ) throws JsonProcessingException, UnsupportedEncodingException {

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

    /**
     *  logging out and disabling active token from oidc
     *
     * @param refreshToken
     */
    public void logout(String refreshToken) throws Exception {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id",clientId);
        map.add("client_secret",clientSecret);
        map.add("refresh_token",refreshToken);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, null);
        restTemplate.postForObject(keycloakLogout, request, String.class);
    }

    public List<String> getRoles(String token) throws Exception {
        KeycloakUserDetails keycloakUserDetails = getUserInfo(token);
        return Arrays.asList(keycloakUserDetails.getRoles());
    }

    @Transactional
    public KeycloakUserDetails getUserInfo(String token) throws JsonProcessingException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", "Bearer " + token);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
        LOG.info("request: {}", request);

        String userInfo = restTemplate.postForObject(keycloakUserInfo, request, String.class);
        LOG.debug("userInfo: {}", userInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        KeycloakUserDetails keycloakUserDetails = objectMapper.readValue(userInfo, KeycloakUserDetails.class);

        if( keycloakUserDetails.getSub().isEmpty() ){
            LOG.info("no subscriber retrieved for token {}", token);
        }else {
            List<UserPreference> userPreferenceList =
                userPreferenceRepository.findByNameContent(USER_PREFERENCE_KEYCLOAK_ID, keycloakUserDetails.getSub());

            if( userPreferenceList.isEmpty()){
                User user = new User();
                user.setPassword(passwordEncoder.encode(RandomStringUtils.random(16)));
                user.setActivated(true);
                user.setManagedExternally(true);

                updateUserFromKeycloak(keycloakUserDetails, user);
                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(user.getId());
                userPreference.setName(USER_PREFERENCE_KEYCLOAK_ID);
                userPreference.setContent(keycloakUserDetails.getSub());
                userPreferenceRepository.save(userPreference);
                LOG.info("created new user {}", user.getId());
            }else{
                UserPreference userPreference = userPreferenceList.get(0);
                Optional<User> userOptional = userRepository.findById(userPreference.getUserId());
                if( userOptional.isPresent()){
                    User user = userOptional.get();
                    updateUserFromKeycloak(keycloakUserDetails, user);
                }else{
                    LOG.warn("no user retrievable for user id {}", userPreference.getUserId());
                }
            }
        }
        return keycloakUserDetails;
    }

    private void updateUserFromKeycloak(KeycloakUserDetails keycloakUserDetails, User user) {
        boolean update = false;
        String effLoginName = keycloakUserDetails.getName();
        if( (effLoginName == null) || effLoginName.isEmpty()){
            effLoginName = keycloakUserDetails.getPreferred_username();
            if( (effLoginName == null) || effLoginName.isEmpty()) {
                effLoginName = keycloakUserDetails.getEmail();
                if( (effLoginName == null) || effLoginName.isEmpty()) {
                    effLoginName = (keycloakUserDetails.getGiven_name() + " " + keycloakUserDetails.getFamily_name()).trim();
                    LOG.debug("using 'given name' and 'family name' ('{}') as login", effLoginName);
                }else{
                    LOG.debug("using 'email' ('{}') as login", effLoginName);
                }
            }else{
                LOG.debug("using 'preferred_username' ('{}') as login", effLoginName);
            }
        }else{
            LOG.debug("using 'name' ('{}') as login", effLoginName);
        }

        if(!StringUtils.equals(user.getLogin(), effLoginName)){
            LOG.info("oidc data updates user name from '{}' to '{}'", user.getLogin(), effLoginName);
            user.setLogin(effLoginName);
            update = true;
        }
        if(!StringUtils.equals(user.getFirstName(), keycloakUserDetails.getGiven_name())){
            LOG.info("oidc data updates first name from '{}' to '{}'", user.getFirstName(), keycloakUserDetails.getGiven_name());
            user.setFirstName(keycloakUserDetails.getGiven_name());
            update = true;
        }
        if(!StringUtils.equals(user.getLastName(), keycloakUserDetails.getFamily_name())){
            LOG.info("oidc data updates first name from '{}' to '{}'", user.getLastName(), keycloakUserDetails.getFamily_name());
            user.setLastName(keycloakUserDetails.getFamily_name());
            update = true;
        }
        if(!StringUtils.equals(user.getEmail(), keycloakUserDetails.getEmail())){
            LOG.info("oidc data updates first name from '{}' to '{}'", user.getEmail(), keycloakUserDetails.getEmail());
            user.setEmail(keycloakUserDetails.getEmail());
            update = true;
        }

        if(!user.isManagedExternally()){
            user.setManagedExternally(true);
            update = true;
        }

        Set<Authority> authoritySet = getAuthoritiesFromKeycloak(keycloakUserDetails.getRoles());

        if( authoritySet.containsAll(user.getAuthorities()) && user.getAuthorities().containsAll(authoritySet)){
            LOG.debug("Roles local / oidc are identical");
        }else{
            LOG.info("oidc roles '{}' != current roles '{}'", authoritySet, user.getAuthorities());
            user.setAuthorities(authoritySet);
            update = true;
        }

        if(update){
            user.setLastUserDetailsUpdate(Instant.now());
            userRepository.save(user);
        }
    }

    public Set<GrantedAuthority> getAuthorities(final KeycloakUserDetails keycloakUserDetails){
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        for( Authority authority:  getAuthoritiesFromKeycloak(keycloakUserDetails.getRoles())){
            LOG.debug("oidc role '{}' added to granted roles", authority.getName());
            grantedAuthoritySet.add(new SimpleGrantedAuthority(authority.getName()));
        }
        return grantedAuthoritySet;
    }

    private Set<Authority> getAuthoritiesFromKeycloak(String[] roles) {
        Set<Authority> authoritySet = new HashSet<>();

        for( Authority authority: authorityRepository.findAll()){

            if( authority.getName().equalsIgnoreCase("ROLE_USER")){
//                addMatchedRole(authoritySet, roles, authority, rolesUserArr);
                authoritySet.add(authority);
                LOG.debug("added role '{}' due to oidc login", authority.getName());

            }else if( authority.getName().equalsIgnoreCase("ROLE_RA_DOMAIN")){
                addMatchedRole(authoritySet, roles, authority, rolesDomainRAArr);
            }else if( authority.getName().equalsIgnoreCase("ROLE_RA")){
                addMatchedRole(authoritySet, roles, authority, rolesRAArr);
            }else if( authority.getName().equalsIgnoreCase("ROLE_ADMIN")){
                addMatchedRole(authoritySet, roles, authority, rolesAdminArr);
            }else{
                LOG.warn("Unexpected authority '{}' !", authority.getName());
            }
       }

       return authoritySet;
    }

    private void addMatchedRole(Set<Authority> authoritySet, String[] oidcRoles, Authority authority, String[] rolesNameArr) {

        if((oidcRoles == null) || (oidcRoles.length == 0)){
            LOG.debug("addMatchedRole : roles from kerberos is empty");
            return;
        }

        for (String role : rolesNameArr) {
            LOG.debug("addMatchedRole accepted role '{}' for authority '{}'", role, authority.getName());
        }

        for (String role : oidcRoles) {
            if (ArrayUtils.contains(rolesNameArr, role)) {
                authoritySet.add(authority);
                LOG.debug("addMatchedRole checking oidc role '{}' does match mapping", role);
            }else{
                LOG.debug("addMatchedRole checking oidc role '{}' does not match mapping", role);
            }
        }
    }
}
