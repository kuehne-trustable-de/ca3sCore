package de.trustable.ca3s.core.exception;

public class UserConnectsOnInappropriateEndpointException extends RuntimeException{

    public UserConnectsOnInappropriateEndpointException() {}

    public UserConnectsOnInappropriateEndpointException(String msg) {
        super(msg);
    }
}
