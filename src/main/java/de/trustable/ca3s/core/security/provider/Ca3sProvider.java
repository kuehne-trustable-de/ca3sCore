package de.trustable.ca3s.core.security.provider;

import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.ca3s.cert.bundle.KeyStoreImpl;

public class Ca3sProvider extends Provider {

	public static final String SERVICE_NAME = "ca3s";
	private static final String STORE_TYPE_KEYSTORE = "Keystore";

	/**
	 * 
	 */
	private static final long serialVersionUID = -2476288508778039686L;

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sProvider.class);

	public Ca3sProvider() {
		super("Ca3sProvider", 1.0, "Certificate provider implemented by ca3s");
		
		super.put("Keystore.ca3s", KeyStoreImpl.class.getName());
		super.put("Keystore.ca3s storetype", "ca3s");

//		putService( new ProviderService(this, STORE_TYPE_KEYSTORE, SERVICE_NAME, AcmeKeyStoreImpl.class.getName()));
		
		LOG.debug("registered KeyStoreImpl in Ca3sProvider");
		
		for( String prop: super.stringPropertyNames()){
			LOG.debug("provider attribute {} : '{}'", prop, this.getProperty(prop));
		}

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
						return new KeyStoreImpl(new Ca3sBundleFactory());
					}
				}
			}catch(Exception ex ) {
				throw new NoSuchAlgorithmException("Error constructing " + type + " for " + algo + "using Ca3sProvider ");
			}
			throw new NoSuchAlgorithmException("No impl for " + type + " / " + algo );
		}
	}
}
