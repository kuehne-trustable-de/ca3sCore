package de.trustable.ca3s.core.security;

import org.springframework.security.core.AuthenticationException;

/**
 * This exception is thrown in case of a not activated user trying to authenticate.
 */
public class UserBlockedException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public UserBlockedException(String message) {
        super(message);
    }

    public UserBlockedException(String message, Throwable t) {
        super(message, t);
    }
}
