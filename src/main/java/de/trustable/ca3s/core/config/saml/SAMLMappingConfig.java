package de.trustable.ca3s.core.config.saml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SAMLMappingConfig {

    private final String[] rolesUserArr;
    private final String[] rolesDomainRAArr;
    private final String[] rolesRAArr;
    private final String[] rolesAdminArr;
    private final String[] attributesFirstName;
    private final String[] attributesLastName;
    private final String[] attributesEmail;


    public SAMLMappingConfig(
        @Value("${ca3s.saml.roles.user:USER}") String[] rolesUserArr,
        @Value("${ca3s.saml.roles.domainra:DOMAIN_RA}") String[] rolesDomainRAArr,
        @Value("${ca3s.saml.roles.ra:RA}") String[] rolesRAArr,
        @Value("${ca3s.saml.roles.admin:ADMIN}") String[] rolesAdminArr,
        @Value("${ca3s.saml.attributes.firstName:firstName}") String[] attributesFirstName,
        @Value("${ca3s.saml.attributes.lastName:lastName}") String[] attributesLastName,
        @Value("${ca3s.saml.attributes.email:email}") String[] attributesEmail
    ) {
        this.rolesUserArr = rolesUserArr;
        this.rolesDomainRAArr = rolesDomainRAArr;
        this.rolesRAArr = rolesRAArr;
        this.rolesAdminArr = rolesAdminArr;
        this.attributesFirstName = attributesFirstName;
        this.attributesLastName = attributesLastName;
        this.attributesEmail = attributesEmail;
    }

    public String[] getRolesUserArr() {
        return rolesUserArr;
    }

    public String[] getRolesDomainRAArr() {
        return rolesDomainRAArr;
    }

    public String[] getRolesRAArr() {
        return rolesRAArr;
    }

    public String[] getRolesAdminArr() {
        return rolesAdminArr;
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
}
