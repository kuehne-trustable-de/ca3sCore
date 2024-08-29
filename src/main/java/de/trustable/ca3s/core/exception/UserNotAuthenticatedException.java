package de.trustable.ca3s.core.exception;

public class UserNotAuthenticatedException extends RuntimeException{

    public UserNotAuthenticatedException() {}

    public UserNotAuthenticatedException(String msg) {
        super(msg);
    }
}
