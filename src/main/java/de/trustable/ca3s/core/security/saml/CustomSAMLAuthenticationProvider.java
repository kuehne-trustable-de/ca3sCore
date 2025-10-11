package de.trustable.ca3s.core.security.saml;

import de.trustable.ca3s.core.config.saml.SAMLMappingConfig;
import de.trustable.ca3s.core.config.util.SPeLUtil;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.dto.Languages;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.providers.ExpiringUsernameAuthenticationToken;
import org.springframework.security.saml.SAMLAuthenticationProvider;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static de.trustable.ca3s.core.domain.UserPreference.USER_PREFERENCE_SAML_ID;

public class CustomSAMLAuthenticationProvider extends SAMLAuthenticationProvider {

    private final Logger LOG = LoggerFactory.getLogger(CustomSAMLAuthenticationProvider.class);


    final private UserPreferenceRepository userPreferenceRepository;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;
    final private TenantRepository tenantRepository;
    final private SPeLUtil sPeLUtil;
    private final Languages languages;

    private final SAMLMappingConfig samlMappingConfig;

    public CustomSAMLAuthenticationProvider(UserPreferenceRepository userPreferenceRepository,
                                            UserRepository userRepository,
                                            AuthorityRepository authorityRepository,
                                            TenantRepository tenantRepository,
                                            SPeLUtil sPeLUtil,
                                            String availableLanguages,
                                            SAMLMappingConfig samlMappingConfig) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.tenantRepository = tenantRepository;
        this.sPeLUtil = sPeLUtil;
        this.languages = new Languages(availableLanguages);
        this.samlMappingConfig = samlMappingConfig;

