package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.cert.bundle.TimedRenewalCertMap;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.domain.AcmeAccount;
import de.trustable.ca3s.core.domain.enumeration.AccountStatus;
import de.trustable.ca3s.core.repository.AcmeAccountRepository;
import de.trustable.ca3s.core.security.provider.Ca3sFallbackBundleFactory;
import de.trustable.ca3s.core.security.provider.Ca3sKeyManagerProvider;
import de.trustable.ca3s.core.security.provider.Ca3sKeyStoreProvider;
import de.trustable.ca3s.core.security.provider.TimedRenewalCertMapHolder;
import de.trustable.ca3s.core.service.util.JwtUtil;
import de.trustable.ca3s.core.service.util.KeyUtil;
import de.trustable.util.JCAManager;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeServerException;
import org.shredzone.acme4j.exception.AcmeUserActionRequiredException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

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
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.shredzone.acme4j.Identifier.TYPE_IP;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AcmeHappyPathIT {

    private static final Logger LOG = LoggerFactory.getLogger(AcmeHappyPathIT.class);

	@LocalServerPort
	int serverPort; // random port chosen by spring test

    final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
    final String ACME_DOMAIN_REUSE_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM_DOMAIN_REUSE + "/directory";
    final String ACME_DOMAIN_REUSE_WARN_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM_DOMAIN_REUSE_WARN + "/directory";
    final String ACME_KEY_UNIQUE_WARN_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM_KEY_UNIQUE_WARN + "/directory";
    final String ACME_PATH_PART_OTHER_REALM = "/acme/" + PipelineTestConfiguration.ACME1CN_REALM + "/directory";
    String dirUrl;
    String dirUrlDomainReuse;
    String dirUrlDomainReuseWarn;
    String dirUrlKeyUniqueWarn;
    String dirUrlOtherRealm;

    HttpChallengeHelper httpChallengeHelper;

    @Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @Autowired
    AcmeAccountRepository acctRepository;

    @Autowired
    JwtUtil jwtUtil;

    static KeyUtil keyUtil = new KeyUtil("RSA-4096");

	@BeforeEach
	void init() {
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
        dirUrlDomainReuse  = "http://localhost:" + serverPort + ACME_DOMAIN_REUSE_PATH_PART;
        dirUrlDomainReuseWarn  = "http://localhost:" + serverPort + ACME_DOMAIN_REUSE_WARN_PATH_PART;
        dirUrlKeyUniqueWarn  = "http://localhost:" + serverPort + ACME_KEY_UNIQUE_WARN_PATH_PART;

        dirUrlOtherRealm = "http://localhost:" + serverPort + ACME_PATH_PART_OTHER_REALM;
		ptc.getInternalACMETestPipelineLaxRestrictions();
        ptc.getInternalACMETestPipeline_1_CN_ONLY_Restrictions();
        ptc.getInternalACMETestPipelineLaxDomainReuseRestrictions();
        ptc.getInternalACMETestPipelineLaxDomainReuseWarnRestrictions();
        ptc.getInternalACMETestPipelineLaxWarnRestrictions();

        prefTC.getTestUserPreference();

        int port = prefTC.getFreePort();
        LOG.info("http challenge on port {}", port);
        httpChallengeHelper = new HttpChallengeHelper(port);
	}


	@BeforeAll
	public static void setUpBeforeClass() {

		JCAManager.getInstance();

		TimedRenewalCertMap certMap = new TimedRenewalCertMap(null, new Ca3sFallbackBundleFactory("O=test trustable solutions, C=DE", keyUtil));
		Security.addProvider(new Ca3sKeyStoreProvider(certMap, "ca3s"));
    	Security.addProvider(new Ca3sKeyManagerProvider(certMap));
    	new TimedRenewalCertMapHolder().setCertMap(certMap);
	}


    @Test
    public void testToSHandling() throws AcmeException {

        System.out.println("connecting to " + dirUrlOtherRealm);
        Session session = new Session(dirUrlOtherRealm);
        Metadata meta = session.getMetadata();
        Assertions.assertNotNull(meta.getTermsOfService(), "Expecting a ToS URI to be present");
        Assertions.assertEquals("http://to.agreement.link/index.html", meta.getTermsOfService().toString());

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

        try {
            new AccountBuilder()
                .addContact("mailto:acmeTest@ca3s.org")
                .useKeyPair(accountKeyPair)
                .create(session);
            fail("ToS agreement is required, exception expected");
        }catch(AcmeUserActionRequiredException acmeUserActionRequiredException){
            // as expected
        }

        Account account = new AccountBuilder()
            .addContact("mailto:acmeTest@ca3s.org")
            .useKeyPair(accountKeyPair)
            .agreeToTermsOfService()
            .create(session);

        Assertions.assertNotNull(account, "Expecting an account to be returned");
    }

    @Test
    public void testAccountContactHandling() throws AcmeException, JoseException {

        System.out.println("connecting to " + dirUrl);
        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

        try {
            new AccountBuilder()
                .useKeyPair(accountKeyPair)
                .create(session);
            Assertions.fail("empty contact, exception expected");
        } catch( AcmeServerException ase) {
            Assertions.assertEquals("Contact matching regEx '^$' were rejected.", ase.getMessage());
        }

        // ensure account wasn't created
        List<AcmeAccount> accListExisting = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(accountKeyPair.getPublic()));
        Assertions.assertTrue(accListExisting.isEmpty(), "empty contact, account MUST NOT be created");

        Account account = new AccountBuilder()
                .addContact("mailto:acmeTest@ca3s.org")
                .useKeyPair(accountKeyPair)
                .create(session);

        Assertions.assertNotNull(account, "created account MUST NOT be null");

        accListExisting = acctRepository.findByPublicKeyHashBase64(jwtUtil.getJWKThumbPrint(accountKeyPair.getPublic()));
        Assertions.assertFalse(accListExisting.isEmpty(), "contact provided, account MUST be created");

        AcmeAccount acmeAccount = accListExisting.get(0);
        Assertions.assertEquals(1, acmeAccount.getContacts().size());

        account.modify().addEmail("additionalContact@ca3s.org").commit();
        Assertions.assertEquals(2, account.getContacts().size());

        account.modify().addEmail("additionalContact@ca3s.org").commit();
        Assertions.assertEquals(2, account.getContacts().size());

        // check contact restriction RegEx
        Session sessionContactRegEx = new Session(dirUrlOtherRealm);

        accountKeyPair = KeyPairUtils.createKeyPair(2048);

        String[] rejectedContactArr = new String[]{
            "mailto:root@localhost",
            "mailto:joe@servicedesk.com",
            "mailto:joe@servicedesk.org",
            "mailto:jim@127.0.0.1"
        };

        for(String contact: rejectedContactArr) {
            try {
                new AccountBuilder()
                    .useKeyPair(accountKeyPair)
                    .addContact(contact)
                    .agreeToTermsOfService()
                    .create(sessionContactRegEx);
                Assertions.fail("empty contact, exception expected");
            } catch (AcmeServerException ase) {
                assertEquals("Contact matching regEx '.*@localhost|.*@127.0.0.1|.*@servicedesk.*' were rejected.", ase.getMessage());
            }
        }

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
            .useKeyPair(accountKeyPair)
            .create(session);

        Assertions.assertNotNull(account, "created account MUST NOT be null");
		URL accountLocationUrl = account.getLocation();
		LOG.debug("accountLocationUrl {}", accountLocationUrl);


		Account retrievedAccount = new AccountBuilder()
		        .onlyExisting()         // Do not create a new account
		        .useKeyPair(accountKeyPair)
		        .create(session);

		Assertions.assertNotNull(retrievedAccount, "created account MUST NOT be null");
		Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to fimnd the smae account (URL)");

		account.modify()
	      .addContact("mailto:acmeHappyPathTest@ca3s.org")
	      .commit();

		KeyPair accountNewKeyPair = KeyPairUtils.createKeyPair(2048);

		account.changeKey(accountNewKeyPair);

		Assertions.assertNotNull(account.getContacts(), "account contacts MUST NOT be null");

		KeyPair accountECKeyPair = KeyPairUtils.createECKeyPair("secp256r1");
		account.changeKey(accountECKeyPair);

		account.modify()
	      .addContact("mailto:acmeHappyPathECTest@ca3s.org")
	      .commit();

		Assertions.assertEquals(3, account.getContacts().size(), "three account contact expected");

		account.deactivate();

		Assertions.assertEquals(AccountStatus.DEACTIVATED.toString().toLowerCase(), account.getStatus().toString().toLowerCase(), "account status 'deactivated' expected");
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
                    provideAuthEndpoint(challenge, order, prefTC);
                    challenge.trigger();

                } else {
                    LOG.warn("http01 Challenge not found for order");
                }
			}
		}

        {
            // This is not allowed !
            KeyPair domainKeyPair = accountKeyPair;

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("localhost");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();

            try {
                order.execute(csr);
                Assertions.fail("AcmeException due to restriction violation expected");
            }catch(AcmeServerException ase) {
                ase.printStackTrace();
                // as expected
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
		Assertions.assertNotNull(acmeCert, "Expected to receive a certificate");

		java.security.cert.X509Certificate x509Cert = acmeCert.getCertificate();
        Assertions.assertNotNull(x509Cert, "Expected to receive a x509Cert");
        Assertions.assertNotNull(x509Cert.getSubjectDN().getName(), "Expected the certificate to have a x509Cert");

		Iterator<Order> orderIt = account.getOrders();
		assertTrue(orderIt.hasNext(), "Expected to find at least one order");

		for( int i = 0; i < 100; i++) {
			buildOrder(account, i);
		}

        Iterator<Order> orderIt2 = account.getOrders();
        Assertions.assertNotNull(orderIt2.hasNext(), "Expected to find at least one order");
        System.out.println("Account orders : " + orderIt2);

/*
		for(int i = 0; orderIt2.hasNext(); i++) {
			Order orderRetrieved = orderIt2.next();
            System.out.println("order " + i + " : "+ orderRetrieved);
		}
*/
	}

	@Test
	public void testHTTPValidationAndRevocation() throws AcmeException, IOException, InterruptedException {

		System.out.println("connecting to " + dirUrl );
		Session session = new Session(dirUrl);
		Metadata meta = session.getMetadata();

		URI tos = meta.getTermsOfService();
		URL website = meta.getWebsite();
		LOG.debug("TermsOfService {}, website {}", tos, website);

        Session sessionDomainReuseRealm = new Session(dirUrlDomainReuse);
        KeyPair domainReuseRealmAccountKeyPair = KeyPairUtils.createKeyPair(2048);
        Account domainReuseRealmAccount = new AccountBuilder()
            .addContact("mailto:sessionDomainReuseRealm@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(domainReuseRealmAccountKeyPair)
            .create(sessionDomainReuseRealm);

        Session sessionDomainReuseWarnRealm = new Session(dirUrlDomainReuseWarn);
        KeyPair domainReuseWarnRealmAccountKeyPair = KeyPairUtils.createKeyPair(2048);
        Account domainReuseWarnRealmAccount = new AccountBuilder()
            .addContact("mailto:sessionDomainReuseWarnRealm@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(domainReuseWarnRealmAccountKeyPair)
            .create(sessionDomainReuseWarnRealm);

        Session sessionKeyUniqueWarnRealm = new Session(dirUrlKeyUniqueWarn);
        KeyPair keyUniqueWarnRealmAccountKeyPair = KeyPairUtils.createKeyPair(2048);
        Account keyUniqueWarnRealmAccount = new AccountBuilder()
            .addContact("mailto:sessionKeyUniqueWarnRealm@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(keyUniqueWarnRealmAccountKeyPair)
            .create(sessionKeyUniqueWarnRealm);


        System.out.println("connecting to " + dirUrlOtherRealm );
        Session sessionOtherRealm = new Session(dirUrlOtherRealm);


        KeyPair otherRealmAccountKeyPair = KeyPairUtils.createKeyPair(2048);
        Account otherRealmAccount = new AccountBuilder()
            .addContact("mailto:otherRealmy@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(otherRealmAccountKeyPair)
            .create(sessionOtherRealm);


        KeyPair dummyAccountKeyPair = KeyPairUtils.createKeyPair(2048);
        Account dummyAccount = new AccountBuilder()
            .addContact("mailto:acmeDummy@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(dummyAccountKeyPair)
            .create(session);


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


            resolveAuthorizations(order);

            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain("localhost");
			csrb.addDomain("127.0.0.1");
			csrb.setOrganization("The Example Organization");
			csrb.sign(domainKeyPair);
			byte[] csr = csrb.getEncoded();

			order.execute(csr);

			Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to pass");

			Certificate acmeCert = order.getCertificate();
			Assertions.assertNotNull(acmeCert, "Expected to receive no certificate");


            // try to revoke a certificate with a session from another realm
            try {
                Login login = sessionOtherRealm.login(otherRealmAccount.getLocation(), otherRealmAccountKeyPair);
                Certificate.revoke(login, acmeCert.getCertificate(), RevocationReason.KEY_COMPROMISE);
                Assertions.fail("certificate already revoked, exception expected");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("problem authenticating account / order / certificate for RevokeRequest", acmeServerException.getMessage());
            }

            // try to revoke a certificate not related to the given account
            try {
                Login login = session.login(dummyAccount.getLocation(), dummyAccountKeyPair);
                Certificate.revoke(login, acmeCert.getCertificate(), RevocationReason.KEY_COMPROMISE);
                Assertions.fail("certificate already revoked, exception expected");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("problem authenticating account / order / certificate for RevokeRequest", acmeServerException.getMessage());
            }

            // try to revoke a certificate but signing the request with an unrelated key
            KeyPair dummyKeyPair = KeyPairUtils.createKeyPair(2048);
            try {
                Certificate.revoke(session, dummyKeyPair, acmeCert.getCertificate(), RevocationReason.KEY_COMPROMISE);
                Assertions.fail("certificate revocation expected to fail, unrelated key used for request signing");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("Certificate revocation failed, neither KID nor JWK found in request", acmeServerException.getMessage());
            }

            // revoke without account authentication but with the certificate's private key
            Certificate.revoke(session, domainKeyPair, acmeCert.getCertificate(), RevocationReason.KEY_COMPROMISE);

            try {
                acmeCert.revoke(RevocationReason.CESSATION_OF_OPERATION);
                Assertions.fail("certificate already revoked, exception expected");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("certificate already revoked", acmeServerException.getMessage());
            }

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


            resolveAuthorizations(order);

            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("localhost");
            csrb.addDomain("127.0.0.1");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();

            order.execute(csr);

            Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to succeed");

            Certificate acmeCert = order.getCertificate();
            Assertions.assertNotNull(acmeCert, "Expected to receive no certificate");
		}


        KeyPair domainKeyPairForMultiUse = KeyPairUtils.createKeyPair(2048);

        /*
         * test with an IP address only
         */
        {
            Order order = account.newOrder()
                .identifier(new Identifier(TYPE_IP, "127.0.0.1"))
                .notAfter(Instant.now().plus(Duration.ofDays(20L)))
                .create();

            resolveAuthorizations(order);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("127.0.0.1");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPairForMultiUse);
            byte[] csr = csrb.getEncoded();

            order.execute(csr);

            Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to succeed");

            Certificate acmeCert = order.getCertificate();
            Assertions.assertNotNull(acmeCert, "Expected to receive no certificate");

            acmeCert.revoke(RevocationReason.CESSATION_OF_OPERATION);

            try {
                acmeCert.revoke(RevocationReason.CESSATION_OF_OPERATION);
                Assertions.fail("certificate already revoked, exception expected");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("certificate already revoked", acmeServerException.getMessage());
            }
        }

        /*
         * test with an already used key pair for different domain and lax policy
         */
        {
            Order order = account.newOrder()
                .domains("127.0.0.1")
                .notAfter(Instant.now().plus(Duration.ofDays(20L)))
                .create();

            resolveAuthorizations(order);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("127.0.0.1");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPairForMultiUse);
            byte[] csr = csrb.getEncoded();

            order.execute(csr);
            Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to succeed, policy allows key reuse");

        }

        // use a different account with a different realm, rejecting key reuse
        {
            Order order = otherRealmAccount.newOrder()
                .domains("localhost")
                .create();

            resolveAuthorizations(order);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("localhost");
            csrb.sign(domainKeyPairForMultiUse);
            byte[] csr = csrb.getEncoded();

            try {
                order.execute(csr);
                Assertions.fail("certificate reuses key pair, not allowed by pipeline");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("Key usage scope not applicable. Hint: create a new keypair for each request", acmeServerException.getMessage());
            }
        }

        // use a different account with a different realm, allowing domain key reuse
        {
            KeyPair domainKeyPairForSuccessfulMultiUse = KeyPairUtils.createKeyPair(2048);

            Order order = domainReuseRealmAccount.newOrder()
                .domains("localhost")
                .create();

            resolveAuthorizations(order);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("localhost");
            csrb.sign(domainKeyPairForSuccessfulMultiUse);

            order.execute(csrb.getEncoded());
            Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to succeed, policy allows key reuse");

            Order order2 = domainReuseRealmAccount.newOrder()
                .domains("localhost")
                .create();

            resolveAuthorizations(order2);

            CSRBuilder csrb2 = new CSRBuilder();
            csrb2.addDomain("localhost");
            csrb2.sign(domainKeyPairForSuccessfulMultiUse);

            order2.execute(csrb2.getEncoded());
            Assertions.assertEquals(Status.VALID, order2.getStatus(), "Expecting the finalize request to succeed, policy allows key reuse");

            Order order127 = domainReuseRealmAccount.newOrder()
                .domains("127.0.0.1")
                .create();

            resolveAuthorizations(order127);

            CSRBuilder csrb127 = new CSRBuilder();
            csrb127.addDomain("127.0.0.1");
            csrb127.sign(domainKeyPairForSuccessfulMultiUse);

            try {
                order127.execute(csrb127.getEncoded());
                Assertions.fail("certificate reuses key pair, not allowed by pipeline");
            }catch (AcmeServerException acmeServerException){
                Assertions.assertEquals("Key usage scope not applicable. Hint: create a new keypair for each request", acmeServerException.getMessage());
            }

        }

        // use a different account with a different realm, just warning on key reuse
        {
            Order order = keyUniqueWarnRealmAccount.newOrder()
                .domains("127.0.0.2")
                .create();

            resolveAuthorizations(order);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("127.0.0.2");
            csrb.sign(domainKeyPairForMultiUse);

            order.execute(csrb.getEncoded());
            Assertions.assertEquals(Status.VALID, order.getStatus(), "Expecting the finalize request to succeed, policy allows key reuse");

        }

    }

    private void resolveAuthorizations(Order order) throws IOException, InterruptedException, AcmeException {
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
			Assertions.assertNotNull(acmeCert, "Expected to receive a certificate");

			java.security.cert.X509Certificate x509Cert = acmeCert.getCertificate();
			Assertions.assertNotNull(x509Cert, "Expected to receive a x509Cert");

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
