package de.trustable.ca3s.core.security.provider;

import java.io.IOException;
import java.net.InetAddress;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.trustable.ca3s.cert.bundle.BundleFactory;
import de.trustable.ca3s.cert.bundle.KeyCertBundle;
import de.trustable.ca3s.core.domain.CAConnectorConfig;
import de.trustable.ca3s.core.domain.Certificate;
import de.trustable.ca3s.core.service.util.CaConnectorAdapter;
import de.trustable.ca3s.core.service.util.CertificateUtil;
import de.trustable.util.CryptoUtil;

public class Ca3sBundleFactory implements BundleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Ca3sBundleFactory.class);

	private CAConnectorConfig caConfigDao;

	private CaConnectorAdapter cacAdapt;

	private CertificateUtil certUtil;

	public Ca3sBundleFactory(CAConnectorConfig caConfigDao, CaConnectorAdapter cacAdapt, CertificateUtil certUtil) {
		this.caConfigDao = caConfigDao;
		this.cacAdapt = cacAdapt;
		this.certUtil = certUtil;
	}


	@Override
	public KeyCertBundle newKeyBundle(final String bundleName, long minValiditySeconds) throws GeneralSecurityException {


		KeyPair localKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		try {
			InetAddress ip = InetAddress.getLocalHost();
			String hostname = ip.getCanonicalHostName();
			X500Principal subject = new X500Principal("CN=" + hostname + ", OU=ca3s " + System.currentTimeMillis() + ", O=trustable solutione, C=DE");
			LOG.debug("requesting certificate for subject : " + subject.getName() );

			String csrBase64 = CryptoUtil.getCsrAsPEM(subject, localKeyPair.getPublic(), localKeyPair.getPrivate(), null);

			Certificate cert = cacAdapt.signCertificateRequest(csrBase64, caConfigDao);

			// build the chain
			X509Certificate[] certificateChain = certUtil.getX509CertificateChain(cert);

			LOG.debug("returning new certificate : " + certificateChain[0] );

			return new KeyCertBundle(bundleName, certificateChain, certificateChain[0], localKeyPair.getPrivate());

		} catch (IOException e) {
			// certificate creation failed with an exception not inheriting from 'GeneralSecurityException'
			throw new GeneralSecurityException(e);
		}

	}

}
