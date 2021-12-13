package de.trustable.ca3s.core.security;

import java.io.Serializable;

/**
 * {
 *    "sub":"95d3f753-ca88-4eb1-9c67-d3766b4e7bff",
 *    "email_verified":false,
 *    "roles":[
 *       "offline_access",
 *       "uma_authorization",
 *       "default-roles-ca3srealm",
 *       "user"
 *    ],
 *    "name":"User",
 *    "preferred_username":"kcuser",
 *    "given_name":"User",
 *    "family_name":"",
 *    "email":"user@trustable.de"
 * }
 */
public class KeycloakUserDetails implements Serializable {
    private String sub;
    private String[] roles;
    private String name;
    private String preferred_username;
    private String given_name;
    private String family_name;
    private String email;
    private boolean email_verified;

    public KeycloakUserDetails(){}

    public String getSub() {
        return sub;
    }

    public void setSub(String sub) {
        this.sub = sub;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferred_username() {
        return preferred_username;
    }

    public void setPreferred_username(String preferred_username) {
        this.preferred_username = preferred_username;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmail_verified() {
        return email_verified;
    }

    public void setEmail_verified(boolean email_verified) {
        this.email_verified = email_verified;
    }
}
