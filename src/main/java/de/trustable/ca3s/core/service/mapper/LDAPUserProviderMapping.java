package de.trustable.ca3s.core.service.mapper;


import com.unboundid.ldap.sdk.*;
import com.unboundid.util.ssl.SSLUtil;
import de.trustable.ca3s.core.config.LDAPConfig;
import de.trustable.ca3s.core.domain.Authority;
import de.trustable.ca3s.core.domain.Tenant;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.repository.AuthorityRepository;
import de.trustable.ca3s.core.repository.TenantRepository;
import de.trustable.ca3s.core.repository.UserRepository;
import de.trustable.ca3s.core.security.provider.Ca3sTrustManager;
import de.trustable.ca3s.core.service.dto.Languages;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.*;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

import static com.unboundid.ldap.sdk.SearchRequest.ALL_USER_ATTRIBUTES;

@Component
public class LDAPUserProviderMapping {

    private static final Logger LOG = LoggerFactory.getLogger(LDAPUserProviderMapping.class);

    final private LDAPConfig ldapConfig;
    final private UserRepository userRepository;
    final private AuthorityRepository authorityRepository;
    final private TenantRepository tenantRepository;
    private final Languages languages;
    private final Ca3sTrustManager ca3sTrustManager;

    public LDAPUserProviderMapping(
        LDAPConfig ldapConfig, UserRepository userRepository,
        AuthorityRepository authorityRepository,
        TenantRepository tenantRepository,
        @Value("${ca3s.ui.languages:en,de,pl}") String availableLanguages, Ca3sTrustManager ca3sTrustManager) {
        this.ldapConfig = ldapConfig;

        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
        this.tenantRepository = tenantRepository;
        this.languages = new Languages(availableLanguages);
        this.ca3sTrustManager = ca3sTrustManager;
    }


    public void updateUserFromLDAP(final User user) throws NamingException {
        boolean update = false;

        String firstNameOld = user.getFirstName();
        String lastNameOld = user.getLastName();
        String emailOld = user.getEmail();
        Tenant tenantOld = user.getTenant();
        String languageOld = user.getLangKey();

        SearchControls sc = new SearchControls();
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
        sc.setReturningAttributes(null);

        try{
            HashMap<String, List<String>> ldapAttributeMap = getUserAttributesFromLDAP( user.getLogin());

            List<String> attributesFirstNameList = Arrays.asList(ldapConfig.getAttributesFirstName());
            List<String> attributesLastNameList = Arrays.asList(ldapConfig.getAttributesLastName());
            List<String> attributesEmailList = Arrays.asList(ldapConfig.getAttributesEmail());
            List<String> attributesTenantList = Arrays.asList(ldapConfig.getAttributesTenant());
            List<String> attributesLanguageList = Arrays.asList(ldapConfig.getAttributesLanguage());


            for (String ldapAttName : ldapAttributeMap.keySet()) {

                List<String> attributeValues = ldapAttributeMap.get(ldapAttName);

                if (attributesFirstNameList.contains(ldapAttName)) {
                    if (!attributeValues.isEmpty()) {
                        user.setFirstName(attributeValues.get(0));
                    }
                }
                if (attributesLastNameList.contains(ldapAttName)) {
                    if (!attributeValues.isEmpty()) {
                        user.setLastName(attributeValues.get(0));
                    }
                }
                if (attributesEmailList.contains(ldapAttName)) {
                    if (!attributeValues.isEmpty()) {
                        user.setEmail(attributeValues.get(0));
                    }
                }
                if (attributesTenantList.contains(ldapAttName)) {
                    if (!attributeValues.isEmpty()) {
                        String tenantName = attributeValues.get(0);
                        user.setTenant(findTenantByName(tenantName));
                    }
                }
                if (attributesLanguageList.contains(ldapAttName)) {
                    if (!attributeValues.isEmpty()) {
                        String language = attributeValues.get(0);
                        user.setLangKey(language.toLowerCase(Locale.ROOT));
                    }
                }
            }

            if (!Objects.equals(firstNameOld, user.getFirstName())) {
                LOG.info("ldap first name '{}' updated to '{}'", firstNameOld, user.getFirstName());
                update = true;
            }

            if (!Objects.equals(lastNameOld, user.getLastName())) {
                LOG.info("ldap last name '{}' updated to '{}'", lastNameOld, user.getLastName());
                update = true;
            }

            if (!Objects.equals(emailOld, user.getEmail())) {
                LOG.info("ldap email '{}' updated to '{}'", emailOld, user.getEmail());
                update = true;
            }

            if (!Objects.equals(tenantOld, user.getTenant())) {

                String tenantNameOld = tenantOld == null ? "null" : tenantOld.getName();
                String tenantNameNew = user.getTenant() == null ? "null" : user.getTenant().getName();
                LOG.info("tenant '{}' updated to '{}'", tenantNameOld, tenantNameNew);
                update = true;
            }

            if (!Objects.equals(languageOld, user.getLangKey())) {
                LOG.info("ldap language '{}' updated to '{}'", languageOld, user.getLangKey());
                update = true;
            }

            if (!user.isManagedExternally()) {
                user.setManagedExternally(true);
                update = true;
            }


            List<String> attributeValues = ldapAttributeMap.get("memberOf");
            Set<Authority> authoritySet = matchRolesAndAuthorities(attributeValues);

            if (authoritySet.containsAll(user.getAuthorities()) && user.getAuthorities().containsAll(authoritySet)) {
                LOG.debug("Roles local / ldap are identical");
            } else {
                LOG.info("ldap roles '{}' != current roles '{}'", authoritySet, user.getAuthorities());
                update = true;
            }

            if (update) {
                user.setLastUserDetailsUpdate(Instant.now());
            }

            user.setAuthorities(authoritySet);
            userRepository.save(user);

        } catch (MalformedURLException|LDAPException|GeneralSecurityException e) {
            LOG.info("accessing ldap fails with exception", e);
        }

    }