        List<Authority> authorityList = authorityRepository.findAll();
        for(String role: samlMappingConfig.getRolesOtherArr()){
            Authority newAuthority = new Authority();
            newAuthority.setName(role);
            if( !authorityList.contains(newAuthority)){
                authorityRepository.save(newAuthority);
            }
        }
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Authentication authenticationAuthed = super.authenticate(authentication);
        LOG.debug("authenticate(authentication) succeeded");
        return authenticationAuthed;
    }

    @Override
    @Transactional
    public Collection<? extends GrantedAuthority> getEntitlements(SAMLCredential credential, Object userDetail) {

        LOG.debug("SAML credential processing");
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (credential != null && credential.getNameID() != null) {
            LOG.debug("saml role '{}' added to granted roles", credential.getNameID().getValue());
            authorities.add(new SimpleGrantedAuthority(credential.getNameID().getValue()));

            storeUserInfo(credential);
        }

        if(userDetail instanceof ExpiringUsernameAuthenticationToken) {
            authorities.addAll(((ExpiringUsernameAuthenticationToken) userDetail).getAuthorities());
            for( GrantedAuthority authority: authorities) {
                LOG.debug("SAML authority: {}", authority);
            }
        }

        return authorities;
    }

    private void storeUserInfo(final SAMLCredential credential) {

        if( credential == null || credential.getNameID() == null){
            LOG.debug("No / not sufficient SAML credentials provided!");
        }else {
            String id = credential.getNameID().getValue();

            Optional<User> userOptional = userRepository.findOneByLogin(id);

            if( userOptional.isEmpty()){
                User user = new User();
                user.setPassword("$0$0$0000000000000000000000000000000000000000000000000000000");
                user.setActivated(true);
                user.setManagedExternally(true);

                user.setLangKey(languages.alignLanguage("en"));

                updateUserFromSAMLCredentials(id, credential, user);

                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(user.getId());
                userPreference.setName(USER_PREFERENCE_SAML_ID);
                userPreference.setContent(id);
                userPreferenceRepository.save(userPreference);
                LOG.info("created new user {}", user.getId());
            }else{
                User user = userOptional.get();
                updateUserFromSAMLCredentials(id, credential, user);
                LOG.info("updated known user {}", user.getId());
            }
        }
    }

    private void updateUserFromSAMLCredentials(final String effLoginName, final SAMLCredential credential, final User user) {
        boolean update = false;

        String firstNameOld = user.getFirstName();
        String lastNameOld = user.getLastName();
        String emailOld = user.getEmail();
        Tenant tenantOld = user.getTenant();
        String languageOld = user.getLangKey();

        if(!StringUtils.equals(user.getLogin(), effLoginName)){
            LOG.info("saml data updates user name from '{}' to '{}'", user.getLogin(), effLoginName);
            user.setLogin(effLoginName);
            update = true;
        }

        for( Attribute saml2Att: credential.getAttributes()){
            LOG.info("SAML attribute '{}' to '{}'", saml2Att.getName(), saml2Att.getAttributeValues().get(0));

        }

        List<String> attributesFirstNameList = Arrays.asList( samlMappingConfig.getAttributesFirstName());
        List<String> attributesLastNameList = Arrays.asList( samlMappingConfig.getAttributesLastName());
        List<String> attributesEmailList = Arrays.asList( samlMappingConfig.getAttributesEmail());
        List<String> attributesTenantList = Arrays.asList( samlMappingConfig.getAttributesTenant());
        List<String> attributesLanguageList = Arrays.asList( samlMappingConfig.getAttributesLanguage());

        HashMap<String, List<String>> attributeMap = new HashMap<>();
        for(Attribute attribute: credential.getAttributes()){

            attributeMap.put(attribute.getName(), fromXMLObjectList(attribute.getAttributeValues()));

            if( attributesFirstNameList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setFirstName(fromXMLObject(attribute.getAttributeValues().get(0)));
                }
            }
            if( attributesLastNameList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setLastName(fromXMLObject(attribute.getAttributeValues().get(0)));
                }
            }
            if( attributesEmailList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setEmail(fromXMLObject(attribute.getAttributeValues().get(0)));
                }
            }
            if( attributesTenantList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    String tenantName = fromXMLObject(attribute.getAttributeValues().get(0));
                    user.setTenant(findTenantByName(tenantName));
                }
            }
            if( attributesLanguageList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    String language = fromXMLObject(attribute.getAttributeValues().get(0));
                    user.setLangKey(language.toLowerCase(Locale.ROOT));
                }
            }

        }

        if (samlMappingConfig.getExprFirstName() != null && !samlMappingConfig.getExprFirstName().isEmpty()) {
            user.setFirstName(sPeLUtil.evaluateExpression(attributeMap, samlMappingConfig.getExprFirstName()));
        }

        if (samlMappingConfig.getExprLastName() != null && !samlMappingConfig.getExprLastName().isEmpty()) {
            user.setLastName(sPeLUtil.evaluateExpression(attributeMap, samlMappingConfig.getExprLastName()));
        }

        if (samlMappingConfig.getExprEmail() != null && !samlMappingConfig.getExprEmail().isEmpty()) {
            user.setEmail(sPeLUtil.evaluateExpression(attributeMap, samlMappingConfig.getExprEmail()));
        }

        if (samlMappingConfig.getExprTenant() != null && !samlMappingConfig.getExprTenant().isEmpty()) {
            String tenantName = sPeLUtil.evaluateExpression(attributeMap, samlMappingConfig.getExprTenant());
            user.setTenant(findTenantByName(tenantName));
        }

        if (samlMappingConfig.getExprLanguage() != null && !samlMappingConfig.getExprLanguage().isEmpty()) {
            String language = sPeLUtil.evaluateExpression(attributeMap, samlMappingConfig.getExprLanguage());
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
            }else if(Arrays.stream(samlMappingConfig.getRolesOtherArr()).anyMatch(
                role -> role.equalsIgnoreCase(authority.getName()))){
                LOG.debug("authority.getName() {} included in roles {}", authority.getName(), StringUtils.join(samlMappingConfig.getRolesOtherArr(), ","));

                if( credential.getAttributes().stream().anyMatch(
                    saml2Att-> saml2Att.getName().equals("Role") && ((XSString)saml2Att.getAttributeValues().get(0)).getValue().equals(authority.getName()))) {
                    authoritySet.add(authority);
                }
            }
        }

        if( authoritySet.containsAll(user.getAuthorities()) && user.getAuthorities().containsAll(authoritySet)){
            LOG.debug("Roles local / saml are identical");
        }else{
            LOG.info("saml roles '{}' != current roles '{}'", authoritySet, user.getAuthorities());
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

    private String fromXMLObject(XMLObject xmlObject){
        if( xmlObject instanceof XSString){
            return ((XSString)xmlObject).getValue();
        }else{
            return xmlObject.toString();
        }
    }

    private List<String> fromXMLObjectList(List<XMLObject> xmlObjectList){
        return
            xmlObjectList.stream()
            .map(this::fromXMLObject)
            .collect(Collectors.toList());
    }

}
