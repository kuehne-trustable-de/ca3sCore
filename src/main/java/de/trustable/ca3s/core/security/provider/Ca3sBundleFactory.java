package de.trustable.ca3s.core.security.provider;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x500.X500Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.util.CryptoUtil;

public class Ca3sBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sBundleFactory.class);

	private X500Name x500Issuer;

	private CryptoUtil cryptoUtil = new CryptoUtil();

	private CAConnectorConfig caConfigDao;
	

	public Ca3sBundleFactory(CAConnectorConfig caConfigDao) {
		this.caConfigDao = caConfigDao;
	}
	
	
	@Override
	public KeyCertBundle newKeyBundle(String bundleName) throws GeneralSecurityException {
		
		
		KeyPair localKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
		
		try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getCanonicalHostName();
			LOG.debug("requesting certificate for host : " + hostname );
			X500Principal subject = new X500Principal("CN=" + InetAddress.getLocalHost().getCanonicalHostName() + ", OU=ca3s " + System.currentTimeMillis() + ", O=trustable Ltd, C=DE");

			String csr = CryptoUtil.getCsrAsPEM(subject, localKeyPair.getPublic(), localKeyPair.getPrivate(), null);
			
/*			
			if (CAConnectorType.Adcs.equals(caConfigDao.getCaConnectorType())) {
				LOG.debug("CAConnectorType ADCS at " + caConfigDao.getCaUrl());
				
				Certificate  cert = adcsController.signCertificateRequest(csr, caConfigDao);
			}
			

			// build the (short) chain
			X509Certificate[] certificateChain = {issuedCertificate, getRootCertificate()};

			LOG.debug("returning new  certificate : " + issuedCertificate );

			return new KeyCertBundle(bundleName, certificateChain, issuedCertificate, localKeyPair.getPrivate());
*/
			return null;
		} catch (IOException e) {
			// certificate creation failed with an exception not inheriting from 'GeneralSecurityException'
			throw new GeneralSecurityException(e);
		}

	}

}
