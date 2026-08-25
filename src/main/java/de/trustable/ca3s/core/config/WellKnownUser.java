package de.trustable.ca3s.core.config;

/**
 * Application constants.
 */
public enum WellKnownUser {

    SYSTEM_USER("system"),
    ANONYMOUS_USER("anonymousUser");

    public final String label;

    private WellKnownUser(String label) {
        this.label = label;
    }
}
