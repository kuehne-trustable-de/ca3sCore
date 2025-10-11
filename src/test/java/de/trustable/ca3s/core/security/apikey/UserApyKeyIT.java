package de.trustable.ca3s.core.security.apikey;

import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.ca3s.core.service.dto.AccountCredentialsType;
import de.trustable.ca3s.core.service.dto.CredentialUpdateType;
import de.trustable.ca3s.core.service.dto.PasswordChangeDTO;
import de.trustable.ca3s.core.service.dto.UserDTO;
import de.trustable.ca3s.core.service.util.UserUtil;
import de.trustable.ca3s.core.web.rest.JWTToken;
import de.trustable.ca3s.core.web.rest.vm.LoginData;
import de.trustable.ca3s.core.web.rest.vm.TokenRequest;
import de.trustable.ca3s.core.web.rest.vm.TokenResponse;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static de.trustable.ca3s.core.ui.WebTestBase.USER_NAME_USER;
import static de.trustable.ca3s.core.ui.WebTestBase.USER_PASSWORD_USER;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class UserApyKeyIT {

    private static final Logger LOG = LoggerFactory.getLogger(UserApyKeyIT.class);
    public static final String X_API_KEY = "X-API-KEY";

    static String apiKey;

    @LocalServerPort
	int serverPort; // random port chosen by spring test

    String basePath;
	final String USER_PATH_PART = "/api/users";

    @Autowired
    UserUtil userUtil;


    @BeforeEach
	void init() {
        basePath = "http://localhost:" + serverPort;
        userUtil.updateUserByLogin(USER_NAME_USER, USER_PASSWORD_USER, "user@localhost");
    }

    @BeforeAll
	public static void setUpBeforeClass() throws NoSuchAlgorithmException {
		JCAManager.getInstance();

        apiKey = "TEST_APIKEY_Myc4pWzjhdg4UTklh7jgfdFJ9F3OHDeibPku3LDKEJj6HgPub1CPJLWPgyOoNSBzVgy7Vk1IaqI" +
            "intV3qHvPU6MixZYawEwuZyXYXRtR1scPUkcPW5dq" + SecureRandom.getInstanceStrong().nextLong();

        System.setProperty("ca3s.auth.api-key.enabled", "true");
        System.setProperty("ca3s.auth.api-key.auth-token-header-name", X_API_KEY);
//        System.setProperty("ca3s.auth.api-key.auth-token-admin", apiKey);


    }

    @Test
    public void testValidAPIKeyGet() {

        Client client = ClientBuilder.newClient();

        String jwt = getUserJWT(client);

        String apiKey = getUserApiToken(client, jwt);

        getUserDetails(client, "", apiKey, 200);
        getUserDetails(client, jwt, "", 200);
        getUserDetails(client, "", "", 401);

        getUserDetails(client, "", apiKey + "999", 401);

        StringBuffer buffer = new StringBuffer(apiKey);
        int c = (int)buffer.charAt(6);
        buffer.setCharAt(6, (char)(c^3));
        getUserDetails(client, "", buffer.toString(), 401);

    }

    private  String getUserJWT(Client client) {
        WebTarget webTarget = client.target( basePath + "/api/authenticate");
        Invocation.Builder invocationBuilderLogin = webTarget.request();
        LoginData loginData = new LoginData();
        loginData.setUsername(USER_NAME_USER);
        loginData.setPassword(USER_PASSWORD_USER);
        loginData.setSecondSecret("NONE");
        Response responseLogin = invocationBuilderLogin.post(Entity.entity(loginData, MediaType.APPLICATION_JSON));
        Assertions.assertEquals(200, responseLogin.getStatus());
        JWTToken jwtToken = responseLogin.readEntity(JWTToken.class);
        return jwtToken.getIdToken();
    }

    private  String getUserApiToken(Client client, String jwt) {

        WebTarget webTargetToken = client.target( basePath + "/api/token/apiToken");
        Invocation.Builder invocationBuilderToken = webTargetToken.request();
        TokenRequest tokenRequest = new TokenRequest();
        tokenRequest.setCredentialType(AccountCredentialsType.API_TOKEN);
        tokenRequest.setValiditySeconds(3600);
        Response responseToken = invocationBuilderToken
            .header("Authorization", "Bearer " + jwt)
            .post(Entity.entity(tokenRequest, MediaType.APPLICATION_JSON));
        Assertions.assertEquals(200, responseToken.getStatus());
        TokenResponse tokenResponse = responseToken.readEntity(TokenResponse.class);

        WebTarget webTarget = client.target( basePath + "/api/account/change-password");
        Invocation.Builder invocationBuilderStoreToken = webTarget.request();
        PasswordChangeDTO passwordChangeDto = new PasswordChangeDTO();
        passwordChangeDto.setApiTokenValue(tokenResponse.getTokenValue());
        passwordChangeDto.setApiTokenValiditySeconds(3600);
        passwordChangeDto.setCurrentPassword(USER_PASSWORD_USER);
        passwordChangeDto.setCredentialUpdateType(CredentialUpdateType.API_TOKEN);
        Response responseLogin = invocationBuilderStoreToken
            .header("Authorization", "Bearer " + jwt)
            .post(Entity.entity(passwordChangeDto, MediaType.APPLICATION_JSON));
        Assertions.assertEquals(200, responseLogin.getStatus());

        return tokenResponse.getTokenValue();
    }

    private UserDTO getUserDetails(Client client, String jwt, String apiKey, int expectedResponseStatus) {

        UserDTO userDTO = null;

        WebTarget webTarget = client.target( basePath + "/api/users/" + USER_NAME_USER);
        Invocation.Builder invocationBuilderListSettings = webTarget.request();

        if( jwt != null && !jwt.isEmpty()) {
            invocationBuilderListSettings.header("Authorization", "Bearer " + jwt);
        }
        if( apiKey != null && !apiKey.isEmpty()) {
            invocationBuilderListSettings.header(X_API_KEY, apiKey);
        }

        Response responseAttributes = invocationBuilderListSettings.get();
        Assertions.assertEquals(expectedResponseStatus, responseAttributes.getStatus());
        if( 200 == expectedResponseStatus) {
            Assertions.assertEquals("application/json", responseAttributes.getHeaderString("Content-Type"));

            userDTO = responseAttributes.readEntity(UserDTO.class);
            LOG.info("userDTO: {}", userDTO.getLogin());
        }
        return userDTO;
    }

 }
