package de.trustable.ca3s.core.config.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OIDCMappingConfig {

    private final String[] rolesUserArr;
    private final String[] rolesDomainRAArr;
    private final String[] rolesRAArr;
    private final String[] rolesAdminArr;
    final private String[] rolesOtherArr;
    private final String[] attributesFirstName;
    private final String[] attributesLastName;
    private final String[] attributesEmail;
    private final String[] attributesTenant;
    private final String[] attributesLanguage;

    private final String exprFirstName;
    private final String exprLastName;
    private final String exprEmail;
    private final String exprTenant;
    private final String exprLanguage;
    private final String exprRolesOther;

    public OIDCMappingConfig(
        @Value("${ca3s.oidc.roles.user:USER}") String[] rolesUserArr,
        @Value("${ca3s.oidc.roles.domainra:}") String[] rolesDomainRAArr,
        @Value("${ca3s.oidc.roles.ra:}") String[] rolesRAArr,
        @Value("${ca3s.oidc.roles.admin:}") String[] rolesAdminArr,
        @Value("${ca3s.oidc.roles.other:}") String[] rolesOtherArr,
        @Value("${ca3s.oidc.attributes.firstName:given_name}") String[] attributesFirstName,
        @Value("${ca3s.oidc.attributes.lastName:family_name}") String[] attributesLastName,
        @Value("${ca3s.oidc.attributes.email:email}") String[] attributesEmail,
        @Value("${ca3s.oidc.attributes.tenant:}") String[] attributesTenant,
        @Value("${ca3s.oidc.attributes.language:}") String[] attributesLanguage,
        @Value("${ca3s.oidc.expression.firstName:}") String exprFirstName,
        @Value("${ca3s.oidc.expression.lastName:}") String exprLastName,
        @Value("${ca3s.oidc.expression.email:}") String exprEmail,
        @Value("${ca3s.oidc.expression.tenant:}") String exprTenant,
        @Value("${ca3s.oidc.expression.language:}") String exprLanguage,
        @Value("${ca3s.oidc.expression.roles.other:}") String exprRolesOther
        ) {

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
        this.exprFirstName = exprFirstName;
        this.exprLastName = exprLastName;
        this.exprEmail = exprEmail;
        this.exprTenant = exprTenant;
        this.exprLanguage = exprLanguage;
        this.exprRolesOther = exprRolesOther;
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

    public String[] getRolesOtherArr() {
        return rolesOtherArr;
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
    public String[] getAttributesLanguage() {return attributesLanguage;}

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
    public String getExprLanguage() {
        return exprLanguage;
    }

    public String getExprRolesOther() {
        return exprRolesOther;
    }
}
