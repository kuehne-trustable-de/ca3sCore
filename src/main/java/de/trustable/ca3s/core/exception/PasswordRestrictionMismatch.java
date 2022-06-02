package de.trustable.ca3s.core.exception;

public class PasswordRestrictionMismatch extends RuntimeException{

    public PasswordRestrictionMismatch(String msg) {
        super(msg);
    }
}
