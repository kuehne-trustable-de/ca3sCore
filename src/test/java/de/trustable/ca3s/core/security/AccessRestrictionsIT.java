package de.trustable.ca3s.core.security;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.trustable.ca3s.core.Ca3SApp;
import de.trustable.util.JCAManager;
import org.junit.jupiter.api.Assertions;
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

        knownAnonymousResources.add("get /actuator/health");

        knownAnonymousResources.add("get /scep/{realm}");
        knownAnonymousResources.add("post /scep/{realm}");
        knownAnonymousResources.add("get /scep/{realm}/pkiclient.exe");
        knownAnonymousResources.add("post /scep/{realm}/pkiclient.exe");
        knownAnonymousResources.add("get /scep/{realm}/cgi-bin/pkiclient.exe");
        knownAnonymousResources.add("post /scep/{realm}/cgi-bin/pkiclient.exe");
        knownAnonymousResources.add("get /acme/{realm}/directory");
        knownAnonymousResources.add("post /acme/{realm}/directory");
        knownAnonymousResources.add("post /api/pipelineViews");
        knownAnonymousResources.add("put /api/pipelineViews");
        knownAnonymousResources.add("post /cmpTest/{alias}");
        knownAnonymousResources.add("post /api/register");
        knownAnonymousResources.add("post /api/authenticate");
        knownAnonymousResources.add("post /api/authenticateLDAP");
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
        knownAnonymousResources.add("get /publicapi/requestsByMonth");

        knownAnonymousResources.add("get /publicapi/clientAuthKeystore/{login}/{filename}");
        knownAnonymousResources.add("post /publicapi/clientAuthKeystore/{login}/{filename}");
        knownAnonymousResources.add("get /publicapi/keystore/{certId}/{filename}/{alias}");
        knownAnonymousResources.add("get /publicapi/cert/{certId}");

        knownAnonymousResources.add("get /publicapi/certPKIX/{certId}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEM/{certId}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMPart/{certId}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMChain/{certId}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMFull/{certId}/{filename}");

        knownAnonymousResources.add("get /publicapi/certPKIX/{certId}/ski/{ski}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEM/{certId}/ski/{ski}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMPart/{certId}/ski/{ski}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMChain/{certId}/ski/{ski}/{filename}");
        knownAnonymousResources.add("get /publicapi/certPEMFull/{certId}/ski/{ski}/{filename}");

        knownAnonymousResources.add("post /publicapi/smsDelivery/{user}");

        knownAnonymousResources.add("get /.well-known/est/{label}/csrattrs");
        knownAnonymousResources.add("get /.well-known/est/{label}/cacerts");
        knownAnonymousResources.add("get /.well-known/est/csrattrs");
        knownAnonymousResources.add("get /.well-known/est/cacerts");

        knownUserResources.add("put /api/users");
        knownUserResources.add("post /api/users");
        knownUserResources.add("post /api/token/apiToken");
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
        knownUserResources.add("post /api/smsDelivery");

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

        Assertions.assertEquals(200, oasContent.getStatusCodeValue());
        JsonObject jsonRoot = JsonParser.parseString(oasContent.getBody()).getAsJsonObject();

        ArrayList<String> unreachableKnownAnonymousResources = new ArrayList<>(knownAnonymousResources);
        ArrayList<String> unreachableAcceptedUserAuthenticatedList = new ArrayList<>(knownUserResources);
        ArrayList<String> unreachableAcceptedRaAuthenticatedList = new ArrayList<>(knownRaResources);

        if( jsonRoot.has("paths")){
            JsonObject pathObject = jsonRoot.getAsJsonObject("paths");
            for(Map.Entry<String, JsonElement> entryPath: pathObject.entrySet()){

                JsonObject objectMethod = entryPath.getValue().getAsJsonObject();
                for( Map.Entry<String, JsonElement> entryMethod: objectMethod.entrySet()){

                    LOG.info( "path element found: {} / {}", entryPath.getKey(), entryMethod.getKey());
                    checkResourceAccessability(acceptedAnonymousList, entryPath.getKey(), entryMethod.getKey(), null);
                    checkResourceAccessability(acceptedUserAuthenticatedList, entryPath.getKey(), entryMethod.getKey(), userToken);
                    checkResourceAccessability(acceptedRaAuthenticatedList, entryPath.getKey(), entryMethod.getKey(), raToken);

                    unreachableKnownAnonymousResources.remove(entryPath.getKey());
                    unreachableAcceptedUserAuthenticatedList.remove(entryPath.getKey());
                    unreachableAcceptedRaAuthenticatedList.remove(entryPath.getKey());
                }
            }
        }

//        Assert.assertTrue( acceptedAnonymousList.contains("/publicapi/certPKIX/{certId}/ski/{ski}/{filename}"));

        unreachableKnownAnonymousResources.forEach(resource -> LOG.warn("### Unimplemented Resource accessible anonymously : " + resource));
        unreachableAcceptedUserAuthenticatedList.forEach(resource -> LOG.warn("+++ Unimplemented Resource accessible as user : " + resource));
        unreachableAcceptedRaAuthenticatedList.forEach(resource -> LOG.warn("+++ Unimplemented Resource accessible as ra : " + resource));
/*
        Assertions.assertEquals(0, unreachableKnownAnonymousResources.size(), "no unimplemented anonymously accessible resources expected");
        Assertions.assertEquals(0, unreachableAcceptedUserAuthenticatedList.size(), "no unimplemented user accessible resources expected");
        Assertions.assertEquals(0, unreachableAcceptedRaAuthenticatedList.size(), "no unimplemented ra accessible resources expected");
*/
        unreachableKnownAnonymousResources.forEach(resource -> LOG.warn("### Expected Resource not implemented : " + resource));

        acceptedAnonymousList.removeAll(knownAnonymousResources);
        acceptedUserAuthenticatedList.removeAll(knownAnonymousResources);
        acceptedUserAuthenticatedList.removeAll(knownUserResources);

        acceptedRaAuthenticatedList.removeAll(knownAnonymousResources);
        acceptedRaAuthenticatedList.removeAll(knownUserResources);
        acceptedRaAuthenticatedList.removeAll(knownRaResources);

        acceptedAnonymousList.forEach(resource -> LOG.warn("### Resource accessible anonymously : " + resource));
        acceptedUserAuthenticatedList.forEach(resource -> LOG.warn("+++ Resource accessible as user : " + resource));
        acceptedRaAuthenticatedList.forEach(resource -> LOG.warn("+++ Resource accessible as ra : " + resource));


        Assertions.assertEquals(0, acceptedAnonymousList.size(), "no anonymously accessible resources expected");
        Assertions.assertEquals(0, acceptedUserAuthenticatedList.size(), "no user accessible resources expected");
        Assertions.assertEquals(0, acceptedRaAuthenticatedList.size(), "no ra accessible resources expected");
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

        LOG.info( "status {} for method: {} / path {}", responseContent.getStatusCodeValue(), method, effectivePath);
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
