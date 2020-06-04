package de.trustable.ca3s.core.service.scep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
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
import org.jscep.transaction.FailInfo;
import org.jscep.transaction.TransactionException;
import org.jscep.transport.response.Capabilities;
import org.jscep.transport.response.Capability;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.test.util.AcceptAllCertSelector;
import de.trustable.ca3s.core.test.util.AcceptAllVerifier;
import de.trustable.ca3s.core.test.util.X509Certificates;
import de.trustable.util.CryptoUtil;
import de.trustable.util.JCAManager;


//@Disabled("Integration test fails for unknown reason, running it as a separate client succeeds. Maybe a classloader issue? ")
@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ScepServiceIT{

	public static final Logger LOG = LogManager.getLogger(ScepServiceIT.class);

	@LocalServerPort
	int serverPort; // random port chosen by spring test
 
	static KeyPair keyPair;
	static X509Certificate ephemeralCert;
	static X500Principal enrollingPrincipal; 
	static char[] password = "password".toCharArray();

	@Autowired
	PipelineTestConfiguration ptc;
	
	
	CertificateVerifier acceptAllVerifier = new AcceptAllVerifier();

	Client client;
	Client client1CN;

	
	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		JCAManager.getInstance();

		keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

		enrollingPrincipal = new X500Principal("CN=SCEPRequested_" + System.currentTimeMillis() + ",O=trustable Ltd,C=DE");

		ephemeralCert = X509Certificates.createEphemeral(enrollingPrincipal, keyPair);

	}

	
	@BeforeEach
	public void setUp() throws MalformedURLException {

		ptc.getInternalSCEPTestPipelineLaxRestrictions();
		ptc.getInternalSCEPTestPipelineCN1Restrictions();

		URL serverUrl = new URL("http://localhost:" + serverPort + "/ca3sScep/" + PipelineTestConfiguration.SCEP_REALM);
		LOG.debug("scep serverUrl : " + serverUrl.toString());

		client = new Client(serverUrl, acceptAllVerifier);


		URL serverUrl1CN = new URL("http://localhost:" + serverPort + "/ca3sScep/" + PipelineTestConfiguration.SCEP1CN_REALM);
		LOG.debug("scep serverUrl1CN : " + serverUrl1CN.toString());

		client1CN = new Client(serverUrl1CN, acceptAllVerifier);

	}

	@Test
	public void testScepEnrol() throws GeneralSecurityException, IOException, ClientException, TransactionException {


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
				LOG.debug("returned certificate : " + cert.toString());
			}

		}

	}

	@Test
	public void testScepEnrolRejected() throws GeneralSecurityException, IOException, ClientException, TransactionException {


		PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal, 
				keyPair.getPublic(), 
				keyPair.getPrivate(),
				password);

		EnrollmentResponse resp = client1CN.enrol(ephemeralCert, keyPair.getPrivate(), csr);
		assertNotNull(resp);
		assertTrue(resp.isFailure());
		
		assertEquals("Expecting FailInfo.badRequest", FailInfo.badRequest, resp.getFailInfo());
		
	}

	
	
	@Test
	public void testScepCapabilities() throws MalformedURLException {

		// Invoke operations on the client.
		Capabilities caps = client.getCaCapabilities();
		assertNotNull(caps);
		LOG.debug("caps : " + caps);
		assertTrue("Expecting support for SHA-256", caps.contains(Capability.SHA_256));
	}

	@Test
	public void testGetCaCertificate() throws MalformedURLException, ClientException, CertStoreException {

		CertStore certStore = client.getCaCertificate();

		assertNotNull(certStore);
		Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());

		assertTrue("Expecting getCaCertificate to return a certificate", !collCerts.isEmpty());
		Certificate caCert = collCerts.iterator().next();
		LOG.debug("caCert : " + caCert.toString());
		LOG.debug("collCerts.size() : " + collCerts.size());
	}
	
	@Test
	public void testEnrollThenGetCertificate() throws ClientException, GeneralSecurityException, IOException, TransactionException {

		PKCS10CertificationRequest csr = CryptoUtil.getCsr(enrollingPrincipal, 
				keyPair.getPublic(), 
				keyPair.getPrivate(),
				password);

        EnrollmentResponse response = client.enrol(
                ephemeralCert,
                keyPair.getPrivate(),
                csr);
        
        X509Certificate issued = (X509Certificate) response.getCertStore().getCertificates(null).iterator().next();
        
        Certificate retrieved = client.getCertificate(ephemeralCert, 
        		keyPair.getPrivate(),
                issued.getSerialNumber()).getCertificates(null).iterator().next();

        assertEquals(issued, retrieved);
	}

	@Test
	public void testGetRolloverCertificate() throws MalformedURLException, ClientException, CertStoreException {

		Capabilities caps = client.getCaCapabilities();
		boolean hasNextCA = caps.contains(Capability.GET_NEXT_CA_CERT);

		try{
			CertStore certStore = client.getRolloverCertificate();
	
			assertTrue("only when the capabilities contain the GET_NEXT_CA_CERT bit ...", hasNextCA);
			
			assertNotNull(certStore);
			Collection<? extends Certificate> collCerts = certStore.getCertificates(new AcceptAllCertSelector());
	
			assertTrue("Expecting getRolloverCertificate to return a certificate", !collCerts.isEmpty());
			Certificate caCert = collCerts.iterator().next();
			LOG.debug("caCert : " + caCert.toString());
			LOG.debug("collCerts.size() : " + collCerts.size());
			
		} catch( UnsupportedOperationException uoe){
			assertFalse("expected when the capabilities do NOT contain the GET_NEXT_CA_CERT bit ...", hasNextCA);
		}
	}

}
