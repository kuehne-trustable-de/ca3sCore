package de.trustable.ca3s.core.security.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Provider;

public class Ca3sTrustManagerProvider extends Provider {

	public static final String SERVICE_NAME = "ca3sTrustAlgo";
	private static final String PROVIDER_TYPE_TRUSTMANAGER = "TrustManagerFactory";

	/**
	 *
	 */
	private static final long serialVersionUID = -2476359432739686L;

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sTrustManagerProvider.class);


	@SuppressWarnings("deprecation")
	public Ca3sTrustManagerProvider(final Ca3sTrustManagerProviderSpi ca3sTrustManagerProviderSpi) {
		super("Ca3sTrustManagerProvider", 1.0, "Trust management provider implemented by ca3s");

        putService(new Service(
            this,
            PROVIDER_TYPE_TRUSTMANAGER,
            SERVICE_NAME,
            ca3sTrustManagerProviderSpi.getClass().getName(),
            null,
            null
        ));

		LOG.debug("registered Ca3sTrustManagerProviderSpi in Ca3sTrustManagerProvider");

		for( String prop: super.stringPropertyNames()){
			LOG.debug("provider attribute {} : '{}'", prop, this.getProperty(prop));
		}

	}
}
