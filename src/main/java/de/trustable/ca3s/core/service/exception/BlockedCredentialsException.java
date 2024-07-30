package de.trustable.ca3s.core.service.exception;

import org.springframework.security.authentication.BadCredentialsException;

import java.time.Instant;

public class BlockedCredentialsException extends BadCredentialsException {

    private Instant blockedUntilDate = null;

    public BlockedCredentialsException(String msg) {
        super(msg);
    }

    public BlockedCredentialsException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public BlockedCredentialsException(String msg, Instant blockedUntilDate) {
        super(msg);
        this.blockedUntilDate = blockedUntilDate;
    }

    public Instant getBlockedUntilDate() {
        return blockedUntilDate;
    }
}
