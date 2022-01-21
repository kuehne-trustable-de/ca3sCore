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
@RequestMapping("/app/resource")
public class ResourceController {

    @Value("${ca3s.ui.logo:classpath:images/ca3s-36x36.png}")
    private Resource logoImageResource;

    @GetMapping(value = "/logo.png", produces = MediaType.IMAGE_PNG_VALUE)
    public void getImage(HttpServletResponse response) throws IOException {

        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        StreamUtils.copy(logoImageResource.getInputStream(), response.getOutputStream());
    }
}
