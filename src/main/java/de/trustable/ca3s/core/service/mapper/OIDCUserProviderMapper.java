package de.trustable.ca3s.core.service.mapper;

import de.trustable.ca3s.core.config.oidc.OIDCMappingConfig;
import de.trustable.ca3s.core.config.util.SPeLUtil;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.KeycloakUserDetails;
import de.trustable.ca3s.core.service.dto.Languages;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.*;

import static de.trustable.ca3s.core.domain.UserPreference.USER_PREFERENCE_KEYCLOAK_ID;

@Component
public class OIDCUserProviderMapper {

    private final Logger LOG = LoggerFactory.getLogger(OIDCUserProviderMapper.class);

    final private OIDCMappingConfig oidcMappingConfig;
    final private UserPreferenceRepository userPreferenceRepository;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;
    final private TenantRepository tenantRepository;
    final private SPeLUtil sPeLUtil;
    private final Languages languages;
    final private PasswordEncoder passwordEncoder;

    public OIDCUserProviderMapper(OIDCMappingConfig oidcMappingConfig,
                                  UserPreferenceRepository userPreferenceRepository,
                                  UserRepository userRepository,
                                  AuthorityRepository authorityRepository,
                                  TenantRepository tenantRepository,
                                  SPeLUtil sPeLUtil,
                                  @Value("${ca3s.ui.languages:en,de,pl}") String availableLanguages,
                                  PasswordEncoder passwordEncoder) {
        this.oidcMappingConfig = oidcMappingConfig;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.tenantRepository = tenantRepository;
        this.sPeLUtil = sPeLUtil;
        this.languages = new Languages(availableLanguages);
        this.passwordEncoder = passwordEncoder;

        List<Authority> authorityList = authorityRepository.findAll();
        for(String role: oidcMappingConfig.getRolesOtherArr()){
            Authority newAuthority = new Authority();
            newAuthority.setName(role);
            if( !authorityList.contains(newAuthority)){
                authorityRepository.save(newAuthority);
            }
        }
    }

    public void storeUserInfo(final KeycloakUserDetails keycloakUserDetails) {

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

                updateUserFromOIDCUserDetails(keycloakUserDetails, user);
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
                    updateUserFromOIDCUserDetails(keycloakUserDetails, user);
                }else{
                    LOG.warn("no user retrievable for user id {}", userPreference.getUserId());
                }
            }
        }
    }

    private void updateUserFromOIDCUserDetails(final KeycloakUserDetails keycloakUserDetails, final User user) {

        boolean update = false;

        String effLoginName = retrieveUserName(keycloakUserDetails);

        String firstNameOld = user.getFirstName();
        String lastNameOld = user.getLastName();
        String emailOld = user.getEmail();
        Tenant tenantOld = user.getTenant();
        String languageOld = user.getLangKey();

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

        HashMap<String, List<String>> attributeMap = new HashMap<>();
        attributeMap.put("Name", Collections.singletonList(keycloakUserDetails.getName()));
        attributeMap.put("FamilyName", Collections.singletonList(keycloakUserDetails.getFamily_name()));
        attributeMap.put("PreferredUsername", Collections.singletonList(keycloakUserDetails.getPreferred_username()));
        attributeMap.put("Email", Collections.singletonList(keycloakUserDetails.getEmail()));
        attributeMap.put("Sub", Collections.singletonList(keycloakUserDetails.getSub()));
        if( keycloakUserDetails.getRoles() == null){
            attributeMap.put("Roles", Collections.EMPTY_LIST);
        }else {
            attributeMap.put("Roles", List.of(keycloakUserDetails.getRoles()));
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


        Set<Authority> authoritySet = getAuthorities(keycloakUserDetails);

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

    public Set<Authority> getAuthorities(final KeycloakUserDetails keycloakUserDetails){

        Set<Authority> authoritySet = new HashSet<>();
        for( Authority authority:  getAuthoritiesFromOIDC(keycloakUserDetails.getRoles())){
            LOG.debug("oidc role '{}' added to granted roles", authority.getName());
            authoritySet.add(authority);
        }
        return authoritySet;
    }

    private Set<Authority> getAuthoritiesFromOIDC(String[] roles) {
        Set<Authority> authoritySet = new HashSet<>();

        for( Authority authority: authorityRepository.findAll()){

            if( authority.getName().equalsIgnoreCase("ROLE_USER")){
                addMatchedRole(authoritySet, roles, authority, oidcMappingConfig.getRolesUserArr());
            }else if( authority.getName().equalsIgnoreCase("ROLE_RA_DOMAIN")){
                addMatchedRole(authoritySet, roles, authority, oidcMappingConfig.getRolesDomainRAArr());
            }else if( authority.getName().equalsIgnoreCase("ROLE_RA")){
                addMatchedRole(authoritySet, roles, authority, oidcMappingConfig.getRolesRAArr());
            }else if( authority.getName().equalsIgnoreCase("ROLE_ADMIN")){
                addMatchedRole(authoritySet, roles, authority, oidcMappingConfig.getRolesAdminArr());
            }else{
                addMatchedRole(authoritySet, roles, authority, oidcMappingConfig.getRolesOtherArr());
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
                LOG.debug("addMatchedRole added authority '{}' as default role", role);
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
