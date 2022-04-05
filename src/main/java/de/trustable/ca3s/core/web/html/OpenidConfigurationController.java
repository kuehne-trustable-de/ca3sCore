package de.trustable.ca3s.core.web.html;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/auth/realms")
public class OpenidConfigurationController {

    @Value("${ca3s.oidc.configurationFile:classpath:public/openid-configuration/openid-configuration.json}")
    private Resource oidcConfigResource;

    @GetMapping(value = "/{realm}/.well-known/openid-configuration", produces = MediaType.APPLICATION_JSON_VALUE )
    public void getOpenidConfiguration(HttpServletResponse response) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        StreamUtils.copy(oidcConfigResource.getInputStream(), response.getOutputStream());
    }
}
