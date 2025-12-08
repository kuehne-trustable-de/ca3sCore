package de.trustable.ca3s.core.web.html;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Controller
public class NDESPasswordController {

    @Value("${ca3s.help.en:classpath:templates/web/NDES_PASSWORD.html}")
    private Resource ndesPasswordTemplate;


    @GetMapping(value = {"/certsrv/mscep", "/certsrv/mscep/mscep.dll", "/certsrv/mscep_admin"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String ndesDefaultPasswordHTML() throws IOException {

        InputStream is = ndesPasswordTemplate.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return ndesPasswordHTML("default");
    }

    @GetMapping(value = {"/certsrv/{realm}/mscep","/certsrv/{realm}/mscep/mscep.dll", "/certsrv/{realm}/mscep_admin"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String ndesPasswordHTML(@PathVariable("realm") String realm) throws IOException {
        InputStream is = ndesPasswordTemplate.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
