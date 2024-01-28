package de.trustable.ca3s.core.exception;

public class TenantNotFoundException extends RuntimeException{

    public TenantNotFoundException() {}

    public TenantNotFoundException(String msg) {
        super(msg);
    }
}