    private @NotNull Set<Authority> matchRolesAndAuthorities(List<String> attributeValues) {

        Set<Authority> authoritySet = new HashSet<>();
        for( String attributeValue : attributeValues){
            LOG.debug("checking memberOf attribute value '{}'", attributeValue);
            RDN[] memberOfRdnArr = new X500Name(attributeValue).getRDNs();
            addMatchingAuthorities(authoritySet, memberOfRdnArr);
        }
        return authoritySet;
    }

    private void addMatchingAuthorities(Set<Authority> authoritySet, RDN[] memberOfRdnArr) {

        for (Authority authority : authorityRepository.findAll()) {

            if (authority.getName().equalsIgnoreCase("ROLE_USER")) {
                if (Arrays.stream(ldapConfig.getRolesUserArr()).anyMatch(role -> {
                    LOG.info("role '{}' identifying as 'user'", role);
                    if("*".equals(role)){
                        return true;
                    }
                    return rdnMatchesRole(role, memberOfRdnArr);
                })) {
                    LOG.info("ldap role matching {} found", authority.getName());
                    authoritySet.add(authority);
                }
            }

            if (authority.getName().equalsIgnoreCase("ROLE_RA")) {
                if (Arrays.stream(ldapConfig.getRolesRAArr()).anyMatch(roles -> {
                    return rdnMatchesRole(roles, memberOfRdnArr);
                })) {
                    LOG.info("ldap role matching {} found", authority.getName());
                    authoritySet.add(authority);
                }
            }
            if (authority.getName().equalsIgnoreCase("ROLE_ADMIN")) {
                if (Arrays.stream(ldapConfig.getRolesAdminArr()).anyMatch(roles -> {
                    return rdnMatchesRole(roles, memberOfRdnArr);
                })) {
                    LOG.info("ldap role matching {} found", authority.getName());
                    authoritySet.add(authority);
                }
            }
        }
    }

    public static boolean rdnMatchesRole(String roles, RDN[] rdnArr){

        if( roles.trim().isEmpty() ){
            return false;
        }
        X500Name x500NameRoles = new X500Name(roles);
        boolean isPresent = Arrays.stream(x500NameRoles.getRDNs()).allMatch(rdn->(Arrays.asList(rdnArr).contains(rdn)));
        LOG.info("role '{}' present in member RDNs", roles);
        return isPresent;
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

    HashMap<String, List<String>> getUserAttributesFromLDAP(final String username) throws GeneralSecurityException, LDAPException, MalformedURLException {

        HashMap<String, List<String>> ldapAttributeMap = new HashMap<>();

        // For testing only: trust all certs. For production, use SSLUtil with a truststore.
        SSLUtil sslUtil = new SSLUtil(ca3sTrustManager);
        LDAPConnectionOptions options = new LDAPConnectionOptions();
        options.setConnectTimeoutMillis(5000);

        try(LDAPConnection conn = new LDAPConnection(sslUtil.createSSLSocketFactory(), options,
            ldapConfig.getLdapHost(),
            ldapConfig.getLdapPort(),
            ldapConfig.getPrincipal(),
            ldapConfig.getPassword())){

            Filter filter = Filter.and(
                Filter.equals("objectClass", "user"),
                Filter.equals("sAMAccountName", username)
            );

            SearchRequest searchRequest = new SearchRequest(ldapConfig.getBaseDN(), SearchScope.SUB, filter, ALL_USER_ATTRIBUTES );
            com.unboundid.ldap.sdk.SearchResult result = conn.search(searchRequest);
            for(SearchResultEntry enty: result.getSearchEntries()){
                for( com.unboundid.ldap.sdk.Attribute attr: enty.getAttributes()){
                    List<String> valueList = new ArrayList<>();
                    Collections.addAll(valueList, attr.getValues());
                    ldapAttributeMap.put(attr.getName(), valueList);
                    LOG.info("LDAP attribute '{}' to '{}'", attr.getName(), valueList);
                }
            }
        }

        return ldapAttributeMap;
    }
}
