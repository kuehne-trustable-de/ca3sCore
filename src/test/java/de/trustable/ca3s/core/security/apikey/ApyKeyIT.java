package de.trustable.ca3s.core.security.apikey;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.PipelineTestConfiguration;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;

import static de.trustable.ca3s.core.web.rest.acme.AcmeController.REPLAY_NONCE_HEADER;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class ApyKeyIT {

    private static final Logger LOG = LoggerFactory.getLogger(ApyKeyIT.class);
    public static final String X_API_KEY = "X-API-KEY";

    static String apiKey;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

	final String USER_PATH_PART = "/api/users";


    @BeforeEach
	void init() {
    }

    @BeforeAll
	public static void setUpBeforeClass() throws NoSuchAlgorithmException {
		JCAManager.getInstance();

        apiKey = "TEST_APIKEY_Myc4pWzjhdg4UTklh7jgfdFJ9F3OHDeibPku3LDKEJj6HgPub1CPJLWPgyOoNSBzVgy7Vk1IaqI" +
            "intV3qHvPU6MixZYawEwuZyXYXRtR1scPUkcPW5dq" + SecureRandom.getInstanceStrong().nextLong();

        System.setProperty("ca3s.auth.api-key.enabled", "true");
        System.setProperty("ca3s.auth.api-key.auth-token-header-name", X_API_KEY);
        System.setProperty("ca3s.auth.api-key.auth-token-admin", apiKey);

    }

    @Test
    public void testValidAPIKeyGet() {

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target( "http://localhost:" + serverPort + USER_PATH_PART);

        // Valid invocation with given API Key
        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(X_API_KEY, apiKey);
        Response response = invocationBuilder.get();
        Assertions.assertEquals(200, response.getStatus());

        // Invalid invocation with unknown API Key
        Invocation.Builder invocationBuilderFail = webTarget.request();
        invocationBuilderFail.header(X_API_KEY, "apiKeyUnknown1234567890");
        Response responseFail =  invocationBuilderFail.get();

        Assertions.assertEquals(403, responseFail.getStatus());

        // Invalid invocation without API Key
        Invocation.Builder invocationBuilderNoKey = webTarget.request();
        Response responseNoKey =  invocationBuilderNoKey.get();

        Assertions.assertEquals(403, responseNoKey.getStatus());
    }

}
