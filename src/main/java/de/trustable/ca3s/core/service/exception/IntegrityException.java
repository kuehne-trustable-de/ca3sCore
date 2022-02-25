package de.trustable.ca3s.core.service.exception;

public class IntegrityException extends RuntimeException{

    public IntegrityException() {}

    public IntegrityException(String msg) {
        super(msg);
    }
}
