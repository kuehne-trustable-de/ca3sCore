package de.trustable.ca3s.core.security.provider;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;


public final class TomcatTrustManager implements X509TrustManager {

    private static volatile X509TrustManager delegate;

    public TomcatTrustManager() {
        // Required by Tomcat.
    }

    public static void setDelegate(X509TrustManager trustManager) {
        if (trustManager == null) {
            throw new IllegalArgumentException("trustManager must not be null");
        }
        delegate = trustManager;
    }

    private static X509TrustManager delegate() {
        X509TrustManager tm = delegate;
        if (tm == null) {
            throw new IllegalStateException("TomcatTrustManager has not been initialized");
        }
        return tm;
    }

    @Override
    public void checkClientTrusted(
        X509Certificate[] chain,
        String authType) throws java.security.cert.CertificateException {

        delegate().checkClientTrusted(chain, authType);
    }

    @Override
    public void checkServerTrusted(
        X509Certificate[] chain,
        String authType) throws java.security.cert.CertificateException {

        delegate().checkServerTrusted(chain, authType);
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return delegate().getAcceptedIssuers();
    }
}
