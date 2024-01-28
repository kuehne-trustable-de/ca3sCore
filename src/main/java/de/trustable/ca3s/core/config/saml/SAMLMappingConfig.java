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
    private final String[] attributesTenant;

    private final String exprFirstName;
    private final String exprLastName;
    private final String exprEmail;
    private final String exprTenant;

    public SAMLMappingConfig(
        @Value("${ca3s.saml.roles.user:USER}") String[] rolesUserArr,
        @Value("${ca3s.saml.roles.domainra:}") String[] rolesDomainRAArr,
        @Value("${ca3s.saml.roles.ra:}") String[] rolesRAArr,
        @Value("${ca3s.saml.roles.admin:}") String[] rolesAdminArr,
        @Value("${ca3s.saml.attributes.firstName:firstName}") String[] attributesFirstName,
        @Value("${ca3s.saml.attributes.lastName:lastName}") String[] attributesLastName,
        @Value("${ca3s.saml.attributes.email:email}") String[] attributesEmail,
        @Value("${ca3s.saml.attributes.tenant:}") String[] attributesTenant,
        @Value("${ca3s.saml.expression.firstName:}") String exprFirstName,
        @Value("${ca3s.saml.expression.lastName:}") String exprLastName,
        @Value("${ca3s.saml.expression.email:}") String exprEmail,
        @Value("${ca3s.saml.expression.tenant:}") String exprTenant) {

        this.rolesUserArr = rolesUserArr;
        this.rolesDomainRAArr = rolesDomainRAArr;
        this.rolesRAArr = rolesRAArr;
        this.rolesAdminArr = rolesAdminArr;
        this.attributesFirstName = attributesFirstName;
        this.attributesLastName = attributesLastName;
        this.attributesEmail = attributesEmail;
        this.attributesTenant = attributesTenant;
        this.exprFirstName = exprFirstName;
        this.exprLastName = exprLastName;
        this.exprEmail = exprEmail;
        this.exprTenant = exprTenant;
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
    public String[] getAttributesTenant() {return attributesTenant;}

    public String getExprFirstName() {
        return exprFirstName;
    }

    public String getExprLastName() {
        return exprLastName;
    }

    public String getExprEmail() {
        return exprEmail;
    }

    public String getExprTenant() {
        return exprTenant;
    }
}
