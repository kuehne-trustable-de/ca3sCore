package de.trustable.ca3s.core.security.saml;

import de.trustable.ca3s.core.config.saml.SAMLMappingConfig;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.UserPreference;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.UserPreferenceRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.service.dto.Languages;
import org.opensaml.saml2.core.Attribute;
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

import static de.trustable.ca3s.core.domain.UserPreference.USER_PREFERENCE_SAML_ID;

public class CustomSAMLAuthenticationProvider extends SAMLAuthenticationProvider {

    private final Logger LOG = LoggerFactory.getLogger(CustomSAMLAuthenticationProvider.class);


    final private UserPreferenceRepository userPreferenceRepository;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;

    private final Languages languages;

    private final SAMLMappingConfig samlMappingConfig;

    public CustomSAMLAuthenticationProvider(UserPreferenceRepository userPreferenceRepository,
                                            UserRepository userRepository,
                                            AuthorityRepository authorityRepository,
                                            String availableLanguages,
                                            SAMLMappingConfig samlMappingConfig) {
        this.userPreferenceRepository = userPreferenceRepository;
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.languages = new Languages(availableLanguages);
        this.samlMappingConfig = samlMappingConfig;
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
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
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

                updateUserFromKeycloak(id, credential, user);

                UserPreference userPreference = new UserPreference();
                userPreference.setUserId(user.getId());
                userPreference.setName(USER_PREFERENCE_SAML_ID);
                userPreference.setContent(id);
                userPreferenceRepository.save(userPreference);
                LOG.info("created new user {}", user.getId());
            }else{
                User user = userOptional.get();
                updateUserFromKeycloak(id, credential, user);
                LOG.info("updated known user {}", user.getId());
            }
        }
    }

    private void updateUserFromKeycloak(final String effLoginName, final SAMLCredential credential, final User user) {
        boolean update = false;

        if(!StringUtils.equals(user.getLogin(), effLoginName)){
            LOG.info("oidc data updates user name from '{}' to '{}'", user.getLogin(), effLoginName);
            user.setLogin(effLoginName);
            update = true;
        }

        for( Attribute saml2Att: credential.getAttributes()){
            LOG.info("SAML attribute '{}' to '{}'", saml2Att.getName(), saml2Att.getAttributeValues());

        }

        List attributesFirstNameList = Arrays.asList( samlMappingConfig.getAttributesFirstName());
        List attributesLastNameList = Arrays.asList( samlMappingConfig.getAttributesLastName());
        List attributesEmailList = Arrays.asList( samlMappingConfig.getAttributesEmail());

        for(Attribute attribute: credential.getAttributes()){

            if( attributesFirstNameList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setFirstName(attribute.getAttributeValues().get(0).toString());
                    update = true;
                }
            }
            if( attributesLastNameList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setLastName(attribute.getAttributeValues().get(0).toString());
                    update = true;
                }
            }
            if( attributesEmailList.contains(attribute.getName())){
                if( !attribute.getAttributeValues().isEmpty()) {
                    user.setEmail(attribute.getAttributeValues().get(0).toString());
                    update = true;
                }
            }
        }

        if(!user.isManagedExternally()){
            user.setManagedExternally(true);
            update = true;
        }

//        Set<Authority> authoritySet = getAuthoritiesFromKeycloak(keycloakUserDetails.getRoles());
        Set<Authority> authoritySet = new HashSet<>();

        for( Authority authority: authorityRepository.findAll()){

            if( authority.getName().equalsIgnoreCase("ROLE_USER")) {
                authoritySet.add( authority);
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
            userRepository.save(user);
        }
    }

}
