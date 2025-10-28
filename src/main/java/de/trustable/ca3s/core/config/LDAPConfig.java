package de.trustable.ca3s.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LDAPConfig {


    final private String adDomain;
    final private String ldapUrl;
    final private String ldapHost;
    final private int ldapPort;
    final private String baseDN;
    final private String principal;
    final private String password;

    private final String rolesUserArr;
    private final String rolesDomainRAArr;
    private final String rolesRAArr;
    private final String rolesAdminArr;
    final private String rolesOtherArr;

    private final String[] attributesFirstName;
    private final String[] attributesLastName;
    private final String[] attributesEmail;
    private final String[] attributesTenant;
    private final String[] attributesLanguage;

    public LDAPConfig(@Value("${ca3s.auth.ad-domain:unknownDomain}") String adDomain,
                      @Value("${ca3s.auth.ldap.url:#{null}}") String ldapUrl,
                      @Value("${ca3s.auth.ldap.host:#{null}}") String ldapHost,
                      @Value("${ca3s.auth.ldap.port:636}") int ldapPort,
                      @Value("${ca3s.auth.ldap.baseDN:#{null}}") String baseDN,
                      @Value("${ca3s.auth.ldap.principal:#{null}}") String principal,
                      @Value("${ca3s.auth.ldap.password:#{null}}") String password,
                      @Value("${ca3s.auth.ldap.roles.user:USER}") String rolesUserArr,
                      @Value("${ca3s.auth.ldap.roles.domainra:}") String rolesDomainRAArr,
                      @Value("${ca3s.auth.ldap.roles.ra:}") String rolesRAArr,
                      @Value("${ca3s.auth.ldap.roles.admin:}") String rolesAdminArr,
                      @Value("${ca3s.auth.ldap.roles.other:}") String rolesOtherArr,
                      @Value("${ca3s.auth.ldap.attributes.firstName:firstName}") String[] attributesFirstName,
                      @Value("${ca3s.auth.ldap.attributes.lastName:lastName}") String[] attributesLastName,
                      @Value("${ca3s.auth.ldap.attributes.email:email}") String[] attributesEmail,
                      @Value("${ca3s.auth.ldap.attributes.tenant:}") String[] attributesTenant,
                      @Value("${ca3s.auth.ldap.attributes.language:}") String[] attributesLanguage) {

        this.adDomain = adDomain;
        this.ldapUrl = ldapUrl;
        this.ldapHost = ldapHost;
        this.ldapPort = ldapPort;
        this.baseDN = baseDN;
        this.principal = principal;
        this.password = password;

        this.rolesUserArr = rolesUserArr;
        this.rolesDomainRAArr = rolesDomainRAArr;
        this.rolesRAArr = rolesRAArr;
        this.rolesAdminArr = rolesAdminArr;
        this.rolesOtherArr = rolesOtherArr;
        this.attributesFirstName = attributesFirstName;
        this.attributesLastName = attributesLastName;
        this.attributesEmail = attributesEmail;
        this.attributesTenant = attributesTenant;
        this.attributesLanguage = attributesLanguage;

    }

    public String getAdDomain() {
        return adDomain;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }

    public String getLdapHost() {
        return ldapHost;
    }

    public int getLdapPort() {
        return ldapPort;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public String getPrincipal() {
        return principal;
    }

    public String getPassword() {
        return password;
    }

    public String[] getRolesUserArr() {
        String[] tmpArr = new String[1];
        tmpArr[0] = rolesUserArr;
        return tmpArr;
    }

    public String[] getRolesDomainRAArr() {
        String[] tmpArr = new String[1];
        tmpArr[0] = rolesDomainRAArr;
        return tmpArr;
    }

    public String[] getRolesRAArr() {
        String[] tmpArr = new String[1];
        tmpArr[0] = rolesRAArr;
        return tmpArr;
    }

    public String[] getRolesAdminArr() {
        String[] tmpArr = new String[1];
        tmpArr[0] = rolesAdminArr;
        return tmpArr;
    }

    public String[] getRolesOtherArr() {
        String[] tmpArr = new String[1];
        tmpArr[0] = rolesOtherArr;
        return tmpArr;
    }

    public String[] getAttributesFirstName() {
        return attributesFirstName;
    }

    public String[] getAttributesLastName() {
        return attributesLastName;
    }

    public String[] getAttributesEmail() {
        return attributesEmail;
    }

    public String[] getAttributesTenant() {
        return attributesTenant;
    }

    public String[] getAttributesLanguage() {
        return attributesLanguage;
    }
}
