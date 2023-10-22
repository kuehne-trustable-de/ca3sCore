package de.trustable.ca3s.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import de.trustable.ca3s.core.Ca3SApp;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class SecurityConfigurationIT {

	@LocalServerPort
	int serverPort = 8080; // random port chosen by spring test

	String dirUrl;

	@BeforeEach
	void init() {
		dirUrl = "http://localhost:" + serverPort + "/";

	}

	@Test
	void testAuthenticationEndpointAvailability() throws Exception {

		// Given
	    HttpUriRequest request = new HttpGet( dirUrl + "api/authenticate" );

	    // When
	    HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

	    // Then
	    assertEquals( HttpStatus.SC_OK,
	    		httpResponse.getStatusLine().getStatusCode());
	}

	@Test
	void testRAOnlyEndpointAvailability() throws Exception {

		// Given
	    HttpUriRequest request = new HttpGet( dirUrl + "/api/administerRequest" );

	    // When
	    HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

	    // Then
	    assertEquals( HttpStatus.SC_UNAUTHORIZED,
	    		httpResponse.getStatusLine().getStatusCode());


		// Given
	    request = new HttpGet( dirUrl + "/api/administerRequest" );

		request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_USER_CONTENT) );

	    // When
	    httpResponse = HttpClientBuilder.create().build().execute( request );

	    // Then
	    assertEquals( HttpStatus.SC_FORBIDDEN,
	    		httpResponse.getStatusLine().getStatusCode());


		// Given
	    request = new HttpGet( dirUrl + "/api/administerRequest" );

		request.addHeader("Authorization", "Bearer " + getJWT(LOGIN_RA_CONTENT) );

	    // When
	    httpResponse = HttpClientBuilder.create().build().execute( request );

	    // Then
	    assertEquals( HttpStatus.SC_OK,
	    		httpResponse.getStatusLine().getStatusCode());


	}

	String getJWT(final String param) throws IOException, UnsupportedOperationException, ParseException {

		// Given
		HttpPost request = new HttpPost( dirUrl + "api/authenticate" );
	    request.setEntity(new StringEntity(param));
	    request.setHeader("Accept", "application/json");
	    request.setHeader("Content-type", "application/json");
	    HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

	    assertEquals( HttpStatus.SC_OK,
	    		httpResponse.getStatusLine().getStatusCode());

        JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
        JSONObject token = (JSONObject)parser.parse(httpResponse.getEntity().getContent());

        return token.getAsString("id_token");
	}


	final static String LOGIN_ADMIN_CONTENT = "{\"username\":\"admin\",\"password\":\"admin\",\"rememberMe\":null}";
	final static String LOGIN_RA_CONTENT = "{\"username\":\"ra\",\"password\":\"s3cr3t\",\"rememberMe\":null}";
	final static String LOGIN_USER_CONTENT = "{\"username\":\"user\",\"password\":\"user\",\"rememberMe\":null}";
}
