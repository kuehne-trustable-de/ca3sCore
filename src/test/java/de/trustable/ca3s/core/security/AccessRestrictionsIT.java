package de.trustable.ca3s.core.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.util.JCAManager;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@SpringBootTest(classes = Ca3SApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
public class AccessRestrictionsIT {

    private static final Logger LOG = LoggerFactory.getLogger(AccessRestrictionsIT.class);

    @LocalServerPort
    int serverPort; // random port chosen by spring test

    static ArrayList<String> knownAnonymousResources = new ArrayList<>();
    static ArrayList<String> knownUserResources = new ArrayList<>();
    static ArrayList<String> knownRaResources = new ArrayList<>();

    @BeforeAll
    public static void setUpBeforeClass() {
        JCAManager.getInstance();

        System.setProperty("springdoc.pathsToMatch", "/**");

        knownAnonymousResources.add("get /scep/{realm}");
        knownAnonymousResources.add("post /scep/{realm}");
        knownAnonymousResources.add("get /acme/{realm}/directory");
        knownAnonymousResources.add("post /acme/{realm}/directory");
        knownAnonymousResources.add("post /api/pipelineViews");
        knownAnonymousResources.add("put /api/pipelineViews");
        knownAnonymousResources.add("post /cmpTest/{alias}");
        knownAnonymousResources.add("post /api/register");
        knownAnonymousResources.add("post /api/authenticate");
        knownAnonymousResources.add("post /api/acme-challenges/validation");
        knownAnonymousResources.add("post /api/acme-challenges/pending/request-proxy-configs/{requestProxyId}");
        knownAnonymousResources.add("post /api/request-proxy-configs/remote-config/{requestProxyId}");
        knownAnonymousResources.add("get /api/account");
        knownAnonymousResources.add("post /api/account");
        knownAnonymousResources.add("post /api/account/reset-password/init");
        knownAnonymousResources.add("post /api/account/reset-password/finish");
        knownAnonymousResources.add("get /acme/{realm}/challenge/{challengeId}");
        knownAnonymousResources.add("get /acme/{realm}/cert/{certId}");
        knownAnonymousResources.add("get /acme/{realm}/authorization/{authorizationId}");
        knownAnonymousResources.add("get /api/activate");
        knownAnonymousResources.add("get /publicapi/clientAuth");
        knownAnonymousResources.add("post /publicapi/clientAuth");

        knownUserResources.add("put /api/users");
        knownUserResources.add("post /api/users");
        knownUserResources.add("post /api/clientKeystore");
        knownUserResources.add("put /api/user-preferences");
        knownUserResources.add("post /api/user-preferences");
        knownUserResources.add("put /api/preference/{userId}");
        knownUserResources.add("post /api/withdrawOwnRequest");
        knownUserResources.add("post /api/withdrawOwnCertificate");
        knownUserResources.add("post /api/uploadContent");
        knownUserResources.add("post /api/selfAdministerRequest");
        knownUserResources.add("post /api/selfAdministerCertificate");
        knownUserResources.add("post /api/account/change-password");
        knownUserResources.add("post /api/exception-translator-test/method-argument");

        knownRaResources.add("post /api/administerRequest");
        knownRaResources.add("post /api/administerCertificate");


    }

    @Test
    public void checkAccessRestriction() throws IOException {

        List<String> acceptedAnonymousList = new ArrayList<>();
        List<String> acceptedUserAuthenticatedList = new ArrayList<>();
        List<String> acceptedRaAuthenticatedList = new ArrayList<>();

        String userToken = getLoginToken("user", "user");
        String raToken = getLoginToken("ra", "s3cr3t");

            URL oasUrl = new URL("http", "localhost", serverPort, "/v3/api-docs");
        LOG.debug("Opening connection to  : " + oasUrl);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> oasContent = restTemplate.getForEntity(oasUrl.toString(), String.class);

        assertEquals(200, oasContent.getStatusCodeValue());
        JsonObject jsonRoot = JsonParser.parseString(oasContent.getBody()).getAsJsonObject();

        if( jsonRoot.has("paths")){
            JsonObject pathObject = jsonRoot.getAsJsonObject("paths");
            for(Map.Entry<String, JsonElement> entryPath: pathObject.entrySet()){

                JsonObject objectMethod = entryPath.getValue().getAsJsonObject();
                for( Map.Entry<String, JsonElement> entryMethod: objectMethod.entrySet()){

                    LOG.info( "path element found: {} / {}", entryPath.getKey(), entryMethod.getKey());
                    checkResourceAccessability(acceptedAnonymousList, entryPath.getKey(), entryMethod.getKey(), null);
                    checkResourceAccessability(acceptedUserAuthenticatedList, entryPath.getKey(), entryMethod.getKey(), userToken);
                    checkResourceAccessability(acceptedRaAuthenticatedList, entryPath.getKey(), entryMethod.getKey(), raToken);
                }
            }
        }

        acceptedAnonymousList.removeAll(knownAnonymousResources);
        acceptedUserAuthenticatedList.removeAll(knownAnonymousResources);
        acceptedUserAuthenticatedList.removeAll(knownUserResources);

        acceptedRaAuthenticatedList.removeAll(knownAnonymousResources);
        acceptedRaAuthenticatedList.removeAll(knownUserResources);
        acceptedRaAuthenticatedList.removeAll(knownRaResources);

        for(String resource: acceptedAnonymousList){
            LOG.warn("### Resource accessible anonymously : " + resource);
        }

        for(String resource: acceptedUserAuthenticatedList){
            LOG.warn("+++ Resource accessible as user : " + resource);
        }

        for(String resource: acceptedRaAuthenticatedList){
            LOG.warn("+++ Resource accessible as ra : " + resource);
        }

        acceptedAnonymousList.stream().forEach( (String resourceName) -> {LOG.warn("unexpected anon item: {}", resourceName);});
        acceptedUserAuthenticatedList.stream().forEach( (String resourceName) -> {LOG.warn("unexpected authed item: {}", resourceName);});
        acceptedRaAuthenticatedList.stream().forEach( (String resourceName) -> {LOG.warn("unexpected ra item: {}", resourceName);});

        Assert.assertEquals("no anonymously accessible resources expected", 0, acceptedAnonymousList.size());
        Assert.assertEquals("no user accessible resources expected", 0, acceptedUserAuthenticatedList.size());
        Assert.assertEquals("no ra accessible resources expected", 0, acceptedRaAuthenticatedList.size());
    }

    private void checkResourceAccessability(List<String> acceptedList,
                                            String path,
                                            String method,
                                            String authenticationToken) {

        String effectivePath = path.replace("{requestProxyId}", "1")
            .replace("{id}", "1")
            .replace("{", "").replace("}", "");
        try {
            URL oasUrl = new URL("http", "localhost", serverPort, effectivePath);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if( authenticationToken != null){
                headers.setBearerAuth(authenticationToken);
            }

            HttpEntity<String> request = new HttpEntity<>("{}",headers);

            if ("get".equalsIgnoreCase(method)) {
                ResponseEntity<String> responseContent = restTemplate.getForEntity(oasUrl.toString(), String.class);
                checkResponse(responseContent, effectivePath, method);
            } else if ("head".equalsIgnoreCase(method)) {
                restTemplate.headForHeaders(oasUrl.toString());
            } else if ("post".equalsIgnoreCase(method)) {
                ResponseEntity<String> responseContent = restTemplate.postForEntity(oasUrl.toString(), request, String.class);
                checkResponse(responseContent, effectivePath, method);
            } else if ("put".equalsIgnoreCase(method)) {
                restTemplate.put(oasUrl.toString(), request);
            } else if ("delete".equalsIgnoreCase(method)) {
                restTemplate.delete(oasUrl.toString());
            } else {
                LOG.info("unexpected method: {} for path {}", method, path);
            }


        }catch( HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized unauth ){
            // as expected
            LOG.info("resource {} / {} rejected", effectivePath, method );
        }catch(HttpServerErrorException serverError){
            LOG.warn("resource {} / {} accepted, returns serverError: {}", effectivePath, method, serverError.getMessage() );
            acceptedList.add(method + " " + path);
        }catch(HttpClientErrorException.BadRequest badRequest){
            LOG.warn("resource {} / {} accepted, returns badRequest: {}", effectivePath, method, badRequest.getMessage() );
            acceptedList.add(method + " " + path);
        }catch(HttpClientErrorException clientErrorException){
            LOG.warn("HTTP client problem", clientErrorException);
        }catch(MalformedURLException malformedURLException){
            LOG.warn("problem with URL", malformedURLException);
        }catch(Throwable throwable){
            LOG.warn("caught throwable: ", throwable);
        }

    }

    private void checkResponse(ResponseEntity<String> responseContent, String effectivePath, String method) {

        LOG.info( "unauthenticated status {} for method: {} / path {}", responseContent.getStatusCodeValue(), method, effectivePath);
    }

    private String getLoginToken(String user, String password) throws MalformedURLException {

        URL oasUrl = new URL("http", "localhost", serverPort, "/api/authenticate");

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>("{\"username\":\"" + user + "\",\"password\":\""+password+"\",\"rememberMe\":null}\n",headers);
        ResponseEntity<String> responseContent = restTemplate.postForEntity(oasUrl.toString(), request, String.class);

        List<String> bearerTokenList = responseContent.getHeaders().get("Authorization");
        if(bearerTokenList  != null && !bearerTokenList.isEmpty()){
            return bearerTokenList.get(0).substring(7);
        }
        return null;
    }
}
