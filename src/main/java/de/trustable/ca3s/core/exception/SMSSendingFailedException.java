package de.trustable.ca3s.core.exception;

public class SMSSendingFailedException extends RuntimeException{

    public SMSSendingFailedException(String msg) {
        super(msg);
    }
}
