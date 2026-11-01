package de.trustable.ca3s.core.web.html;

import de.trustable.ca3s.core.domain.Pipeline;
import de.trustable.ca3s.core.domain.User;
import de.trustable.ca3s.core.domain.enumeration.ContentRelationType;
import de.trustable.ca3s.core.domain.enumeration.PipelineType;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentStatus;
import de.trustable.ca3s.core.domain.enumeration.ProtectedContentType;
import de.trustable.ca3s.core.service.util.PipelineUtil;
import de.trustable.ca3s.core.service.util.ProtectedContentUtil;
import de.trustable.ca3s.core.service.util.RandomUtil;
import de.trustable.ca3s.core.service.util.UserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NDESPasswordController {

    private final static Logger LOG = LoggerFactory.getLogger(NDESPasswordController.class);

    @Value("${ca3s.help.en:classpath:templates/web/NDES_PASSWORD.html}")
    private Resource ndesPasswordTemplate;

    final private ProtectedContentUtil protectedContentUtil;
    private final PipelineUtil pipeUtil;

    final private UserUtil userUtil;
    final private RandomUtil randomUtil;

    final String caThumbprint;

    public NDESPasswordController(ProtectedContentUtil protectedContentUtil, PipelineUtil pipeUtil, UserUtil userUtil, RandomUtil randomUtil,
                                  @Value("${ca3s.scep.ndes.thumbprint:26311695 A37CB314 CD6EBC01 33D31EF9}") String caThumbprint) {
        this.protectedContentUtil = protectedContentUtil;
        this.pipeUtil = pipeUtil;
        this.userUtil = userUtil;
        this.randomUtil = randomUtil;
        this.caThumbprint = caThumbprint;
    }

    @GetMapping(value = {"/certsrv/mscep", "/certsrv/mscep/mscep.dll", "/certsrv/mscep_admin"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> ndesDefaultPasswordHTML(@RequestHeader Map<String, String> headers) throws IOException {
        return ndesPasswordHTML("default", headers);
    }

    @GetMapping(value = {"/certsrv/{realm}/mscep","/certsrv/{realm}/mscep/mscep.dll", "/certsrv/{realm}/mscep_admin"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> ndesPasswordHTML(@PathVariable("realm") String realm,
                                                   @RequestHeader Map<String, String> headers) throws IOException {

        String authHeader = null;
        for( String headerName : headers.keySet() ) {
            if("Authorization".equalsIgnoreCase(headerName)){
                authHeader = headers.get(headerName);
                break;
            }
        }

        String[] authParts = extractCredentials(authHeader);

        if( authParts == null){

            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"ndes\"")
                .build();
        }
        String login = authParts[0];
        String secret = authParts[1];

        User user = userUtil.getUserByLogin(login);
        protectedContentUtil.verifySCEPUser(user, secret);

        String tokenNDES = randomUtil.generateScepToken();

        Pipeline pipeline = pipeUtil.getPipelineByRealm(PipelineType.SCEP, realm);
        if( pipeline == null ) {
            LOG.info("no matching pipeline for scep request realm {}", realm);
        }else if( !pipeline.isActive() ) {
            LOG.info("Deactivated pipeline '{}' found for scep request realm '{}'", pipeline.getName(), realm);
        }else {

            Instant validTo = Instant.now().plus(3600, ChronoUnit.SECONDS);
            protectedContentUtil.createProtectedContent(tokenNDES,
                ProtectedContentType.TOKEN,
                ContentRelationType.NDES_TOKEN,
                user.getId(),
                -1,
                validTo,
                pipeline,
                ProtectedContentStatus.ACTIVE);
        }

        InputStream is = ndesPasswordTemplate.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String responsePage = reader.lines().collect(Collectors.joining(System.lineSeparator()));

        return ResponseEntity
            .ok().body(
                responsePage.replace("{$password}", tokenNDES)
                    .replace("{$thumbprint}",caThumbprint)
            );

    }

    public String[] extractCredentials(String authHeader) {
        if (authHeader != null){
           if(authHeader.toLowerCase(Locale.ROOT).startsWith("basic ")) {
                String base64Credentials = authHeader.substring("Basic ".length()).trim();
                byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                final String[] values = credentials.split(":", 2);

                if (values.length == 2) {
                    LOG.info("Basic Auth parts '{}' / '{}' ", values[0], values[1]);
                    return values;
                }
            }else{
                LOG.info("Auth header has unexpected method {}", authHeader.substring(0, 5));
            }
        }else{
            LOG.info("Auth header is missing");
        }
        return null;
    }
}
