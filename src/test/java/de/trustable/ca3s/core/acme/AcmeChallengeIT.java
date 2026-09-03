package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.*;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.challenge.Challenge;
import org.shredzone.acme4j.challenge.Dns01Challenge;
import org.shredzone.acme4j.challenge.DnsPersist01Challenge;
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
import java.util.EnumSet;
import java.util.Optional;

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
    final String ACME_DNS_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_DNS_REALM + "/directory";
    final String ACME_DNS_PERSIST_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_DNS_PERSIST_REALM + "/directory";
    final String ACME_DNS_PERSIST_WILDCARD_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_DNS_PERSIST_WILDCARD_REALM + "/directory";
    String dirUrl;
    String dirUrlDNS;
    String dirUrlDNSPersist;
    String dirUrlDNSPersistWildcard;

    HttpChallengeHelper httpChallengeHelper;

    static DnsChallengeHelper dnsChallengeHelper;


    @Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeEach
	void init() {
        dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
        dirUrlDNS = "http://localhost:" + serverPort + ACME_DNS_PATH_PART;
        dirUrlDNSPersist = "http://localhost:" + serverPort + ACME_DNS_PERSIST_PATH_PART;
        dirUrlDNSPersistWildcard = "http://localhost:" + serverPort + ACME_DNS_PERSIST_WILDCARD_PATH_PART;

        LOG.info("ptc: {}", ptc);
        try {
            ptc.getInternalACMETestPipelineLaxRestrictions();
            ptc.getInternalACMETestPipelineDNSLaxRestrictions();
            ptc.getInternalACMETestPipelineDNSPersistLaxRestrictions();
            ptc.getInternalACMETestPipelineDNSPersistWildcardLaxRestrictions();

            prefTC.getTestUserPreference();
            httpChallengeHelper = new HttpChallengeHelper(prefTC.getHttpChallengePort());
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

        dnsChallengeHelper = new DnsChallengeHelper(dnsPort);
        dnsChallengeHelper.start();
        LOG.info("Started DNS server");

    }

    @AfterAll
    static void tearDown() {
        dnsChallengeHelper.stop();
        LOG.info("Stopped DNS server");

        System.clearProperty("ca3s.dns.server");
        System.clearProperty("ca3s.dns.port");
    }

    @Test
    public void testAccountKeyHandling() throws AcmeException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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

        logMetaInfo(meta);

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

                Optional<Challenge> challengeOpt = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Challenge challenge = challengeOpt.get();
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

                Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Http01Challenge challenge = challengeOpt.get();

                LOG.debug("correct response would be {}, but it's prepended with 'xxx' ...", challenge.getAuthorization());

                httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), "xxx" + challenge.getAuthorization(), true);

                challenge.trigger();

                LOG.debug("challenge status (post): {}", challenge.getStatus());
                Assertions.assertEquals(Status.PENDING, challenge.getStatus());

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

                Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Http01Challenge challenge = challengeOpt.get();

                if( challenge != null) {
                    httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), true);
                    challenge.trigger();
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
            Assertions.assertEquals("Public key of CSR already in use by account", acmeServerException.getMessage());
        }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testHttpChallengeCSRMatching() throws AcmeException, IOException, InterruptedException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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

                Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Http01Challenge challenge = challengeOpt.get();

                if( challenge != null) {
                    httpChallengeHelper.provideAuthEndpoint(challenge.getToken(), challenge.getAuthorization(), true);
                    challenge.trigger();
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
                System.out.println(" ################ " + auth.getIdentifier().toString() + " " + auth.getLocation());
            }

            try {
                order.execute(csr);
                Assertions.fail("AcmeServerException expected");
            } catch (AcmeServerException acmeServerException) {
                assertTrue(acmeServerException.getMessage().startsWith("failed to find requested hostname 'foo.com' (from CSR) in authorization for order"),
                    "failed to find requested hostname 'foo.com' (from CSR) in authorization for order ");

                waitForFinalStatus(order);
                Assertions.assertEquals(Status.INVALID, order.getStatus());
            }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    private static void waitForFinalStatus(Order order) throws AcmeException {
        for( int i = 0; i < 10; i++){
            if(EnumSet.of(Status.VALID, Status.INVALID).contains(order.getStatus())) {
                return;
            }
            try {
                Thread.sleep(500L);
            }catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
            order.fetch();
        }
    }

    public static void logMetaInfo(Metadata meta) {
        Optional<URI> tosOpt = meta.getTermsOfService();
        Optional<URL> websiteOpt = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tosOpt, websiteOpt);
    }

    @Test
    public void testHttpChallengeRateLimit() throws AcmeException, IOException, InterruptedException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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

                Optional<Http01Challenge> challengeOpt = auth.findChallenge(Http01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Http01Challenge challenge = challengeOpt.get();

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

    @Test
    public void testDnsChallengeHandling() throws AcmeException, IOException {

        Session session = new Session(dirUrlDNS);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

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

                Optional<Dns01Challenge> challengeOpt = auth.findChallenge(Dns01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                Dns01Challenge challenge = challengeOpt.get();

                dnsChallengeHelper.setDNSChallengeDetails( challenge.getDigest(), auth.getIdentifier().getValue());

                challenge.trigger();
                waitForFinalStatus(order);
                Assertions.assertEquals(Status.VALID, challenge.getStatus());
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
            Assertions.assertEquals("Public key of CSR already in use by account", acmeServerException.getMessage());
        }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @Test
    public void testDnsPersistChallengeHandling() throws AcmeException, IOException {

        String domain = "localhost";

        Session session = new Session(dirUrlDNSPersist);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        // #########################
        // dns endpoint serving wrong content
        // #########################

        Order order = account.newOrder()
            .domains(domain)
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        // challenge an authorization that will succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                DnsPersist01Challenge challenge = challengeOpt.get();
/*
                String rdata = challenge.buildRData()
                    .issuerDomainName(domain)
                    .wildcard()
                    .persistUntil(Instant.now().plus(3, ChronoUnit.MONTHS))
                    .noQuotes()
                    .build();

                LOG.debug("rdata by acme4j {}", rdata);
                dnsChallengeHelper.addDNSPersistChallengeDetails(rdata, auth.getIdentifier().getValue());
*/
                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "\"",
                    auth.getIdentifier().getValue());

                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                    auth.getIdentifier().getValue());

                challenge.trigger();
                Assertions.assertEquals(Status.VALID, challenge.getStatus());
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
            Assertions.assertEquals("Public key of CSR already in use by account", acmeServerException.getMessage());
        }

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @Test
    public void testDnsPersistChallengeHandlingPersistUntil() throws AcmeException, IOException {

        String domain = "localhost";

        Session session = new Session(dirUrlDNSPersist);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        Order order = account.newOrder()
            .domains(domain)
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        dnsChallengeHelper.entryAndValueList.clear();

        long untilExpired = Instant.now().getEpochSecond() - 5L;

        // challenge an authorization that will succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                DnsPersist01Challenge challenge = challengeOpt.get();


                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "; persistUntil=" + untilExpired + "; policy=xyz123\"",
                    auth.getIdentifier().getValue());

                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                    auth.getIdentifier().getValue());

                challenge.trigger();
                Assertions.assertEquals(Status.PENDING, challenge.getStatus());
            }
        }

        long until = Instant.now().getEpochSecond() + 5L;

        // challenge an authorization that will succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                DnsPersist01Challenge challenge = challengeOpt.get();


                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "; persistUntil=" + until + "; policy=xyz123\"",
                    auth.getIdentifier().getValue());

                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                    auth.getIdentifier().getValue());

                challenge.trigger();
                Assertions.assertEquals(Status.VALID, challenge.getStatus());
            }
        }

        KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

        CSRBuilder csrb = new CSRBuilder();
        csrb.addDomain("localhost");
        csrb.setOrganization("The Example Organization");
        csrb.sign(domainKeyPair);
        byte[] csr = csrb.getEncoded();

        for(Authorization auth: order.getAuthorizations()){
            System.out.println( " ################ "  + auth.getIdentifier().toString() + " / " + auth.getLocation() );
        }

        order.execute(csr);
        Certificate acmeCert = order.getCertificate();
        Assertions.assertNotNull(acmeCert, "Expected to receive a certificate");

        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @Test
    public void testDnsPersistChallengeHandlingPolicyWildcard() throws AcmeException, IOException {

        String domain = "*.localhost";

        Session session = new Session(dirUrlDNSPersistWildcard);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        // try a wildcard mismatch
        {
            Order order = account.newOrder()
                .domains(domain)
                .notAfter(Instant.now().plus(Duration.ofDays(20L)))
                .create();

            long until = System.currentTimeMillis() + 5000L;

            // challenge an authorization that will succeed
            for (Authorization auth : order.getAuthorizations()) {
                LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
                if (auth.getStatus() == Status.PENDING) {

                    Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                    Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                    DnsPersist01Challenge challenge = challengeOpt.get();


                    dnsChallengeHelper.addDNSPersistChallengeDetails(
                        "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "; persistUntil=" + until + "; policy=wildcard\"",
                        auth.getIdentifier().getValue());

                    dnsChallengeHelper.addDNSPersistChallengeDetails(
                        "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                        auth.getIdentifier().getValue());

                    challenge.trigger();
                    Assertions.assertEquals(Status.VALID, challenge.getStatus());
                }
            }

            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("www.foo.localhost");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();

            for (Authorization auth : order.getAuthorizations()) {
                System.out.println(" ################ " + auth.getIdentifier().toString() + " / " + auth.getLocation());
            }

            try{
                order.execute(csr);
                Assertions.fail("AcmeServerException expected");
            }catch( AcmeServerException acmeServerException){
                Assertions.assertTrue( acmeServerException.getMessage().startsWith("failed to find requested hostname 'www.foo.localhost' (from CSR) in authorization"));
            }
        }

        // successful validation
        {
            Order order = account.newOrder()
                .domains(domain)
                .notAfter(Instant.now().plus(Duration.ofDays(20L)))
                .create();

            long until = System.currentTimeMillis() + 5000L;

            // challenge an authorization that will succeed
            for (Authorization auth : order.getAuthorizations()) {
                LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
                if (auth.getStatus() == Status.PENDING) {

                    Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                    Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                    DnsPersist01Challenge challenge = challengeOpt.get();


                    dnsChallengeHelper.addDNSPersistChallengeDetails(
                        "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "; persistUntil=" + until + "; policy=wildcard\"",
                        auth.getIdentifier().getValue());

                    dnsChallengeHelper.addDNSPersistChallengeDetails(
                        "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                        auth.getIdentifier().getValue());

                    challenge.trigger();
                    Assertions.assertEquals(Status.VALID, challenge.getStatus());
                }
            }
            KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

            CSRBuilder csrb = new CSRBuilder();
            csrb.addDomain("foo.localhost");
            csrb.setOrganization("The Example Organization");
            csrb.sign(domainKeyPair);
            byte[] csr = csrb.getEncoded();

            for (Authorization auth : order.getAuthorizations()) {
                System.out.println(" ################ " + auth.getIdentifier().toString() + " / " + auth.getLocation());
            }

            order.execute(csr);
            Certificate acmeCert = order.getCertificate();
            Assertions.assertNotNull(acmeCert, "Expected to receive a certificate");
        }
        account.deactivate();

        Assertions.assertEquals(Status.DEACTIVATED, account.getStatus(), "account status 'deactivated' expected");
    }

    @Test
    public void testDnsPersistChallengeInvalid() throws AcmeException, IOException {

        String domain = "localhost";

        Session session = new Session(dirUrlDNSPersist);
        Metadata meta = session.getMetadata();

        logMetaInfo(meta);

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
        Assertions.assertEquals(accountLocationUrl, retrievedAccount.getLocation(), "expected to find the same account (URL)");

        Order order = account.newOrder()
            .domains(domain)
            .notAfter(Instant.now().plus(Duration.ofDays(20L)))
            .create();

        // challenge an authorization that will not succeed
        for (Authorization auth : order.getAuthorizations()) {
            LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
            if (auth.getStatus() == Status.PENDING) {

                Optional<DnsPersist01Challenge> challengeOpt = auth.findChallenge(DnsPersist01Challenge.TYPE);
                Assertions.assertTrue(challengeOpt.isPresent(), "expected to find a challenge");

                DnsPersist01Challenge challenge = challengeOpt.get();


                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"letsencrypt.org; accounturi=" + accountLocationUrl.toString() + "\"",
                    auth.getIdentifier().getValue());
                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"acme.ca3s.org; accounturi=http://localhost:44085/acme/acmeTestDNSPersistent/acct/00000000000000\"",
                    auth.getIdentifier().getValue());

                dnsChallengeHelper.addDNSPersistChallengeDetails(
                    "\"acme.ca3s.org; accounturi=" + accountLocationUrl.toString() + "; persistUntil=1767225600\"",
                    auth.getIdentifier().getValue());

                challenge.trigger();
                Assertions.assertEquals(Status.PENDING, challenge.getStatus());
            }
        }
    }

}
