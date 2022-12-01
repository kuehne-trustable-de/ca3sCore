package de.trustable.ca3s.core.security.apikey;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class NonApyKeyIT {

    private static final Logger LOG = LoggerFactory.getLogger(NonApyKeyIT.class);
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
    }

    @Test
    public void testValidAPIKeyGet() {

        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target( "http://localhost:" + serverPort + USER_PATH_PART);

        // Valid invocation with given API Key
        Invocation.Builder invocationBuilder = webTarget.request();
        invocationBuilder.header(X_API_KEY, apiKey);
        Response response =  invocationBuilder.get();
        Assertions.assertEquals(403, response.getStatus());

    }

}
