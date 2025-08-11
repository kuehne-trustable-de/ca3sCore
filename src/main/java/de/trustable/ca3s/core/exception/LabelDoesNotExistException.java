package de.trustable.ca3s.core.exception;

public class LabelDoesNotExistException extends RuntimeException{

    public LabelDoesNotExistException() {}

    public LabelDoesNotExistException(String msg) {
        super(msg);
    }
}
