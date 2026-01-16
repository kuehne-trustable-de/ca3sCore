package de.trustable.ca3s.core.security.saml;

import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    protected final Log logger = LogFactory.getLog(this.getClass());

    private final TokenProvider tokenProvider;

    public CustomUrlAuthenticationSuccessHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        String targetUrl = determineTargetUrl(request, response, authentication);
        tokenProvider.setAuthenticationCookie(response, authentication, targetUrl);
        super.onAuthenticationSuccess(request, response, authentication);

    }

}
