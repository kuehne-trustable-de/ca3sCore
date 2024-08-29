package de.trustable.ca3s.core.security.saml;

import de.trustable.ca3s.core.security.jwt.TokenProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CustomUrlAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String CA3S_JWT_COOKIE_NAME = "ca3sJWT";

    protected final Log logger = LogFactory.getLog(this.getClass());

    private final TokenProvider tokenProvider;

    private final RequestCache requestCache = new HttpSessionRequestCache();

    private final boolean secureCookie;

    public CustomUrlAuthenticationSuccessHandler(TokenProvider tokenProvider,
                                                 @Value("${ca3s.ui.sso.secureCookie:true}") boolean secureCookie) {
        this.tokenProvider = tokenProvider;
        this.secureCookie = secureCookie;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        String jwt = tokenProvider.createToken(authentication, false);

        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            this.logger.debug(LogMessage.format("Did not redirect to %s since response already committed.", targetUrl));
        } else {
            this.logger.debug(LogMessage.format("Redirect to %s .", targetUrl));
            Cookie authCookie = new Cookie(CA3S_JWT_COOKIE_NAME, jwt);
            authCookie.setMaxAge(60); // expires in a minute
            authCookie.setSecure(secureCookie);
            authCookie.setHttpOnly(false);
            authCookie.setPath("/"); // global cookie accessible everywhere
            response.addCookie(authCookie);
            this.logger.debug("Setting JWT as cookie ca3sJWT.");
        }

        super.onAuthenticationSuccess(request, response, authentication);

    }

}
