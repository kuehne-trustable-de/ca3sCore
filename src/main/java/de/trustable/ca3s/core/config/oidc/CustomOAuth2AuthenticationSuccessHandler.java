package de.trustable.ca3s.core.config.oidc;

import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomOAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final Logger LOG = LoggerFactory.getLogger(CustomOAuth2AuthenticationSuccessHandler.class);

    private final TokenProvider tokenProvider;

    public CustomOAuth2AuthenticationSuccessHandler(TokenProvider tokenProvider) {
        super("/");
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {


        tokenProvider.setAuthenticationCookie(response, authentication, "/login");

        LOG.debug("onAuthenticationSuccess: {}", authentication);

        super.onAuthenticationSuccess(request,  response, authentication);
    }
}
