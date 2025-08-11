package de.trustable.ca3s.core.exception;

public class UserCredentialsMissingException extends RuntimeException{

    public UserCredentialsMissingException() {}

    public UserCredentialsMissingException(String msg) {
        super(msg);
    }
}
