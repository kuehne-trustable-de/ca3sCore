package de.trustable.ca3s.core.acme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shredzone.acme4j.Identifier.TYPE_IP;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;

import de.trustable.ca3s.core.PreferenceTestConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Certificate;
import org.shredzone.acme4j.Identifier;
import org.shredzone.acme4j.Metadata;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.security.provider.Ca3sFallbackBundleFactory;
import de.trustable.ca3s.core.security.provider.Ca3sKeyManagerProvider;
import de.trustable.ca3s.core.security.provider.Ca3sKeyStoreProvider;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.util.JCAManager;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ACMEHappyPathIT {

    private static final Logger LOG = LoggerFactory.getLogger(ACMEHappyPathIT.class);

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
	String dirUrl;

    HttpChallengeHelper httpChallengeHelper;

    @Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

	@BeforeEach
	void init() {
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
		ptc.getInternalACMETestPipelineLaxRestrictions();

        prefTC.getTestUserPreference();

        int port = prefTC.getFreePort();
        LOG.info("http challenge on port {}", port);
        httpChallengeHelper = new HttpChallengeHelper(port);

	}


	@BeforeAll
	public static void setUpBeforeClass() {

		JCAManager.getInstance();

		TimedRenewalCertMap certMap = new TimedRenewalCertMap(null, new Ca3sFallbackBundleFactory("O=test trustable solutions, C=DE"));
		Security.addProvider(new Ca3sKeyStoreProvider(certMap, "ca3s"));
    	Security.addProvider(new Ca3sKeyManagerProvider(certMap));
    	new TimedRenewalCertMapHolder().setCertMap(certMap);
	}


	@Test
	public void testAccountHandling() throws AcmeException {

		System.out.println("connecting to " + dirUrl );
		Session session = new Session(dirUrl);
		Metadata meta = session.getMetadata();

		URI tos = meta.getTermsOfService();
		URL website = meta.getWebsite();
		LOG.debug("TermsOfService {}, website {}", tos, website);

		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

//		KeyPair accountKeyPair = KeyPairUtils.createECKeyPair("secp256r1");


		Account account = new AccountBuilder()
		        .addContact("mailto:acmeTest@ca3s.org")
		        .agreeToTermsOfService()
		        .useKeyPair(accountKeyPair)
		        .create(session);

		assertNotNull("created account MUST NOT be null", account);
		URL accountLocationUrl = account.getLocation();
		LOG.debug("accountLocationUrl {}", accountLocationUrl);


		Account retrievedAccount = new AccountBuilder()
		        .onlyExisting()         // Do not create a new account
		        .useKeyPair(accountKeyPair)
		        .create(session);

		assertNotNull("created account MUST NOT be null", retrievedAccount);
		assertEquals("expected to fimnd the smae account (URL)", accountLocationUrl, retrievedAccount.getLocation());

		account.modify()
	      .addContact("mailto:acmeHappyPathTest@ca3s.org")
	      .commit();

		KeyPair accountNewKeyPair = KeyPairUtils.createKeyPair(2048);

		account.changeKey(accountNewKeyPair);

		assertNotNull("account contacts MUST NOT be null", account.getContacts());

		KeyPair accountECKeyPair = KeyPairUtils.createECKeyPair("secp256r1");
		account.changeKey(accountECKeyPair);

		account.modify()
	      .addContact("mailto:acmeHappyPathECTest@ca3s.org")
	      .commit();

		assertEquals("three account contact expected", 3, account.getContacts().size());

		account.deactivate();

		assertEquals("account status 'deactivated' expected", AccountStatus.DEACTIVATED.toString().toLowerCase(), account.getStatus().toString().toLowerCase() );
	}

	@Test
	public void testOrderHandling() throws AcmeException, IOException, InterruptedException {

		System.out.println("connecting to " + dirUrl );
		Session session = new Session(dirUrl);
		Metadata meta = session.getMetadata();

		URI tos = meta.getTermsOfService();
		URL website = meta.getWebsite();
		LOG.debug("TermsOfService {}, website {}", tos, website);

		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

//		KeyPair accountKeyPair = KeyPairUtils.createECKeyPair("secp256r1");


		Account account = new AccountBuilder()
		        .addContact("mailto:acmeOrderTest@ca3s.org")
		        .agreeToTermsOfService()
		        .useKeyPair(accountKeyPair)
		        .create(session);

		Order order = account.newOrder()
		        .domains("localhost")
//		        .identifier(Identifier.ip(InetAddress.getByName("127.0.0.1")))
//		        .domains("localhost", "example.org", "www.example.org", "m.example.org")
//		        .identifier(Identifier.ip(InetAddress.getByName("192.168.56.10")))
		        .notAfter(Instant.now().plus(Duration.ofDays(20L)))
		        .create();


		for (Authorization auth : order.getAuthorizations()) {
			LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            String realmPart = "/" + PipelineTestConfiguration.ACME_REALM + "/";
            assertTrue( auth.getLocation().toString().contains(realmPart));

			if (auth.getStatus() == Status.PENDING) {

				Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

				if( challenge != null) {
                    Thread webThread = provideAuthEndpoint(challenge, order, prefTC);
                    challenge.trigger();
                    if( webThread != null){webThread.stop();}
                } else {
                    LOG.warn("http01 Challange not found for order");
                }
			}
		}

		KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

		CSRBuilder csrb = new CSRBuilder();
		csrb.addDomain("localhost");
//		csrb.addDomain("www.example.org");
//		csrb.addDomain("m.example.org");
		csrb.setOrganization("The Example Organization");
		csrb.sign(domainKeyPair);
		byte[] csr = csrb.getEncoded();

		for(Authorization auth: order.getAuthorizations()){
		    System.out.println( " ################ "  + auth.getIdentifier().toString() + "" + auth.getLocation() );
        }

		order.execute(csr);
		Certificate acmeCert = order.getCertificate();
		assertNotNull("Expected to receive a certificate", acmeCert);

		java.security.cert.X509Certificate x509Cert = acmeCert.getCertificate();
        assertNotNull("Expected to receive a x509Cert", x509Cert);
        assertNotNull("Expected the certificate to have a x509Cert", x509Cert.getSubjectDN().getName());

        account.update();
		Iterator<Order> orderIt = account.getOrders();
		assertTrue(orderIt.hasNext(), "Expected to find at least one order");

		for( int i = 0; i < 100; i++) {
			buildOrder(account, i);
		}

        account.update();
        Iterator<Order> orderIt2 = account.getOrders();
        assertNotNull("Expected to find at least one order", orderIt2.hasNext());
        System.out.println("Account orders : " + orderIt2);

/*
		for(int i = 0; orderIt2.hasNext(); i++) {
			Order orderRetrieved = orderIt2.next();
            System.out.println("order " + i + " : "+ orderRetrieved);
		}
*/
	}

	@Test
	public void testHTTPValidation() throws AcmeException, IOException, InterruptedException {

		System.out.println("connecting to " + dirUrl );
		Session session = new Session(dirUrl);
		Metadata meta = session.getMetadata();

		URI tos = meta.getTermsOfService();
		URL website = meta.getWebsite();
		LOG.debug("TermsOfService {}, website {}", tos, website);

		KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);


		Account account = new AccountBuilder()
		        .addContact("mailto:acmeOrderTest@ca3s.org")
		        .agreeToTermsOfService()
		        .useKeyPair(accountKeyPair)
		        .create(session);

		/*
		 * test with a domain name and an IP address
		 * and matching CSR
		 */
		{
			Order order = account.newOrder()
			        .domains("localhost")
			        .identifier(Identifier.ip(InetAddress.getByName("127.0.0.1")))
			        .notAfter(Instant.now().plus(Duration.ofDays(20L)))
			        .create();


			for (Authorization auth : order.getAuthorizations()) {
				LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
				if (auth.getStatus() == Status.PENDING) {

					Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

					int MAX_TRIAL = 10;
					for( int retry = 0; retry < MAX_TRIAL; retry++) {
						try {
                            provideAuthEndpoint(challenge, order, prefTC);
							break;
						} catch( BindException be) {
							System.out.println("bind exception, waiting for port to become available");
						}
						if( retry == MAX_TRIAL -1) {
							System.out.println("callback port not available");
						}
					}
					challenge.trigger();
				}
			}

			KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain("localhost");
			csrb.addDomain("127.0.0.1");
			csrb.setOrganization("The Example Organization");
			csrb.sign(domainKeyPair);
			byte[] csr = csrb.getEncoded();

			order.execute(csr);

			assertEquals("Expecting the finalize request to pass", Status.VALID, order.getStatus());

			Certificate acmeCert = order.getCertificate();
			assertNotNull("Expected to receive no certificate", acmeCert);
		}

		/*
		 * test with a domain name and an IP address
		 * and an additional IP in the CSR
		 */
		{
		Order order = account.newOrder()
            .domains("localhost")
            .identifier(new Identifier(TYPE_IP, "127.0.0.1"))
            .domains("localhost")
		        .notAfter(Instant.now().plus(Duration.ofDays(20L)))
		        .create();


		for (Authorization auth : order.getAuthorizations()) {
			LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
			if (auth.getStatus() == Status.PENDING) {

				Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

				int MAX_TRIAL = 10;
				for( int retry = 0; retry < MAX_TRIAL; retry++) {
					try {
                        provideAuthEndpoint(challenge, order, prefTC);
						break;
					} catch( BindException be) {
						System.out.println("bind exception, waiting for port to become available");
					}
					if( retry == MAX_TRIAL -1) {
						System.out.println("callback port not available");
					}
				}
				challenge.trigger();
			}
		}

		KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

		CSRBuilder csrb = new CSRBuilder();
		csrb.addDomain("localhost");
		csrb.addDomain("127.0.0.1");
		csrb.setOrganization("The Example Organization");
		csrb.sign(domainKeyPair);
		byte[] csr = csrb.getEncoded();

		order.execute(csr);

		assertEquals("Expecting the finalize request to succeed", Status.VALID, order.getStatus());

		Certificate acmeCert = order.getCertificate();
		assertNotNull("Expected to receive no certificate", acmeCert);
		}

        /*
         * test with an IP address only
         */
        {
            Order order = account.newOrder()
                .identifier(new Identifier(TYPE_IP, "127.0.0.1"))
                .notAfter(Instant.now().plus(Duration.ofDays(20L)))
                .create();


            for (Authorization auth : order.getAuthorizations()) {
                LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
                if (auth.getStatus() == Status.PENDING) {

                    Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

                    int MAX_TRIAL = 10;
                    for( int retry = 0; retry < MAX_TRIAL; retry++) {
                        try {
                            provideAuthEndpoint(challenge, order, prefTC);
                            break;
                        } catch( BindException be) {
                            System.out.println("bind exception, waiting for port to become available");
                        }
                        if( retry == MAX_TRIAL -1) {
                            System.out.println("callback port not available");
                        }
                    }
                    challenge.trigger();
                }
            }

            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("127.0.0.1");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();

            order.execute(csr);

            assertEquals("Expecting the finalize request to succeed", Status.VALID, order.getStatus());

            Certificate acmeCert = order.getCertificate();
            assertNotNull("Expected to receive no certificate", acmeCert);
        }


    }


	@Test
	public void testWinStore() throws AcmeException, IOException, GeneralSecurityException, InterruptedException {

		boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

		if( isWindows ) {
			System.out.println("connecting to " + dirUrl );
			Session session = new Session(dirUrl);
			Metadata meta = session.getMetadata();

			URI tos = meta.getTermsOfService();
			URL website = meta.getWebsite();
			LOG.debug("TermsOfService {}, website {}", tos, website);

			KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

			Account account = new AccountBuilder()
			        .addContact("mailto:acmeOrderWinStoreTest@ca3s.org")
			        .agreeToTermsOfService()
			        .useKeyPair(accountKeyPair)
			        .create(session);

			Order order = account.newOrder()
			        .domains("localhost")
	//		        .domains("WinStore.example.org")
	//		        .identifier(Identifier.ip(InetAddress.getByName("192.168.56.20")))
			        .notAfter(Instant.now().plus(Duration.ofDays(20L)))
			        .create();

			for (Authorization auth : order.getAuthorizations()) {
				if (auth.getStatus() == Status.PENDING) {
					LOG.debug("auth {}", auth);
					Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

					provideAuthEndpoint(challenge, order, prefTC);

					challenge.trigger();
				}
			}

		    KeyStore keyStore = KeyStore.getInstance("Windows-MY");
		    keyStore.load(null, null);  // Load keystore

			KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain("localhost");
//			csrb.addDomain("WinStore.example.org");
			csrb.setOrganization("The Example Organization' windows client");
			csrb.sign(domainKeyPair);
			byte[] csr = csrb.getEncoded();

			order.execute(csr);
			Certificate acmeCert = order.getCertificate();
			assertNotNull("Expected to receive a certificate", acmeCert);

			java.security.cert.X509Certificate x509Cert = acmeCert.getCertificate();
			assertNotNull("Expected to receive a x509Cert", x509Cert);

			X509Certificate[] chain = new X509Certificate[acmeCert.getCertificateChain().size()];
			acmeCert.getCertificateChain().toArray(chain);
		    keyStore.setKeyEntry("acmeKey", domainKeyPair.getPrivate(), null, chain);

		    keyStore.store(null, null);
		}
	}


	void buildOrder(Account account, int n) throws AcmeException {
		account.newOrder()
	        .domains("example-"+n+".org")
	        .notAfter(Instant.now().plus(Duration.ofDays(20L)))
	        .create();
	}


    Thread provideAuthEndpoint(final Http01Challenge challenge, Order order, PreferenceTestConfiguration prefTC) throws IOException, InterruptedException {
        Thread webThread = null;
        int MAX_TRIAL = 10;
		for( int retry = 0; retry < MAX_TRIAL; retry++) {
			try {
//				provideAuthEndpoint(challenge, prefTC);
                webThread = httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), true);
				break;
			} catch( BindException be) {
				System.out.println("bind exception, waiting for port to become available");
			}
			if( retry == MAX_TRIAL -1) {
				System.out.println("callback port not available");
			}
		}
		return webThread;
	}
}
