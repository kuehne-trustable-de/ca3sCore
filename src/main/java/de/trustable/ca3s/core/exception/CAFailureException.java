package de.trustable.ca3s.core.exception;

public class CAFailureException extends RuntimeException{

    public CAFailureException() {}

    public CAFailureException(String msg) {
        super(msg);
    }
}
