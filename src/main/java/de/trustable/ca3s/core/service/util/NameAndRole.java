package de.trustable.ca3s.core.service.util;

public class NameAndRole {

    private String name;
    private String role;

    public NameAndRole(final String name, final String role){
        this.name = name;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }
}
