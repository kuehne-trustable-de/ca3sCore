package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.*;
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
import org.takes.Take;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Exit;
import org.takes.http.FtBasic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.time.Duration;
import java.time.Instant;

import static org.junit.Assert.*;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class ACMERestrictionsIT {

    private static final Logger LOG = LoggerFactory.getLogger(ACMERestrictionsIT.class);

	@LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME1CNNOIP_REALM + "/directory";
	String dirUrl;

	@Autowired
	PipelineTestConfiguration ptc;

    @Autowired
    PreferenceTestConfiguration prefTC;

    @BeforeEach
	void init() {
		dirUrl = "http://localhost:" + serverPort + ACME_PATH_PART;
		ptc.getInternalACMETestPipelineLaxRestrictions();
		ptc.getInternalACMETestPipeline_1_CN_ONLY_NO_IP_Restrictions();
        prefTC.getTestUserPreference();
	}

	@BeforeAll
	public static void setUpBeforeClass() {
		JCAManager.getInstance();
	}


	@Test
	public void testAccountHandling() throws AcmeException, IOException, InterruptedException {

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
		// request mismatching restrictions
		// #########################


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
					if( challenge != null) {

                        int MAX_TRIAL = 10;
                        for (int retry = 0; retry < MAX_TRIAL; retry++) {
                            try {
                                provideAuthEndpoint(challenge, order);
                                break;
                            } catch (BindException be) {
                                LOG.warn("bind exception, waiting for port to become available");
                            }
                            if (retry == MAX_TRIAL - 1) {
                                LOG.warn("callback port not available");
                            }
                        }
                        challenge.trigger();
                    }else{
                        LOG.warn("no available for given auth");
                    }
				}
			}

			KeyPair domainKeyPair = KeyPairUtils.createKeyPair(2048);

			CSRBuilder csrb = new CSRBuilder();
			csrb.addDomain("localhost");
			csrb.addIP(InetAddress.getByName("127.0.0.1"));
			csrb.setOrganization("The Example Organization");
			csrb.sign(domainKeyPair);
			byte[] csr = csrb.getEncoded();

			try {
				order.execute(csr);
				Assertions.fail("AcmeException due to restriction violation expected");
			}catch(AcmeServerException ase) {
				// as expected
			}

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

	void provideAuthEndpoint(final Http01Challenge challenge, Order order) throws IOException, InterruptedException {
		int MAX_TRIAL = 10;
		for( int retry = 0; retry < MAX_TRIAL; retry++) {
			try {
				provideAuthEndpoint(challenge);
				break;
			} catch( BindException be) {
				System.out.println("bind exception, waiting for port to become available");
			}
			if( retry == MAX_TRIAL -1) {
				System.out.println("callback port not available");
			}
		}
	}

	void provideAuthEndpoint(final Http01Challenge challenge) throws IOException, InterruptedException {

        int callbackPort = this.prefTC.getFreePort();
		final String fileNameRegEx = "/\\.well-known/acme-challenge/" + challenge.getToken();
		String fileContent = challenge.getAuthorization();

        LOG.debug("Handling authorization for {} serving {} on port {}", fileNameRegEx, fileContent, callbackPort);

		Take tk = new TkFork(new FkRegex(fileNameRegEx, fileContent));

		FtBasic webBasicTmp;
		try {
			webBasicTmp = new FtBasic(tk, callbackPort);
		}catch(BindException be) {
			Thread.sleep(1000L);
			webBasicTmp = new FtBasic(tk, callbackPort);
		}
		final FtBasic webBasic = webBasicTmp;

		final Exit exitOnValid = () -> {
            boolean bTerminate = !(challenge.getStatus().equals( Status.PENDING));
            LOG.info("exitOnValid {}", challenge.getStatus().toString());
            return (bTerminate);
        };

		new Thread(() -> {
            try {
                LOG.debug("ACME callback webserver started for {}", fileNameRegEx);
                webBasic.start(exitOnValid);
                LOG.debug("ACME callback webserver finished for {}", fileNameRegEx);
            } catch (IOException ioe) {
                LOG.warn("exception occur running webserver in extra thread", ioe);
            }
        }).start();

		LOG.debug("started ACME callback webserver for {} on port {}", fileNameRegEx, callbackPort);

	}
}
