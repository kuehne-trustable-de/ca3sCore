package de.trustable.ca3s.core.exception;

public class CsrCertificateAuthorizationMismatch extends RuntimeException{

    public CsrCertificateAuthorizationMismatch() {}

    public CsrCertificateAuthorizationMismatch(String msg) {
        super(msg);
    }
}
