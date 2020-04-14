package de.trustable.ca3s.core.service.scep;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.security.auth.x500.X500Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.jscep.client.Client;
import org.jscep.client.ClientException;
import org.jscep.client.EnrollmentResponse;
import org.jscep.client.verification.CertificateVerifier;
import org.jscep.transaction.TransactionException;

import de.trustable.ca3s.core.test.util.AcceptAllCertSelector;
import de.trustable.ca3s.core.test.util.AcceptAllVerifier;
import de.trustable.ca3s.core.test.util.X509Certificates;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;

public class SCEPTestClient {

	public static final Logger LOG = LogManager.getLogger(SCEPTestClient.class);

	public static void main(String[] args) throws GeneralSecurityException, IOException, ClientException, TransactionException {

		char[] password = "password".toCharArray();
		
		CertificateVerifier acceptAllVerifier = new AcceptAllVerifier();


		JCAManager.getInstance();

		KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		X500Principal enrollingPrincipal = new X500Principal("CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable Ltd,C=DE");

		X509Certificate ephemeralCert = X509Certificates.createEphemeral(enrollingPrincipal, keyPair);


//		URL serverUrl = new URL("http://localhost:8080/ca3sScep/test");
		URL serverUrl = new URL("http://localhost:8080/ca3sScep/ejbca");
		LOG.debug("scep serverUrl : " + serverUrl.toString());

		Client client = new Client(serverUrl, acceptAllVerifier);

		LOG.info("ephemeralCert : " + ephemeralCert);

		PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal, 
				keyPair.getPublic(), 
				keyPair.getPrivate(),
				password);

		EnrollmentResponse resp = client.enrol(ephemeralCert, keyPair.getPrivate(), csr);
		assertNotNull(resp);
		assertFalse(resp.isFailure());
		if (resp.isSuccess()) {

			CertStore certStore = resp.getCertStore();
			Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());

			for (Certificate cert : collCerts) {
				LOG.info("returned certificate : " + cert.toString());
			}

		}

	}

}
