package de.trustable.ca3s.core.config.oidc;

import de.trustable.ca3s.core.config.util.SPeLUtil;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.AuthoritiesConstants;
import de.trustable.ca3s.core.security.SecurityUtils;
import de.trustable.ca3s.core.service.dto.Languages;
import de.trustable.ca3s.core.service.util.PreferenceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.*;

public class CustomOAuth2LoginAuthenticationProvider implements AuthenticationProvider {

    private final Logger LOG = LoggerFactory.getLogger(CustomOAuth2LoginAuthenticationProvider.class);

    public static final String USER_PREFERENCE_OAUTH_ID = "OAUTH_ID";

    private final OAuth2AuthorizationCodeAuthenticationProvider authorizationCodeAuthenticationProvider;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> userService = new DefaultOAuth2UserService();

    final private PreferenceUtil preferenceUtil;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;
    final private TenantRepository tenantRepository;
    final private SPeLUtil sPeLUtil;
    private final Languages languages;

    private final OIDCMappingConfig oidcMappingConfig;

    public CustomOAuth2LoginAuthenticationProvider(OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
                                                   PreferenceUtil preferenceUtil,
                                                   UserRepository userRepository,
                                                   AuthorityRepository authorityRepository,
                                                   TenantRepository tenantRepository,
                                                   SPeLUtil sPeLUtil,
                                                   String availableLanguages,
                                                   OIDCMappingConfig oidcMappingConfig) {
        this.preferenceUtil = preferenceUtil;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.tenantRepository = tenantRepository;
        this.sPeLUtil = sPeLUtil;
        this.languages = new Languages(availableLanguages);
        this.oidcMappingConfig = oidcMappingConfig;

        this.authorizationCodeAuthenticationProvider = new OAuth2AuthorizationCodeAuthenticationProvider(accessTokenResponseClient);
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        // Ths authentication object may NOT contain a principal!
        // for auth details rely on the OAuth2LoginAuthenticationToken object below
        LOG.debug("authenticate( {} )", authentication);

        OAuth2LoginAuthenticationToken loginAuthenticationToken = (OAuth2LoginAuthenticationToken)authentication;
        if (loginAuthenticationToken.getAuthorizationExchange().getAuthorizationRequest().getScopes().contains("openid")) {
            LOG.debug("scope 'openid' found");
        }

        OAuth2AuthorizationCodeAuthenticationToken authorizationCodeAuthenticationToken;
        try {
            authorizationCodeAuthenticationToken = (OAuth2AuthorizationCodeAuthenticationToken)this.authorizationCodeAuthenticationProvider.authenticate(new OAuth2AuthorizationCodeAuthenticationToken(loginAuthenticationToken.getClientRegistration(), loginAuthenticationToken.getAuthorizationExchange()));
            LOG.debug("authorizationCodeAuthenticationToken '{}' found", authorizationCodeAuthenticationToken);
            if( !authorizationCodeAuthenticationToken.isAuthenticated()){
                throw new OAuth2AuthenticationException("User is explicitly NOT authenticated!");
            }
        } catch (OAuth2AuthorizationException ex) {
            OAuth2Error oauth2Error = ex.getError();
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }

        OAuth2AccessToken accessToken = authorizationCodeAuthenticationToken.getAccessToken();
        Map<String, Object> additionalParameters = authorizationCodeAuthenticationToken.getAdditionalParameters();

        LOG.debug("accessToken '{}' found", accessToken);

        Collection<GrantedAuthority> authorities = authorizationCodeAuthenticationToken.getAuthorities();
        if(authorities == null || authorities.isEmpty()){
            authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));
        }

        OAuth2User oauth2User = this.userService.loadUser(new OAuth2UserRequest(loginAuthenticationToken.getClientRegistration(), accessToken, additionalParameters));
        LOG.debug("oauth2User '{}' found", oauth2User);

        OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(loginAuthenticationToken.getClientRegistration(), loginAuthenticationToken.getAuthorizationExchange(), oauth2User, authorities, accessToken, authorizationCodeAuthenticationToken.getRefreshToken());
        authenticationResult.setDetails(loginAuthenticationToken.getDetails());

        String principal = SecurityUtils.extractPrincipal(authenticationResult);
        if( principal == null ){
            throw new InsufficientAuthenticationException("authentication '"+authenticationResult+"' has no principal!" );
        }

        storeUserInfo(principal, authenticationResult);

        return authenticationResult;

    }

    public boolean supports(Class<?> authentication) {
        LOG.debug("supports '{}' : {}", authentication, OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication));
        return OAuth2LoginAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private void storeUserInfo(final String principal, final OAuth2LoginAuthenticationToken authenticationResult) {

        Optional<User> userOptional = userRepository.findOneByLogin(principal);

        if( userOptional.isEmpty()){
            User user = new User();
            user.setPassword("$0$0$0000000000000000000000000000000000000000000000000000000");
            user.setActivated(true);
            user.setManagedExternally(true);

            user.setLangKey(languages.alignLanguage("en"));

            updateUserFromOAuthUser(principal, authenticationResult.getPrincipal(), user);

            preferenceUtil.setPreference(USER_PREFERENCE_OAUTH_ID,user.getId(), USER_PREFERENCE_OAUTH_ID);

            LOG.info("created new user {}", user.getId());
        }else{
            User user = userOptional.get();
            updateUserFromOAuthUser(principal, authenticationResult.getPrincipal(), user);
            LOG.info("updated known user {}", user.getId());
        }
    }

    private void updateUserFromOAuthUser(final String effLoginName, final OAuth2User oauth2User, final User user) {
        boolean update = false;

        String firstNameOld = user.getFirstName();
        String lastNameOld = user.getLastName();
        String emailOld = user.getEmail();
        Tenant tenantOld = user.getTenant();
        String languageOld = user.getLangKey();

        if( LOG.isDebugEnabled()) {
            for (String attributeName : oauth2User.getAttributes().keySet()) {
                LOG.debug("OIDC attribute '{}' with value '{}'", attributeName, oauth2User.getAttributes().get(attributeName));
            }

            for (GrantedAuthority grantedAuthority : oauth2User.getAuthorities()) {
                LOG.debug("OIDC authority '{}' present", grantedAuthority.getAuthority());
            }
        }

        if(!StringUtils.equals(user.getLogin(), effLoginName)){
            LOG.info("oidc data updates user name from '{}' to '{}'", user.getLogin(), effLoginName);
            user.setLogin(effLoginName);
            update = true;
        }

        List<String> attributesFirstNameList = Arrays.asList( oidcMappingConfig.getAttributesFirstName());
        List<String> attributesLastNameList = Arrays.asList( oidcMappingConfig.getAttributesLastName());
        List<String> attributesEmailList = Arrays.asList( oidcMappingConfig.getAttributesEmail());
        List<String> attributesTenantList = Arrays.asList( oidcMappingConfig.getAttributesTenant());
        List<String> attributesLanguageList = Arrays.asList( oidcMappingConfig.getAttributesLanguage());

        HashMap<String, List<String>> attributeMap = new HashMap<>();
        for( String attributeName: oauth2User.getAttributes().keySet()){

            String value = "" + oauth2User.getAttributes().get(attributeName);
            attributeMap.put(attributeName, Collections.singletonList(value));

            if (attributesFirstNameList.contains(attributeName)) {
                user.setFirstName(value);
            }
            if (attributesLastNameList.contains(attributeName)) {
                user.setLastName(value);
            }
            if (attributesEmailList.contains(attributeName)) {
                user.setEmail(value);
            }
            if (attributesTenantList.contains(attributeName)) {
                user.setTenant(findTenantByName(value));
            }
            if (attributesLanguageList.contains(attributeName)) {
                user.setLangKey(value.toLowerCase(Locale.ROOT));
            }

        }

        if (oidcMappingConfig.getExprFirstName() != null && !oidcMappingConfig.getExprFirstName().isEmpty()) {
            user.setFirstName(sPeLUtil.evaluateExpression(attributeMap, oidcMappingConfig.getExprFirstName()));
        }

        if (oidcMappingConfig.getExprLastName() != null && !oidcMappingConfig.getExprLastName().isEmpty()) {
            user.setLastName(sPeLUtil.evaluateExpression(attributeMap, oidcMappingConfig.getExprLastName()));
        }

        if (oidcMappingConfig.getExprEmail() != null && !oidcMappingConfig.getExprEmail().isEmpty()) {
            user.setEmail(sPeLUtil.evaluateExpression(attributeMap, oidcMappingConfig.getExprEmail()));
        }

        if (oidcMappingConfig.getExprTenant() != null && !oidcMappingConfig.getExprTenant().isEmpty()) {
            String tenantName = sPeLUtil.evaluateExpression(attributeMap, oidcMappingConfig.getExprTenant());
            user.setTenant(findTenantByName(tenantName));
        }

        if (oidcMappingConfig.getExprLanguage() != null && !oidcMappingConfig.getExprLanguage().isEmpty()) {
            String language = sPeLUtil.evaluateExpression(attributeMap, oidcMappingConfig.getExprLanguage());
            user.setLangKey(language.toLowerCase(Locale.ROOT));
        }

        if(!Objects.equals(firstNameOld, user.getFirstName())){
            LOG.info("sso first name '{}' updated to '{}'", firstNameOld, user.getFirstName());
            update = true;
        }

        if(!Objects.equals(lastNameOld, user.getLastName())){
            LOG.info("sso last name '{}' updated to '{}'", lastNameOld, user.getLastName());
            update = true;
        }

        if(!Objects.equals(emailOld, user.getEmail())){
            LOG.info("sso email '{}' updated to '{}'", emailOld, user.getEmail());
            update = true;
        }

        if(!Objects.equals(tenantOld, user.getTenant())){

            String tenantNameOld = tenantOld == null ? "null": tenantOld.getName();
            String tenantNameNew = user.getTenant() == null ? "null": user.getTenant().getName();
            LOG.info("tenant '{}' updated to '{}'", tenantNameOld, tenantNameNew);
            update = true;
        }

        if(!Objects.equals(languageOld, user.getLangKey())){
            LOG.info("sso language '{}' updated to '{}'", languageOld, user.getLangKey());
            update = true;
        }

        if(!user.isManagedExternally()){
            user.setManagedExternally(true);
            update = true;
        }

        Set<Authority> authoritySet = new HashSet<>();

        for( Authority authority: authorityRepository.findAll()){
            if( authority.getName().equalsIgnoreCase("ROLE_USER")) {
                authoritySet.add(authority);
            }else if(Arrays.stream(oidcMappingConfig.getRolesOtherArr()).anyMatch(
                role -> role.equalsIgnoreCase(authority.getName()))){
                LOG.debug("authority.getName() {} included in roles {}", authority.getName(), StringUtils.join(oidcMappingConfig.getRolesOtherArr(), ","));

                if( oauth2User.getAuthorities().stream().anyMatch(
                    oidcAuthority-> oidcAuthority.getAuthority().equals(authority.getName()))) {
                    authoritySet.add(authority);
                }
            }
        }

        if( authoritySet.containsAll(user.getAuthorities()) && user.getAuthorities().containsAll(authoritySet)){
            LOG.debug("Roles local / oidc are identical");
        }else{
            LOG.info("oidc roles '{}' != current roles '{}'", authoritySet, user.getAuthorities());
            user.setAuthorities(authoritySet);
            update = true;
        }

        if(update){
            user.setLastUserDetailsUpdate(Instant.now());
        }
        userRepository.save(user);
    }

    private Tenant findTenantByName(String tenantName) {
        Optional<Tenant> tenantOptional = tenantRepository.findByName(tenantName);
        if (tenantOptional.isEmpty()) {
            LOG.info("Unknown tenant: '{}'", tenantName);
//            throw new TenantNotFoundException("Unknown tenant: " + tenantName);
            return null;
        } else {
            Tenant tenant = tenantOptional.get();

            if( !tenant.getActive() ){
                LOG.info("tenant: '{}' deactivated", tenantName);
//                throw new TenantNotFoundException("Unknown tenant: " + tenantName);
                return null;
            }
            return tenant;
        }
    }
}
