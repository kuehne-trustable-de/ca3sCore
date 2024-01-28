package de.trustable.ca3s.core.exception;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException() {}

    public UserNotFoundException(String msg) {
        super(msg);
    }
}
