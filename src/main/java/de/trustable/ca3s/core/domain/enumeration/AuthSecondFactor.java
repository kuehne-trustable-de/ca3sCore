package de.trustable.ca3s.core.domain.enumeration;

public enum AuthSecondFactor {
    NONE,
    CLIENT_CERT,
    TOTP,
    EMAIL,
    SMS
}
