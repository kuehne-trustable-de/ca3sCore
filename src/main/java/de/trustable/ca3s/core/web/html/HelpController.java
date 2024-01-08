package de.trustable.ca3s.core.web.html;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Value;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

import java.io.IOException;
import java.io.InputStreamReader;

@Controller
@RequestMapping("/app/help")
public class HelpController {

    static final String prefixHTML = "<html>\n" +
        "<head>\n" +
        "<meta charset=\"UTF-8\">\n" +
        "<title>ca3s {role} Help</title>\n" +
        "<link rel=\"stylesheet\" href=\"../../css/modest.css\">\n" +
        "</head>\n" +
        "<body>\n";

    static final String postfixHTML = "\n</body>\n</html>";
    final private Resource helpResourceEn;
    final private Resource helpResourceDe;
    final private Resource helpAdminResourceEn;
    final private Resource helpAdminResourceDe;

    public HelpController(@Value("${ca3s.help.en:classpath:help/help_en.md}") Resource helpResourceEn,
                          @Value("${ca3s.help.de:classpath:help/help_de.md}") Resource helpResourceDe,
                          @Value("${ca3s.help.admin.en:classpath:help/helpAdmin_en.md}")Resource helpAdminResourceEn,
                          @Value("${ca3s.help.admin.de:classpath:help/helpAdmin_de.md}")Resource helpAdminResourceDe) {
        this.helpResourceEn = helpResourceEn;
        this.helpResourceDe = helpResourceDe;
        this.helpAdminResourceEn = helpAdminResourceEn;
        this.helpAdminResourceDe = helpAdminResourceDe;
    }

    @GetMapping(value = "/help-{lang}.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String helpAsHTML(@PathVariable String lang) throws IOException {

        Resource helpResource = helpResourceEn;
        if( "de".equalsIgnoreCase(lang)){
            helpResource = helpResourceDe;
        }

        MutableDataSet options = new MutableDataSet();

        // uncomment to set optional extensions
        //options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create(), StrikethroughExtension.create()));

        // uncomment to convert soft-breaks to hard breaks
        //options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parseReader(new InputStreamReader(helpResource.getInputStream(),"UTF-8"));

        return getPrefixForRole("") + renderer.render(document) + postfixHTML;
    }

    @GetMapping(value = "/helpAdmin-{lang}.html", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String helpAdminAsHTML(@PathVariable String lang) throws IOException {

        Resource helpResource = helpAdminResourceEn;
        if( "de".equalsIgnoreCase(lang)){
            helpResource = helpAdminResourceDe;
        }

        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        // You can re-use parser and renderer instances
        Node document = parser.parseReader(new InputStreamReader(helpResource.getInputStream(),"UTF-8"));

        return getPrefixForRole("Admin") + renderer.render(document) + postfixHTML;
    }

    @GetMapping(value = "/help-{lang}.md", produces = "text/markdown; charset=UTF-8")
    @ResponseBody
    public String helpAsMD(@PathVariable String lang) throws IOException {

        Resource helpResource = helpResourceEn;
        if( "de".equalsIgnoreCase(lang)){
            helpResource = helpResourceDe;
        }

        return IOUtils.toString(new InputStreamReader(helpResource.getInputStream(),"UTF-8"));
    }

    private String getPrefixForRole(final String role){
        return prefixHTML.replace("{role}", role);
    }
}
