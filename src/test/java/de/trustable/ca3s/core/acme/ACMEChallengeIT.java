package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeServerException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ACMEChallengeIT {

    private static final Logger LOG = LoggerFactory.getLogger(ACMEChallengeIT.class);

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
        assertNotNull("created account MUST NOT be null", account);

        URL accountLocationUrl = account.getLocation();
        LOG.debug("accountLocationUrl {}", accountLocationUrl);

        Account existingAccount = new AccountBuilder()
            .addContact("mailto:acmeFindExisting@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .onlyExisting()
            .create(session);

        assertNotNull("retrieved account MUST NOT be null", existingAccount);
        assertEquals(account.getContacts().get(0), existingAccount.getContacts().get(0));

        Account newAccount = new AccountBuilder()
            .addContact("mailto:acmeCollidingKey@ca3s.org")
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .create(session);

        assertNotNull("retrieved account MUST NOT be null", newAccount);

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
        assertNotNull("created account MUST NOT be null", account);

        URL accountLocationUrl = account.getLocation();
        LOG.debug("accountLocationUrl {}", accountLocationUrl);


        Account retrievedAccount = new AccountBuilder()
            .onlyExisting()         // Do not create a new account
            .useKeyPair(accountKeyPair)
            .create(session);

        assertNotNull("created account MUST NOT be null", retrievedAccount);
        assertEquals("expected to fimnd the smae account (URL)", accountLocationUrl, retrievedAccount.getLocation());

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
                assertNotNull("expected to find a challenge", challenge);

                LOG.debug("challenge status (pre): {}", challenge.getStatus());

                challenge.trigger();

                LOG.debug("challenge status (post): {}", challenge.getStatus());
                assertEquals(Status.INVALID, challenge.getStatus());
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
                assertNotNull("expected to find a challenge", challenge);

                LOG.debug("correct response would be {}, but it's prepended with 'xxx' ...", challenge.getAuthorization());

                Boolean terminate = Boolean.FALSE;
                Thread webThread = httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), "xxx" + challenge.getAuthorization(), terminate);

                challenge.trigger();

                LOG.debug("challenge status (post): {}", challenge.getStatus());
                assertEquals(Status.INVALID, challenge.getStatus());

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
                    httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), false);
                    challenge.trigger();
                } else {
                    LOG.warn("http01 Challange not found for order");
                }
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
            fail("AcmeServerException  expected");
        }catch( AcmeServerException acmeServerException){
            assertEquals("Public key of CSR already in use ", acmeServerException.getMessage());
        }

        account.deactivate();

        assertEquals("account status 'deactivated' expected", Status.DEACTIVATED, account.getStatus() );
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
        assertNotNull("created account MUST NOT be null", account);

        URL accountLocationUrl = account.getLocation();
        LOG.debug("accountLocationUrl {}", accountLocationUrl);


        Account retrievedAccount = new AccountBuilder()
            .onlyExisting()         // Do not create a new account
            .useKeyPair(accountKeyPair)
            .create(session);

        assertNotNull("created account MUST NOT be null", retrievedAccount);
        assertEquals("expected to fimnd the smae account (URL)", accountLocationUrl, retrievedAccount.getLocation());

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
                assertNotNull("expected to find a challenge", challenge);

                dnsChallengeHelper.setChallengeDetails( challenge.getDigest(), auth.getIdentifier().toString());
                dnsChallengeHelper.start();

                try {
                    challenge.trigger();
                    assertEquals(Status.VALID, challenge.getStatus());
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
            fail("AcmeServerException expected");
        }catch( AcmeServerException acmeServerException){
            assertEquals("Public key of CSR already in use ", acmeServerException.getMessage());
        }

        account.deactivate();

        assertEquals("account status 'deactivated' expected", Status.DEACTIVATED, account.getStatus() );
    }

    void buildOrder(Account account, int n) throws AcmeException {
        account.newOrder()
            .domains("example_"+n+".org")
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();
    }
}
