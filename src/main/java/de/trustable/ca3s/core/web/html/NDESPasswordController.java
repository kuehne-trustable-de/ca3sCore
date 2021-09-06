package de.trustable.ca3s.core.web.html;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;
import java.nio.file.Files;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/app/{realm}/certsrv/mscep_admin")
public class NDESPasswordController {

    @Value("${ca3s.help.en:classpath:templates/web/NDES_PASSWORD.html}")
    private Resource ndesPasswordTemplate;

    @GetMapping(value = "/*", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String ndesPasswordHTML(@PathVariable("realm") String realm) throws IOException {

        InputStream is = ndesPasswordTemplate.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
}
