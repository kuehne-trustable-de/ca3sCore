package de.trustable.ca3s.core.security.provider;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.ca3s.cert.bundle.KeyStoreImpl;
import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;

public class Ca3sKeyStoreProvider extends Provider {

	public static final String SERVICE_NAME = "ca3s";
	private static final String STORE_TYPE_KEYSTORE = "Keystore";

	/**
	 * 
	 */
	private static final long serialVersionUID = -2476288508778039686L;

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sKeyStoreProvider.class);

    private static KeyStoreImpl keystoreImpl;

	@SuppressWarnings("deprecation")
	public Ca3sKeyStoreProvider(final TimedRenewalCertMap certMap, final String alias) {
		super("Ca3sKeyStoreProvider", 1.0, "Certificate provider implemented by ca3s");
		
		keystoreImpl = new KeyStoreImpl(certMap, alias);
		keystoreImpl.engineGetCertificate(alias);

//		super.put("Keystore.ca3s", KeyStoreImpl.class.getName());
//		super.put("Keystore.ca3s storetype", SERVICE_NAME);

		putService( new ProviderService(this, STORE_TYPE_KEYSTORE, SERVICE_NAME, KeyStoreImpl.class.getName()));
		
		LOG.debug("registered KeyStoreImpl in Ca3sKeyStoreProvider");
		
		for( String prop: super.stringPropertyNames()){
			LOG.debug("provider attribute {} : '{}'", prop, this.getProperty(prop));
		}

	}

	
    public static KeyStoreImpl getKeystoreImpl() { 
    	return keystoreImpl; 
    }

	private static final class ProviderService extends Provider.Service{
		ProviderService( Provider p, String type, String algo, String cn){
			super(p, type, algo, cn, null, null); 
		}
		
		@Override
		public Object newInstance(Object ctrParamObj) throws NoSuchAlgorithmException{
			
			String type = getType();
			String algo = getAlgorithm();
			
			try {
				if( STORE_TYPE_KEYSTORE.equalsIgnoreCase(type)) {
					if( SERVICE_NAME.equalsIgnoreCase(algo)) {
						LOG.debug("creating KeyStoreImpl with a Ca3sBundleFactory instance for type '{}' / algo '{}'", type, algo);

//						keystoreImpl.engineGetCertificate("ca3s_https");
						return keystoreImpl;
					}
				}
			}catch(Exception ex ) {
				LOG.error("exception while provider instantiation", ex );
				throw new NoSuchAlgorithmException("Error constructing provider type '" + type + "' for algo '" + algo + "' using Ca3sKeyStoreProvider ");

			}
			throw new NoSuchAlgorithmException("No impl for " + type + " / " + algo );
		}
	}
}
