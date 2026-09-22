package de.trustable.ca3s.core.service.exception;

public class CertificateAlreadyExistsException extends RuntimeException {
    public CertificateAlreadyExistsException(String message) {
        super(message);
    }
}
