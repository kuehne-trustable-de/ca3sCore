package de.trustable.ca3s.core.web.html;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.IOException;
import java.io.InputStreamReader;

@Controller
@RequestMapping("/app/help")
public class HelpController {

    static final String prefixHTML = "<html>\n" +
        "<head>\n" +
        "<meta charset=\"UTF-8\">\n" +
        "<title>ca3s Help</title>\n" +
        "<link rel=\"stylesheet\" href=\"../../css/modest.css\">\n" +
        "</head>\n" +
        "<body>\n";

    static final String postfixHTML = "\n</body>\n</html>";

    @Value("${ca3s.help.en:classpath:help/help_en.md}")
    private Resource helpResourceEn;

    @Value("${ca3s.help.en:classpath:help/help_de.md}")
    private Resource helpResourceDe;

    @GetMapping(value = "/help-{lang}.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String helpAsHTML(@PathVariable String lang) throws IOException {

        Resource helpResource = helpResourceEn;
        if( "de".equalsIgnoreCase(lang)){
            helpResource = helpResourceDe;
        }
        Parser parser = Parser.builder().build();
        Node document = parser.parseReader(new InputStreamReader(helpResource.getInputStream(),"UTF-8"));
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return prefixHTML + renderer.render(document) + postfixHTML;
    }
}
