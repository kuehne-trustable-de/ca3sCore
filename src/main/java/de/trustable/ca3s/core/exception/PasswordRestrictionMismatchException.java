package de.trustable.ca3s.core.exception;

public class PasswordRestrictionMismatchException extends RuntimeException{

    public PasswordRestrictionMismatchException(String msg) {
        super(msg);
    }
}
