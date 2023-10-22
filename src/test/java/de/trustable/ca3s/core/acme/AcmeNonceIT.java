package de.trustable.ca3s.core.acme;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.ca3s.core.PreferenceTestConfiguration;
import de.trustable.ca3s.core.service.dto.acme.DirectoryResponse;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.SocketUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Locale;

import static de.trustable.ca3s.core.web.rest.acme.AcmeController.REPLAY_NONCE_HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AcmeNonceIT {

    private static final Logger LOG = LoggerFactory.getLogger(AcmeNonceIT.class);

    static int dnsPort = 0;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

	final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";
	String dirUrl;
    DirectoryResponse response;

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

            Client client = ClientBuilder.newClient();
            WebTarget dirWebTarget = client.target(dirUrl);
            Invocation.Builder invocationBuilder = dirWebTarget.request(MediaType.APPLICATION_JSON);
            response = invocationBuilder.get(DirectoryResponse.class);

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
    public void testNonceRetrievalGet() throws IOException {

        HttpURLConnection con = (HttpURLConnection) response.getNewNonceUri().toURL().openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();

        Assertions.assertEquals(204, status);

        checkHeader(con);
    }

    @Test
    public void testNonceRetrievalHead() throws IOException {

        HttpURLConnection con = (HttpURLConnection) response.getNewNonceUri().toURL().openConnection();
        con.setRequestMethod("HEAD");

        int status = con.getResponseCode();
        Assertions.assertEquals(200, status);

        checkHeader(con);

    }

    private void checkHeader(HttpURLConnection con) {
        String nonce = con.getHeaderField(REPLAY_NONCE_HEADER);
        Assertions.assertNotNull(nonce);
        System.out.println("nonce = '" + nonce + "'");
        assertTrue(nonce.length() > 16);

        String cacheControl = con.getHeaderField("Cache-Control");
        Assertions.assertEquals("no-store", cacheControl.toLowerCase(Locale.ROOT));
    }

}
