package de.trustable.ca3s.core.exception;

public class InvalidCsrException extends RuntimeException{

    public InvalidCsrException() {}

    public InvalidCsrException(String msg) {
        super(msg);
    }
}
