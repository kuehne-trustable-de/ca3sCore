package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.shredzone.acme4j.Account;
import org.shredzone.acme4j.AccountBuilder;
import org.shredzone.acme4j.Metadata;
import org.shredzone.acme4j.Session;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.exception.AcmeServerException;
import org.shredzone.acme4j.util.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URL;
import java.security.KeyPair;

import static org.junit.Assert.fail;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AcmeAlgoRestrictionsIT {

    private static final Logger LOG = LoggerFactory.getLogger(AcmeAlgoRestrictionsIT.class);
    public static final String MAILTO_VALID_CA_3_S_ORG = "mailto:valid@ca3s.org";

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
        prefTC.getTestUserPreferenceStrongAlgos(serverPort);
	}

	@BeforeAll
	public static void setUpBeforeClass() {
        System.setProperty("ca3s.acme.account.checkKeyRestrictions", "true");

        JCAManager.getInstance();
	}


    @Test
    public void testAccountHandlingAccountKeyLength() throws AcmeException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        try {
            KeyPair accountKeyPair = KeyPairUtils.createKeyPair(2048);

            new AccountBuilder()
                .addContact(MAILTO_VALID_CA_3_S_ORG)
                .agreeToTermsOfService()
                .useKeyPair(accountKeyPair)
                .create(session);
            fail("account key MUST NOT match restrictions");
        } catch (AcmeServerException acmeServerException) {

        }
    }
    @Test
    public void testAccountHandlingChangeAccountKeyLength() throws AcmeException {

        Session session = new Session(dirUrl);
        Metadata meta = session.getMetadata();

        URI tos = meta.getTermsOfService();
        URL website = meta.getWebsite();
        LOG.debug("TermsOfService {}, website {}", tos, website);

        KeyPair accountKeyPair = KeyPairUtils.createKeyPair(4096);

        Account account = new AccountBuilder()
            .addContact(MAILTO_VALID_CA_3_S_ORG)
            .agreeToTermsOfService()
            .useKeyPair(accountKeyPair)
            .create(session);

        try {
            KeyPair accountNewKeyPair = KeyPairUtils.createKeyPair(2048);

            account.changeKey(accountNewKeyPair);

            fail("changed account key MUST NOT match restrictions");
        } catch (AcmeServerException acmeServerException) {

        }
    }


}
