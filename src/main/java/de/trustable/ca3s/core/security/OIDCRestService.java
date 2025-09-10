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
import de.trustable.ca3s.core.service.dto.Languages;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.keycloak.OAuth2Constants;
import org.keycloak.representations.AccessToken;
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

    final private String clientId;
    final private String clientSecret;

    final private String[] rolesUserArr;
    final private String[] rolesDomainRAArr;
    final private String[] rolesRAArr;
    final private String[] rolesAdminArr;
    final private String[] rolesOtherArr;

    final private UserPreferenceRepository userPreferenceRepository;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;

    final private PasswordEncoder passwordEncoder;
    private final Languages languages;


    public OIDCRestService(@Value("${ca3s.oidc.roles.user:USER}") String[] rolesUserArr,
                           @Value("${ca3s.oidc.roles.domainra:DOMAIN_RA}") String[] rolesDomainRAArr,
                           @Value("${ca3s.oidc.roles.ra:RA}") String[] rolesRAArr,
                           @Value("${ca3s.oidc.roles.admin:ADMIN}") String[] rolesAdminArr,
                           @Value("${ca3s.oidc.roles.other}") String[] rolesOtherArr,
                           @Value("${ca3s.oidc.client-id:#{null}}") String clientId,
                           @Value("${ca3s.oidc.client-secret:clientSecret}") String clientSecret,
                           @Value("${ca3s.ui.languages:en,de,pl}") String availableLanguages,
                           UserPreferenceRepository userPreferenceRepository,
                           UserRepository userRepository,
                           AuthorityRepository authorityRepository,
                           PasswordEncoder passwordEncoder) {

        this.rolesUserArr = rolesUserArr;
        this.rolesDomainRAArr = rolesDomainRAArr;
        this.rolesRAArr = rolesRAArr;
        this.rolesAdminArr = rolesAdminArr;
        this.rolesOtherArr = rolesOtherArr;

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.passwordEncoder = passwordEncoder;

        this.languages = new Languages(availableLanguages);

        List<Authority> authorityList = authorityRepository.findAll();
        for(String role: rolesOtherArr){
            Authority newAuthority = new Authority();
            newAuthority.setName(role);
            if( !authorityList.contains(newAuthority)){
                authorityRepository.save(newAuthority);
            }
        }
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

        storeUserInfo(keycloakUserDetails);

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

        storeUserInfo(keycloakUserDetails);

        return keycloakUserDetails;
    }

    private void storeUserInfo(final KeycloakUserDetails keycloakUserDetails) {

        if( keycloakUserDetails.getSub().isEmpty() ){
            LOG.info("no subscriber retrieved for token {}", keycloakUserDetails);
        }else {
            List<UserPreference> userPreferenceList =
                userPreferenceRepository.findByNameContent(USER_PREFERENCE_KEYCLOAK_ID, keycloakUserDetails.getSub());

            if( userPreferenceList.isEmpty()){
                User user = new User();
                user.setPassword(passwordEncoder.encode(RandomStringUtils.random(16)));
                user.setActivated(true);
                user.setManagedExternally(true);

                user.setLangKey(languages.alignLanguage("en"));

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
    }

    private void updateUserFromKeycloak(KeycloakUserDetails keycloakUserDetails, User user) {
        boolean update = false;

        String effLoginName = retrieveUserName(keycloakUserDetails);

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

        Set<Authority> authoritySet = getAuthoritiesFromOIDC(keycloakUserDetails.getRoles());

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

    @NotNull
    public String retrieveUserName(KeycloakUserDetails keycloakUserDetails) {

        String effLoginName = keycloakUserDetails.getName();
        if( (effLoginName == null) || effLoginName.isEmpty()){
            effLoginName = keycloakUserDetails.getPreferred_username();
            if( (effLoginName == null) || effLoginName.isEmpty()) {
                effLoginName = keycloakUserDetails.getEmail();
                if( (effLoginName == null) || effLoginName.isEmpty()) {
                    effLoginName = (keycloakUserDetails.getGiven_name() + "_" + keycloakUserDetails.getFamily_name()).trim();
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

        effLoginName = effLoginName.replaceAll("[^_.@A-Za-z0-9-]", "_");
        return effLoginName;
    }

    public Set<GrantedAuthority> getAuthorities(final KeycloakUserDetails keycloakUserDetails){
        Set<GrantedAuthority> grantedAuthoritySet = new HashSet<>();
        for( Authority authority:  getAuthoritiesFromOIDC(keycloakUserDetails.getRoles())){
            LOG.debug("oidc role '{}' added to granted roles", authority.getName());
            grantedAuthoritySet.add(new SimpleGrantedAuthority(authority.getName()));
        }
        return grantedAuthoritySet;
    }

    private Set<Authority> getAuthoritiesFromOIDC(String[] roles) {
        Set<Authority> authoritySet = new HashSet<>();

        for( Authority authority: authorityRepository.findAll()){

            if( authority.getName().equalsIgnoreCase("ROLE_USER")){
                addMatchedRole(authoritySet, roles, authority, rolesUserArr);
            }else if( authority.getName().equalsIgnoreCase("ROLE_RA_DOMAIN")){
                addMatchedRole(authoritySet, roles, authority, rolesDomainRAArr);
            }else if( authority.getName().equalsIgnoreCase("ROLE_RA")){
                addMatchedRole(authoritySet, roles, authority, rolesRAArr);
            }else if( authority.getName().equalsIgnoreCase("ROLE_ADMIN")){
                addMatchedRole(authoritySet, roles, authority, rolesAdminArr);
            }else{

                addMatchedRole(authoritySet, roles, authority, rolesOtherArr);
                LOG.warn("Unexpected authority '{}' !", authority.getName());
            }
       }

        if(authoritySet.isEmpty()){
            for( Authority authority: authorityRepository.findAll()){
                if( authority.getName().equalsIgnoreCase("ROLE_USER")){
                    authoritySet.add(authority);
                    LOG.warn("No relevant authority from oidc, adding fallback role 'ROLE_USER' !");
                }
            }
        }

       return authoritySet;
    }

    private boolean addMatchedRole(Set<Authority> authoritySet, String[] oidcRoles, Authority authority, String[] rolesNameArr) {

        if((oidcRoles == null) || (oidcRoles.length == 0)){
            LOG.debug("addMatchedRole : roles from identity provider are empty");
            return false;
        }

        for (String role : rolesNameArr) {
            if( "*".equals(role.trim())){
                authoritySet.add(authority);
                LOG.debug("addMatchedRole added authority '{}' as default role", role, authority.getName());
                return true;
            }else {
                LOG.debug("addMatchedRole accepts role '{}' for authority '{}'", role, authority.getName());
            }
        }

        boolean hasMatch = false;
        for (String role : oidcRoles) {
            if (ArrayUtils.contains(rolesNameArr, role)) {
                authoritySet.add(authority);
                hasMatch = true;
                LOG.debug("addMatchedRole checking oidc role '{}' does match mapping !", role);
            }else{
                LOG.debug("addMatchedRole checking oidc role '{}' does not match mapping", role);
            }
        }

        return hasMatch;
    }

}
