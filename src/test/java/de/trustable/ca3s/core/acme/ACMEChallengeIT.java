package de.trustable.ca3s.core.acme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.BindException;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Authorization;
import org.shredzone.acme4j.Metadata;
import org.shredzone.acme4j.Order;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.Status;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.CaConfigTestConfiguration;
import de.trustable.util.JCAManager;

// @SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @ContextConfiguration(classes=CaConfigTestConfiguration.class)
public class ACMEChallengeIT {

    private static final Logger LOG = LoggerFactory.getLogger(ACMEChallengeIT.class);

//	@LocalServerPort
	int serverPort = 8080; // random port chosen by spring test

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		JCAManager.getInstance();
	}

	
	@SuppressWarnings("deprecation")
	@Test
	public void testAccountHandling() throws AcmeException, IOException, InterruptedException {

		String dirUrl = "http://localhost:" + serverPort + "/acme/ejbca/directory";

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
		
		// challenge an authorization that will not succeed
		for (Authorization auth : order.getAuthorizations()) {
			LOG.debug("checking auth id {} for {} with status {}", auth.getIdentifier(), auth.getLocation(), auth.getStatus());
			if (auth.getStatus() == Status.PENDING) {

				Http01Challenge challenge = auth.findChallenge(Http01Challenge.TYPE);

//				provideAuthEndpoint(challenge, order);

				try {
					challenge.trigger();
					fail("Challenge expected to fail");
				} catch(AcmeException ae) {
					// as expected
				}
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

				LOG.debug("correct response would be {}, but it's prepended with 'xxx' ...", challenge.getAuthorization());
				
				Boolean terminate = Boolean.FALSE;
				Thread webThread = provideAuthEndpoint(challenge.getToken(), "xxx" + challenge.getAuthorization(), terminate);

				try {
					challenge.trigger();
					fail("Challenge expected to fail");
				} catch(AcmeException ae) {
					// as expected
				}finally {
					terminate = Boolean.TRUE;
				}
				
				webThread.stop();
			}

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

	Thread provideAuthEndpoint(String fileName, String fileContent, Boolean terminate) throws IOException, InterruptedException {

		int callbackPort = 8800;
		final String fileNameRegEx = "/\\.well-known/acme-challenge/" + fileName;

		LOG.debug("Handling authorization for {}", fileNameRegEx);

		Take tk = new TkFork(new FkRegex(fileNameRegEx, fileContent));
		
		FtBasic webBasicTmp = null;
		try {
			webBasicTmp = new FtBasic(tk, callbackPort);
		}catch(BindException be) {
			Thread.sleep(1000L);
			webBasicTmp = new FtBasic(tk, callbackPort);
		}
		final FtBasic webBasic = webBasicTmp;

		final Exit exitOnValid = new Exit() {
			@Override
			public boolean ready() {
				LOG.info("exitOnValid by Boolean {}", terminate.booleanValue());
				return (terminate.booleanValue());
			}
		};

		Thread webThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					LOG.debug("ACME callback webserver started for {}", fileNameRegEx);
					webBasic.start(exitOnValid);
					LOG.debug("ACME callback webserver finished for {}", fileNameRegEx);
				} catch (IOException ioe) {
					LOG.warn("exception occur running webserver in extra thread", ioe);
				}
			}
		});
		
		webThread.start();

		LOG.debug("started ACME callback webserver for {} on port {}", fileNameRegEx, callbackPort);

		return webThread;
	}
}
