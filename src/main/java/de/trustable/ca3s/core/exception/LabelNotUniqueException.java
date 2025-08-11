package de.trustable.ca3s.core.exception;

public class LabelNotUniqueException extends RuntimeException{

    public LabelNotUniqueException() {}

    public LabelNotUniqueException(String msg) {
        super(msg);
    }
}
