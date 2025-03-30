package de.trustable.ca3s.core.service.exception;

public class InvalidCredentialException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidCredentialException(final String message) {
        super(message);
    }
}
