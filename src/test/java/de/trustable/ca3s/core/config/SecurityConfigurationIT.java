package de.trustable.ca3s.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.ServerSocket;

import de.trustable.ca3s.core.PipelineTestConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import de.trustable.ca3s.core.Ca3SApp;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SecurityConfigurationIT {

    // @LocalServerPort
    //int serverPort = 8080; // random port chosen by spring test

    @Autowired
    PipelineTestConfiguration ptc;

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfigurationIT.class);

    public static int tlsAccessPort;
    public static int adminAccessPort;
    public static int raAccessPort;
    public static int acmeAccessPort;
    public static int scepAccessPort;
    public static int estAccessPort;

    String startUrl;
    String startRAUrl;
    String startAdminUrl;
    String startAcmeUrl;

    @BeforeAll
    static void setUp() throws IOException {

        tlsAccessPort = getFreePort();
        adminAccessPort = getFreePort();
        raAccessPort = getFreePort();
        acmeAccessPort = getFreePort();
        scepAccessPort = getFreePort();
        estAccessPort = getFreePort();

        System.setProperty(Ca3SApp.SERVER_TLS_PREFIX + "port", "" + tlsAccessPort);
        System.setProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "port", "" + adminAccessPort);
        System.setProperty(Ca3SApp.SERVER_RA_PREFIX + "port", "" + raAccessPort);
        System.setProperty(Ca3SApp.SERVER_ACME_PREFIX + "port", "" + acmeAccessPort);
        System.setProperty(Ca3SApp.SERVER_SCEP_PREFIX + "port", "" + scepAccessPort);
        System.setProperty(Ca3SApp.SERVER_EST_PREFIX + "port", "" + estAccessPort);

        System.setProperty(Ca3SApp.SERVER_TLS_PREFIX + "https", "false");
        System.setProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "https", "false");
        System.setProperty(Ca3SApp.SERVER_RA_PREFIX + "https", "false");
        System.setProperty(Ca3SApp.SERVER_ACME_PREFIX + "https", "false");
        System.setProperty(Ca3SApp.SERVER_SCEP_PREFIX + "https", "false");
        System.setProperty(Ca3SApp.SERVER_EST_PREFIX + "https", "false");

    }

    @AfterAll
    static void tearDown(){
        System.clearProperty(Ca3SApp.SERVER_TLS_PREFIX + "port");
        System.clearProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "port");
        System.clearProperty(Ca3SApp.SERVER_RA_PREFIX + "port");
        System.clearProperty(Ca3SApp.SERVER_ACME_PREFIX + "port");
        System.clearProperty(Ca3SApp.SERVER_SCEP_PREFIX + "port");
        System.clearProperty(Ca3SApp.SERVER_EST_PREFIX + "port");

        System.clearProperty(Ca3SApp.SERVER_TLS_PREFIX + "https");
        System.clearProperty(Ca3SApp.SERVER_ADMIN_PREFIX + "https");
        System.clearProperty(Ca3SApp.SERVER_RA_PREFIX + "https");
        System.clearProperty(Ca3SApp.SERVER_ACME_PREFIX + "https");
        System.clearProperty(Ca3SApp.SERVER_SCEP_PREFIX + "https");
        System.clearProperty(Ca3SApp.SERVER_EST_PREFIX + "https");
    }

    static int getFreePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        } catch (IOException e) {
            LOG.warn("Could not find any ports", e);
            throw e;
        }
    }

    @BeforeEach
    void init() {
        startUrl = "http://localhost:" + tlsAccessPort + "/";
        startRAUrl = "http://localhost:" + raAccessPort + "/";
        startAdminUrl = "http://localhost:" + adminAccessPort + "/";
        startAcmeUrl = "http://localhost:" + acmeAccessPort + "/";
    }

    @Test
    void testAuthenticationEndpointAvailability() throws Exception {

        LOG.debug("calling to URL {}", startUrl
            + "api/authenticate");

        // Given
        HttpUriRequest request = new HttpGet(startUrl + "api/authenticate");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertEquals(HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    void testRAOnlyEndpointAvailability() throws Exception {

        // Given
        HttpUriRequest request = new HttpGet(startRAUrl + "/api/administerRequest");

        // When
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertEquals(HttpStatus.SC_UNAUTHORIZED,
            httpResponse.getStatusLine().getStatusCode());


        // Given
        request = new HttpGet(startRAUrl + "/api/administerRequest");

        request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_USER_CONTENT));

        // When
        httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertEquals(HttpStatus.SC_FORBIDDEN,
            httpResponse.getStatusLine().getStatusCode());


        // Given
        request = new HttpGet(startUrl + "/api/administerRequest");

        request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_RA_CONTENT));

        // When
        httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertEquals(HttpStatus.SC_FORBIDDEN,
            httpResponse.getStatusLine().getStatusCode());

        // Given
        request = new HttpGet(startRAUrl + "/api/administerRequest");

        request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_RA_CONTENT));

        // When
        httpResponse = HttpClientBuilder.create().build().execute(request);

        // Then
        assertEquals(HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());

    }

    @Test
    void testAdminOnlyEndpointAvailability() throws Exception {

        String adminUrl = startAdminUrl + "/api/users";
        String adminUrlWrongPort = startUrl + "/api/users";
        {
            // Given
            HttpUriRequest request = new HttpGet(adminUrl);

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_UNAUTHORIZED,
                httpResponse.getStatusLine().getStatusCode());
        }
        {
            // Given
            HttpUriRequest request = new HttpGet(adminUrl);

            request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_USER_CONTENT));

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_FORBIDDEN,
                httpResponse.getStatusLine().getStatusCode());
        }

        {
            // Given
            HttpUriRequest request = new HttpGet(adminUrlWrongPort);

            request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_ADMIN_CONTENT));

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_FORBIDDEN,
                httpResponse.getStatusLine().getStatusCode());

        }

        {
            // Given
            HttpUriRequest request = new HttpGet(adminUrl);

            request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_ADMIN_CONTENT));

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_OK,
                httpResponse.getStatusLine().getStatusCode());
        }

    }

    @Test
    void testAcmeOnlyEndpointAvailability() throws Exception {

        ptc.getInternalACMETestPipelineLaxRestrictions();

        final String ACME_PATH_PART = "/acme/" + PipelineTestConfiguration.ACME_REALM + "/directory";

        String acmeUrl = startAcmeUrl + ACME_PATH_PART;
        String acmeUrlWrongPort = startUrl + ACME_PATH_PART;

        {
            // Given
            HttpUriRequest request = new HttpGet(acmeUrl);

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_OK,
                httpResponse.getStatusLine().getStatusCode());
        }
        {
            // Given
            HttpUriRequest request = new HttpGet(acmeUrlWrongPort);

            // When
            HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

            // Then
            assertEquals(HttpStatus.SC_UNAUTHORIZED,
                httpResponse.getStatusLine().getStatusCode());
        }
    }


    String getJWT(final String param) throws IOException, UnsupportedOperationException, ParseException {

        // Given
        HttpPost request = new HttpPost(startUrl + "api/authenticate");
        request.setEntity(new StringEntity(param));
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-type", "application/json");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        assertEquals(HttpStatus.SC_OK,
            httpResponse.getStatusLine().getStatusCode());

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject token = (JSONObject) parser.parse(httpResponse.getEntity().getContent());

        return token.getAsString("id_token");
    }


    final static String LOGIN_ADMIN_CONTENT = "{\"username\":\"admin\",\"password\":\"admin\",\"rememberMe\":null}";
    final static String LOGIN_RA_CONTENT = "{\"username\":\"ra\",\"password\":\"s3cr3t\",\"rememberMe\":null}";
    final static String LOGIN_USER_CONTENT = "{\"username\":\"user\",\"password\":\"user\",\"rememberMe\":null}";
}
