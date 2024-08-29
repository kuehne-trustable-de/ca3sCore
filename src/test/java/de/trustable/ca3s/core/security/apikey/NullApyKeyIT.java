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
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class NullApyKeyIT {

    private static final Logger LOG = LoggerFactory.getLogger(NullApyKeyIT.class);
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

        apiKey = "";

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
        invocationBuilder.header(X_API_KEY, "");
        Response response = invocationBuilder.get();
        Assertions.assertEquals(200, response.getStatus());

        {
            // Invalid invocation with unknown API Key
            Invocation.Builder invocationBuilderFail = webTarget.request();
            invocationBuilderFail.header(X_API_KEY, "null");
            Response responseFail = invocationBuilderFail.get();
            Assertions.assertEquals(401, responseFail.getStatus());
        }
        {
            // Invalid invocation with unknown API Key
            Invocation.Builder invocationBuilderFail = webTarget.request();
            invocationBuilderFail.header(X_API_KEY, null);
            Response responseFail = invocationBuilderFail.get();
            Assertions.assertEquals(401, responseFail.getStatus());
        }
        {
            // Invalid invocation without API Key
            Invocation.Builder invocationBuilderNoKey = webTarget.request();
            Response responseNoKey = invocationBuilderNoKey.get();
            Assertions.assertEquals(401, responseNoKey.getStatus());
        }
    }

}
