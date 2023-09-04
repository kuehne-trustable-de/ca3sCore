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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStreamReader;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@RequestMapping("/error")
public class ErrorController {

    static final String prefixHTML = "<html>\n" +
        "<head>\n" +
        "<meta charset=\"UTF-8\">\n" +
        "<title>ca3s Error</title>\n" +
        "<link rel=\"stylesheet\" href=\"../../css/modest.css\">\n" +
        "</head>\n" +
        "<body>\n";

    static final String postfixHTML = "\n</body>\n</html>";

    @Value("${ca3s.error.en:classpath:error/error_en.md}")
    private Resource helpResourceEn;

    @Value("${ca3s.error.de:classpath:error/error_de.md}")
    private Resource helpResourceDe;

    @RequestMapping(value = "/error-{lang}.html",
        produces = MediaType.TEXT_HTML_VALUE,
        method={POST, GET})
    @ResponseBody
    public String helpAsHTML(@PathVariable String lang) throws IOException {

        Resource helpResource = helpResourceEn;
        if ("de".equalsIgnoreCase(lang)) {
            helpResource = helpResourceDe;
        }

        MutableDataSet options = new MutableDataSet();

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parseReader(new InputStreamReader(helpResource.getInputStream(), "UTF-8"));

        return prefixHTML + renderer.render(document) + postfixHTML;
    }

    @GetMapping(value = "/error-{lang}.md", produces = "text/markdown; charset=UTF-8")
    @ResponseBody
    public String helpAsMD(@PathVariable String lang) throws IOException {

        Resource helpResource = helpResourceEn;
        if ("de".equalsIgnoreCase(lang)) {
            helpResource = helpResourceDe;
        }

        return IOUtils.toString(new InputStreamReader(helpResource.getInputStream(), "UTF-8"));
    }
}
