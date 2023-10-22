package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.*;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeRateLimitedException;
import org.shredzone.acme4j.exception.AcmeServerException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AcmeChallengeIT {

    private static final Logger LOG = LoggerFactory.getLogger(AcmeChallengeIT.class);

    static int dnsPort = 0;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
	String dirUrl;

    HttpChallengeHelper httpChallengeHelper;

    DnsChallengeHelper dnsChallengeHelper;


    @Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeEach
	void init() {
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
        LOG.info("ptc: {}", ptc);
        try {
            ptc.getInternalACMETestPipelineLaxRestrictions();
            prefTC.getTestUserPreference();
            httpChallengeHelper = new HttpChallengeHelper(prefTC.getFreePort());
            dnsChallengeHelper = new DnsChallengeHelper(dnsPort);
        }catch( Exception ex ){
            ex.printStackTrace();
        }
    }

	@BeforeAll
	public static void setUpBeforeClass() {
		JCAManager.getInstance();

        dnsPort = SocketUtils.findAvailableTcpPort(45000);
        System.setProperty("ca3s.dns.server", "localhost");
        System.setProperty("ca3s.dns.port", "" + dnsPort);
        LOG.info("DNS server set to {}", "localhost:" + dnsPort);
    }

    @Test
    public void testAccountKeyHandling() throws AcmeException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);


        Account account = new AccountBuilder()
            .addContact("mailto:acmeTest@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .create(session);
        Assertions.assertNotNull(account, "created account MUST NOT be null");

        URL accountLocationUrl = account.getLocation();
        LOG.debug("accountLocationUrl {}", accountLocationUrl);

        Account existingAccount = new AccountBuilder()
            .addContact("mailto:acmeFindExisting@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .onlyExisting()
            .create(session);

        Assertions.assertNotNull(existingAccount, "retrieved account MUST NOT be null");
        Assertions.assertEquals(account.getContacts().get(0), existingAccount.getContacts().get(0));

        Account newAccount = new AccountBuilder()
            .addContact("mailto:acmeCollidingKey@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .create(session);

        Assertions.assertNotNull(newAccount, "retrieved account MUST NOT be null");

    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHttpChallengeHandling() throws AcmeException, IOException, InterruptedException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);


        Account account = new AccountBuilder()
            .addContact("mailto:acmeTest@ca3s.org")
            .agreeToTermsOfService()
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

        // #########################
        // unreachable http endpoint
        // #########################

        Order order = account.newOrder()
            .domains("never.seen.before")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        System.out.println("Auth: " + order.getAuthorizations().get(0).toString() );

        // challenge an authorization that will not succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertNotNull(challenge, "expected to find a challenge");

                LOG.debug("challenge status (pre): {}", challenge.getStatus());

                challenge.trigger();

                LOG.debug("challenge status (post): {}", challenge.getStatus());
                Assertions.assertEquals(Status.PENDING, challenge.getStatus());
            }
        }

        // #########################
        // http endpoint serving wrong content
        // #########################

        order = account.newOrder()
            .domains("localhost")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        // challenge an authorization that will not succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertNotNull(challenge, "expected to find a challenge");

                LOG.debug("correct response would be {}, but it's prepended with 'xxx' ...", challenge.getAuthorization());

                Boolean terminate = Boolean.FALSE;
                Thread webThread = httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), "xxx" + challenge.getAuthorization(), terminate);

                challenge.trigger();

                LOG.debug("challenge status (post): {}", challenge.getStatus());
                Assertions.assertEquals(Status.PENDING, challenge.getStatus());

                webThread.stop();
            }
        }

        // ##########################
        // csr with key already used
        // ##########################

        order = account.newOrder()
            .domains("localhost")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            String realmPart = "/" + PipelineTestConfiguration.ACME_REALM + "/";
            assertTrue( auth.getLocation().toString().contains(realmPart));

            if (auth.getStatus() == Status.PENDING) {

                Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

                if( challenge != null) {
                    Thread webThread = httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), false);
                    challenge.trigger();
                    webThread.stop();

                } else {
                    LOG.warn("http01 Challenge not found for order");
                }
            }
        }

        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomain("localhost");
        csrb.setOrganization("The Example Organization");
        csrb.sign(accountKeyPair); // should be detected !!
        byte[] csr = csrb.getEncoded();

        for(Authorization auth: order.getAuthorizations()){
            System.out.println( " ################ " + auth.getIdentifier().toString() + " " + auth.getLocation() );
        }

        try{
            order.execute(csr);
            Assertions.fail("AcmeServerException  expected");
        }catch( AcmeServerException acmeServerException){
            Assertions.assertEquals("Public key of CSR already in use ", acmeServerException.getMessage());
        }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHttpChallengeCSRMatching() throws AcmeException, IOException, InterruptedException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

        Account account = new AccountBuilder()
            .addContact("mailto:acmeTest@ca3s.org")
            .agreeToTermsOfService()
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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        // #########################
        // valid http endpoint
        // #########################

        Order order = account.newOrder()
            .domains("localhost")
            .notAfter(Instant.now().plus(Duration.ofDays(1L)))
            .create();

        System.out.println("Auth: " + order.getAuthorizations().get(0).toString() );

        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            String realmPart = "/" + PipelineTestConfiguration.ACME_REALM + "/";
            assertTrue( auth.getLocation().toString().contains(realmPart));

            if (auth.getStatus() == Status.PENDING) {

                Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

                if( challenge != null) {
                    Thread webThread = httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), false);
                    challenge.trigger();
                    webThread.stop();
                } else {
                    LOG.warn("http01 challenge not found for order");
                }
            }
        }


        // ##########################
        // csr not matching challenge
        // ##########################
            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("localhost");
            csrb.addDomain("foo.com");
            csrb.setOrganization("The Example Organization");

            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();
            LOG.warn("csr : " + Base64.getEncoder().encodeToString(csr));

            for (Authorization auth : order.getAuthorizations()) {
                System.out.println(" ################ " + auth.getIdentifier().toString() + "" + auth.getLocation());
            }

            try {
                order.execute(csr);
                Assertions.fail("AcmeServerException expected");
            } catch (AcmeServerException acmeServerException) {
                assertTrue(acmeServerException.getMessage().startsWith("failed to find requested hostname 'foo.com' (from CSR) in authorization for order"),
                    "failed to find requested hostname 'foo.com' (from CSR) in authorization for order ");
                order.update();
                Assertions.assertEquals(Status.INVALID, order.getStatus());
            }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @Disabled
    @Test
    public void testHttpChallengeRateLimit() throws AcmeException, IOException, InterruptedException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

        Account account = new AccountBuilder()
            .addContact("mailto:acmeTest@ca3s.org")
            .agreeToTermsOfService()
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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        // #########################
        // valid http endpoint
        // #########################

        Order order = account.newOrder()
            .domains("localhost")
            .notAfter(Instant.now().plus(Duration.ofDays(1L)))
            .create();

        System.out.println("Auth: " + order.getAuthorizations().get(0).toString());

        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            String realmPart = "/" + PipelineTestConfiguration.ACME_REALM + "/";
            assertTrue(auth.getLocation().toString().contains(realmPart));

            if (auth.getStatus() == Status.PENDING) {

                Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

                try {
                    for (int i = 0; i < 100; i++) {
                        // DoS the endpoint
                        challenge.trigger();
                    }
                    fail("AcmeRateLimitedException expected");
                }catch (AcmeRateLimitedException acmeRateLimitedException){
                    // as expected
                    LOG.debug( "AcmeRateLimitedException: {}", acmeRateLimitedException);
                }
            }
        }
    }

    @Disabled
    @Test
    public void testDnsChallengeHandling() throws AcmeException, IOException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

        Account account = new AccountBuilder()
            .addContact("mailto:acmeTestDns@ca3s.org")
            .agreeToTermsOfService()
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

        // #########################
        // http endpoint serving wrong content
        // #########################

        Order order = account.newOrder()
            .domains("localhost")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        // challenge an authorization that will not succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Dns01Challenge challenge = auth.findChallenge(Dns01Challenge.TYPE);
                Assertions.assertNotNull(challenge, "expected to find a challenge");

                dnsChallengeHelper.setChallengeDetails( challenge.getDigest(), auth.getIdentifier().toString());
                dnsChallengeHelper.start();

                try {
                    challenge.trigger();
                    Assertions.assertEquals(Status.VALID, challenge.getStatus());
                }finally {
                    dnsChallengeHelper.stop();
                }

                dnsChallengeHelper.stop();
            }

        }


        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomain("localhost");
        csrb.setOrganization("The Example Organization");
        csrb.sign(accountKeyPair); // should be detected !!
        byte[] csr = csrb.getEncoded();

        for(Authorization auth: order.getAuthorizations()){
            System.out.println( " ################ "  + auth.getIdentifier().toString() + "" + auth.getLocation() );
        }

        try{
            order.execute(csr);
            Assertions.fail("AcmeServerException expected");
        }catch( AcmeServerException acmeServerException){
            Assertions.assertEquals("Public key of CSR already in use ", acmeServerException.getMessage());
        }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    void buildOrder(Account account, int n) throws AcmeException {
        account.newOrder()
            .domains("example_"+n+".org")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();
    }
}
