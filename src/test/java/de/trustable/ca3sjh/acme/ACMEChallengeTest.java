package de.trustable.ca3sjh.acme;

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

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import de.trustable.ca3sjh.Ca3SJhApp;
import de.trustable.util.JCAManager;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Ca3SJhApp.class)
@RunWith(SpringRunner.class)
@ActiveProfiles("int")
@TestPropertySource(locations = "classpath:config/application_test.yml")
public class ACMEChallengeTest {

    private static final Logger LOG = LoggerFactory.getLogger(ACMEChallengeTest.class);

	@Value("${local.server.port}")
	int serverPort; // random port chosen by spring test

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		JCAManager.getInstance();
	}

	
	@Test
	public void testAccountHandling() throws AcmeException, IOException, InterruptedException {

		String dirUrl = "http://localhost:" + serverPort + "/acme/foo/directory";

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

				LOG.debug("correct response would be {}, but we are prepending 'xxx' ...", challenge.getAuthorization());
				
				provideAuthEndpoint(challenge.getToken(), "xxx" + challenge.getAuthorization(), order);

				try {
					challenge.trigger();
					fail("Challenge expected to fail");
				} catch(AcmeException ae) {
					// as expected
				}
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

	void provideAuthEndpoint(String fileName, String fileContent, Order order) throws IOException, InterruptedException {

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
				boolean bTerminate = !(order.getStatus().equals( Status.PENDING));
				LOG.debug("exitOnValid {}", order.getStatus().toString());
				return (bTerminate);
			}
		};

		new Thread(new Runnable() {
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
		}).start();

		LOG.debug("started ACME callback webserver for {} on port {}", fileNameRegEx, callbackPort);

	}
}
