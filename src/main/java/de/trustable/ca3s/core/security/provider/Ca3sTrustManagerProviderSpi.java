package de.trustable.ca3s.core.security.provider;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.InvalidAlgorithmParameterException;

public final class Ca3sTrustManagerProviderSpi
    extends TrustManagerFactorySpi {

    private final Ca3sTrustManager ca3sTrustManager;

    public Ca3sTrustManagerProviderSpi(
        Ca3sTrustManager ca3sTrustManager) {

        this.ca3sTrustManager = ca3sTrustManager;
    }

    @Override
    protected void engineInit(KeyStore keyStore)
        throws KeyStoreException {

        /*
         * The wrapped trust manager is already initialized.
         *
         * Ignore the KeyStore supplied to:
         *
         *     TrustManagerFactory.init(KeyStore)
         */
    }

    @Override
    protected void engineInit(
        ManagerFactoryParameters params)
        throws InvalidAlgorithmParameterException {

        /*
         * The wrapped trust manager is already initialized.
         */
    }

    @Override
    protected TrustManager[] engineGetTrustManagers() {

        return new TrustManager[] {
            ca3sTrustManager
        };
    }
}
