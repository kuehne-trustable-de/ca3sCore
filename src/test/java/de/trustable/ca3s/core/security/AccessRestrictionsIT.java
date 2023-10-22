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
    }

    @Test
    public void checkAccessrestriction() throws IOException {

        List<String> acceptedList = new ArrayList<>();

        URL oasUrl = new URL("http", "localhost", serverPort, "/v3/api-docs");
        LOG.debug("Opening connection to  : " + oasUrl);

        RestTemplate restTemplate = new RestTemplate();

//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ResponseEntity<String> oasContent = restTemplate.getForEntity(oasUrl.toString(), String.class);

        assertEquals(200, oasContent.getStatusCodeValue());
        JsonObject jsonRoot = JsonParser.parseString(oasContent.getBody()).getAsJsonObject();

        if( jsonRoot.has("paths")){
            JsonObject pathObject = jsonRoot.getAsJsonObject("paths");
            for(Map.Entry<String, JsonElement> entryPath: pathObject.entrySet()){

                JsonObject objectMethod = entryPath.getValue().getAsJsonObject();
                for( Map.Entry<String, JsonElement> entryMethod: objectMethod.entrySet()){

                    LOG.info( "path element found: {} / {}", entryPath.getKey(), entryMethod.getKey());
                    checkResourceAccessability(acceptedList, entryPath.getKey(), entryMethod.getKey());
                }
            }
        }

        acceptedList.removeAll(knownAnonymousResources);

        for(String resource: acceptedList){
            LOG.warn("### Resource accessible: " + resource);
        }

        Assert.assertEquals("no anonymously accessable resources expected", 0, acceptedList.size());
    }

    private void checkResourceAccessability(List<String> acceptedList, String path, String method) {

        String effectivePath = path.replace("{requestProxyId}", "1")
            .replace("{", "").replace("}", "");
        try {
            URL oasUrl = new URL("http", "localhost", serverPort, effectivePath);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
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


        }catch( HttpClientErrorException.Unauthorized unauthorized ){
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
        }

    }

    private void checkResponse(ResponseEntity<String> responseContent, String effectivePath, String method) {

        LOG.info( "unauthenticated status {} for method: {} / path {}", responseContent.getStatusCodeValue(), method, effectivePath);
    }
}
